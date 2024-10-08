package ru.gpb.jpixelscoringbot.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gpb.jpixelscoringbot.config.TelegramBotProperties;
import ru.gpb.jpixelscoringbot.dto.AnswerDto;
import ru.gpb.jpixelscoringbot.dto.QuestionDto;
import ru.gpb.jpixelscoringbot.dto.QuestionTypeDto;
import ru.gpb.jpixelscoringbot.exception.JPixelScoringBotException;
import ru.gpb.jpixelscoringbot.service.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;
import static ru.gpb.jpixelscoringbot.component.UserState.AWAITING_NAME;
import static ru.gpb.jpixelscoringbot.config.Constants.*;

@Slf4j
@Component
public class JPixelScoringBot extends AbilityBot {

    private final SilentSender silentSender;
    private final MessageSender messageSender;
    private final JPixelScoringBotState botState;
    
    private final UserService userService;
    private final MinioService minioService;
    private final ReportService reportService;
    private final AnswerService answerService;
    private final QuestionTypeService questionTypeService;
    private final Map<String, QuestionService> questionService;

    public JPixelScoringBot(
            TelegramBotProperties telegramBotProperties,
            UserService userService,
            MinioService minioService,
            ReportService reportService,
            AnswerService answerService,
            QuestionTypeService questionTypeService,
            List<QuestionService> questionServiceList) {
        super(telegramBotProperties.getToken(), telegramBotProperties.getUsername());

        this.silentSender = silent;
        this.messageSender = sender;
        this.botState = new JPixelScoringBotState(db.getMap(CHAT_STATES));

        this.userService = userService;
        this.minioService = minioService;
        this.reportService = reportService;
        this.answerService = answerService;
        this.questionTypeService = questionTypeService;
        this.questionService = questionServiceList.stream()
                .collect(Collectors.toMap(QuestionService::getQuestionTypeCode, Function.identity()));
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info(START_DESCRIPTION)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> replyToStart(ctx.chatId()))
                .build();
    }

    public Ability resultBot() {
        return Ability
                .builder()
                .name("result")
                .info(RESULT_DESCRIPTION)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> replyToResult(ctx.chatId(), ctx.user()))
                .build();
    }

    private void replyToStart(Long chatId) {
        silentSender.execute(createSendMessage(chatId, START_TEXT));
        botState.putChatState(chatId, AWAITING_NAME);
    }

    private void replyToResult(Long chatId, User user) {
        silentSender.execute(createSendMessage(chatId, reportService.getResultReportByUser(user.getId())));
    }

    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) ->
                        replyToButtons(getChatId(upd), upd.getMessage().getFrom(), upd.getMessage().getText());
        return Reply.of(action, Flag.TEXT, upd -> botState.userIsActive(getChatId(upd)));
    }

    public Reply replyToCallBackQuery() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) ->
                        replyToCallBackQuery(getChatId(upd), upd);
        return Reply.of(action, Flag.CALLBACK_QUERY, upd -> botState.userIsActive(getChatId(upd)));
    }


    private void replyToButtons(Long chatId, User user, String data) {
        if (data.equalsIgnoreCase("/stop")) {
            stopChat(chatId, STOP_TEXT);
        }

        switch (botState.getChatState(chatId)) {
            case AWAITING_NAME -> replyToName(chatId, user, data);
            case AWAITING_SELECT_SPECIALIZATION -> replyToSelectSpecialization(chatId, data);
            case AWAITING_START -> replyToStartTest(chatId, user);
            case AWAITING_ANSWER -> replyToAnswer(chatId, user, data);
            default -> unexpectedMessage(chatId);
        }
    }

    private void replyToCallBackQuery(Long chatId, Update upd) {
        var data = upd.getCallbackQuery().getData();

        if (StringUtils.hasText(data) && !data.equals(NO)) {
            replyToButtons(chatId, upd.getCallbackQuery().getFrom(), data);
        } else if (data.equals(NO)) {
            stopChat(chatId, EXIT_TEXT);
        } else {
            stopChat(chatId, STOP_TEXT);
        }
    }

    private void replyToName(Long chatId, User user, String data) {
        userService.createUser(user.getId(), user.getUserName(), user.getFirstName(), user.getLastName(), data);
        botState.putTelegramUser(chatId, user);

        sendMessageWithSimpleText(
                chatId,
                MessageFormat.format(HELLO_TEXT, data),
                createInlineKeyboardMarkupForQuestionTypeList(questionTypeService.findQuestionTypes()),
                UserState.AWAITING_SELECT_SPECIALIZATION
        );
    }

    private static InlineKeyboardMarkup createInlineKeyboardMarkupForQuestionTypeList(List<QuestionTypeDto> questionTypeList) {
        var markupInline = new InlineKeyboardMarkup();

        var rowsInline = new ArrayList<List<InlineKeyboardButton>>();
        questionTypeList.forEach(
                qt -> {
                    var rowInline = new ArrayList<InlineKeyboardButton>();

                    var inlineKeyboardButton = new InlineKeyboardButton(qt.code());
                    inlineKeyboardButton.setCallbackData(qt.code());
                    rowInline.add(inlineKeyboardButton);

                    rowsInline.add(rowInline);
                }
        );

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private void replyToSelectSpecialization(Long chatId, String data) {
        botState.putQuestions(chatId, questionService.get(data).findQuestions());

        sendMessageWithSimpleText(
                chatId,
                MessageFormat.format(START_TEST_TEXT, data),
                createInlineKeyboardMarkup(),
                UserState.AWAITING_START
        );
    }

    private static InlineKeyboardMarkup createInlineKeyboardMarkup() {
        var markupInline = new InlineKeyboardMarkup();

        var rowsInline = new ArrayList<List<InlineKeyboardButton>>();

        var rowInline = new ArrayList<InlineKeyboardButton>();

        var inlineKeyboardButton1 = new InlineKeyboardButton(YES);
        inlineKeyboardButton1.setCallbackData(YES);
        var inlineKeyboardButton2 = new InlineKeyboardButton(NO);
        inlineKeyboardButton2.setCallbackData(NO);

        rowInline.add(inlineKeyboardButton1);
        rowInline.add(inlineKeyboardButton2);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    private void replyToStartTest(Long chatId, User user) {
        var questions = botState.getQuestions(chatId);

        questions.stream()
                .map(QuestionDto::questionTypeCode)
                .findFirst()
                .ifPresent(s -> reportService.removePreviousReport(user.getId(), s));

        var question = getQuestion(questions, 1);

        if (Objects.isNull(question)) {
            stopChat(chatId, END_TEXT);
            return;
        }

        var answerList = answerService.findByQuestionId(question.id());

        sendMessageWithSimpleTextTimer(
                chatId,
                QuestionBuilder.build(question, answerList),
                createInlineKeyboardMarkupForAnswer(answerList),
                question
        );
    }

    private void replyToAnswer(Long chatId, User user, String data) {
        botState.cancelTimerIfWorking(chatId);

        var answer = answerService.findById(Long.valueOf(data));
        var questions = botState.getQuestions(chatId);
        var nextQuestion = getQuestion(questions, answer.questionNumber() + 1);

        reportService.createAndSaveReport(user.getId(), answer.questionId(), answer.id());

        if (Objects.isNull(nextQuestion)) {
            stopChat(chatId, END_TEXT);
            return;
        }

        var answerList = answerService.findByQuestionId(nextQuestion.id());

        editMessageWithSimpleTextTimer(
                chatId,
                botState.getTelegramMessage(chatId),
                QuestionBuilder.build(nextQuestion, answerList),
                createInlineKeyboardMarkupForAnswer(answerList),
                nextQuestion
        );
    }

    private void replyToAnswer(Long chatId, User user, QuestionDto currentQuestion) {
        botState.removeTimerThread(chatId);

        var questions = botState.getQuestions(chatId);
        var nextQuestion = getQuestion(questions, currentQuestion.questionNumber() + 1);

        reportService.createAndSaveReport(user.getId(), currentQuestion.id(), null);

        if (Objects.isNull(nextQuestion)) {
            stopChat(chatId, END_TEXT);
            return;
        }

        var answerList = answerService.findByQuestionId(nextQuestion.id());

        editMessageWithSimpleTextTimer(
                chatId,
                botState.getTelegramMessage(chatId),
                QuestionBuilder.build(nextQuestion, answerList),
                createInlineKeyboardMarkupForAnswer(answerList),
                nextQuestion
        );
    }

    private static InlineKeyboardMarkup createInlineKeyboardMarkupForAnswer(List<AnswerDto> answerList) {
        var markupInline = new InlineKeyboardMarkup();

        var rowsInline = new ArrayList<List<InlineKeyboardButton>>();
        answerList.forEach(
                a -> {
                    var rowInline = new ArrayList<InlineKeyboardButton>();

                    var inlineKeyboardButton = new InlineKeyboardButton(String.valueOf(a.answerNumber()));
                    inlineKeyboardButton.setCallbackData(a.id().toString());
                    rowInline.add(inlineKeyboardButton);

                    rowsInline.add(rowInline);
                }
        );

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    private void sendMessageWithSimpleText(
            Long chatId, String text, ReplyKeyboard replyKeyboard, UserState userState) {
        silentSender.execute(createSendMessage(chatId, text, replyKeyboard));
        botState.putChatState(chatId, userState);
    }

    private void sendMessageWithSimpleTextTimer(
            Long chatId, String text, ReplyKeyboard replyKeyboard, QuestionDto question) {
        boolean haveImageMiniPath = StringUtils.hasText(question.imageMinioPath());

        var sentMessage = silentSender.execute(haveImageMiniPath
                ? createSendMessage(chatId, text) : createSendMessage(chatId, text, replyKeyboard));

        if (haveImageMiniPath) {
            sentPhotoMessage(chatId, replyKeyboard, question.imageMinioPath());
        }

        if (sentMessage.isPresent()) {
            var message = sentMessage.get();
            botState.putTelegramMessage(chatId, message);
            if (question.timeInSeconds() != 0) {
                startTimer(chatId, text, question, message, message.getReplyMarkup());
            }
        }

        botState.putChatState(chatId, UserState.AWAITING_ANSWER);
    }

    private void editMessageWithSimpleTextTimer(
            Long chatId, Message sentMessage, String text, InlineKeyboardMarkup inlineKeyboardMarkup, QuestionDto question) {
        var haveImageMinioPath = StringUtils.hasText(question.imageMinioPath());
        silentSender.execute(haveImageMinioPath
                ? createEditMessage(sentMessage, text) : createEditMessage(sentMessage, text, inlineKeyboardMarkup));

        var photoMessage = botState.getPhotoMessage(chatId);
        if (haveImageMinioPath && Objects.nonNull(photoMessage)) {
            silentSender.execute(createEditMessage(photoMessage, inlineKeyboardMarkup));
        } else if (haveImageMinioPath) {
            sentPhotoMessage(chatId, inlineKeyboardMarkup, question.imageMinioPath());
        } else if (Objects.nonNull(photoMessage)) {
            silentSender.execute(createDeleteMessage(photoMessage));
            botState.removePhotoMessage(chatId);
        }

        if (question.timeInSeconds() != 0) {
            startTimer(chatId, text, question, sentMessage, inlineKeyboardMarkup);
        }
    }

    private void startTimer(Long chatId, String text, QuestionDto question, Message message, InlineKeyboardMarkup inlineKeyboardMarkup) {
        var virtualThread = Thread.startVirtualThread(() -> {
            var notCancel = true;
            var haveImageMinioPath = StringUtils.hasText(question.imageMinioPath());

            for (int i = question.timeInSeconds() - 1; i >= 0; i--) {
                var textWithTimer = QuestionBuilder.replaceSeconds(text, i);

                silentSender.execute(haveImageMinioPath
                        ? createEditMessage(message, textWithTimer) : createEditMessage(message, textWithTimer, inlineKeyboardMarkup));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    notCancel = false;
                    log.info("Таймер был остановлен. Причина дан ответ на вопрос", e);
                    break;
                }
            }

            if (notCancel) {
                replyToAnswer(chatId, botState.getTelegramUser(chatId), question);
            }
        });
        botState.putTimerThread(chatId, virtualThread);
    }

    private void sentPhotoMessage(Long chatId, ReplyKeyboard replyKeyboard, String imageMinioPath) {
        try (InputStream is = minioService.getImage(imageMinioPath)) {
            var sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(new InputFile(is, "photo_question"));
            sendPhoto.setReplyMarkup(replyKeyboard);

            botState.putPhotoMessage(chatId, messageSender.sendPhoto(sendPhoto));
        } catch (TelegramApiException | IOException e) {
            throw new JPixelScoringBotException("Не удалось отправить изображение", e);
        }
    }

    private static QuestionDto getQuestion(List<QuestionDto> questions, Integer questionNumber) {
        if (CollectionUtils.isEmpty(questions) || questionNumber > questions.size()) {
            return null;
        }

        return questions.stream()
                .filter(q -> q.questionNumber() == questionNumber)
                .findFirst()
                .orElseThrow(() -> new JPixelScoringBotException("Порядок вопросов был нарушен"));
    }

    private void unexpectedMessage(Long chatId) {
        silentSender.execute(createSendMessage(chatId, UNEXPECTED_TEXT));
    }

    private void stopChat(Long chatId, String text) {
        silentSender.execute(createSendMessage(chatId, text, new ReplyKeyboardRemove(true)));

        Optional.ofNullable(botState.getTelegramMessage(chatId))
                .ifPresent(message -> silentSender.execute(createDeleteMessage(message)));
        Optional.ofNullable(botState.getPhotoMessage(chatId))
                .ifPresent(message -> silentSender.execute(createDeleteMessage(message)));

        botState.clearState(chatId);
    }

    private static SendMessage createSendMessage(Long chatId, String text) {
        return createSendMessage(chatId, text, null);
    }

    private static SendMessage createSendMessage(Long chatId, String text, ReplyKeyboard replyKeyboard) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(replyKeyboard)
                .build();
    }

    private static EditMessageText createEditMessage(Message originMessage) {
        return createEditMessage(originMessage, null, null);
    }

    private static EditMessageText createEditMessage(Message originMessage, String text) {
        return createEditMessage(originMessage, text, null);
    }

    private static EditMessageText createEditMessage(Message originMessage, InlineKeyboardMarkup inlineKeyboardMarkup) {
        return createEditMessage(originMessage, null, inlineKeyboardMarkup);
    }

    private static EditMessageText createEditMessage(Message originMessage, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        return EditMessageText.builder()
                .text(text)
                .chatId(originMessage.getChatId())
                .messageId(originMessage.getMessageId())
                .replyMarkup(inlineKeyboardMarkup)
                .build();
    }

    private static DeleteMessage createDeleteMessage(Message originMessage) {
        return DeleteMessage.builder()
                .chatId(originMessage.getChatId())
                .messageId(originMessage.getMessageId())
                .build();
    }

    @Override
    public long creatorId() {
        return 942310899192L;
    }
}
