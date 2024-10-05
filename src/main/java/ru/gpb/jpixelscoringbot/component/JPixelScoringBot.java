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
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gpb.jpixelscoringbot.config.Constants;
import ru.gpb.jpixelscoringbot.config.TelegramBotProperties;
import ru.gpb.jpixelscoringbot.dto.AnswerDto;
import ru.gpb.jpixelscoringbot.dto.QuestionDto;
import ru.gpb.jpixelscoringbot.exception.JPixelScoringBotException;
import ru.gpb.jpixelscoringbot.service.AnswerService;
import ru.gpb.jpixelscoringbot.service.QuestionService;
import ru.gpb.jpixelscoringbot.service.ReportService;
import ru.gpb.jpixelscoringbot.service.UserService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;
import static ru.gpb.jpixelscoringbot.component.UserState.AWAITING_NAME;
import static ru.gpb.jpixelscoringbot.config.Constants.*;

@Slf4j
@Component
public class JPixelScoringBot extends AbilityBot {

    private final SilentSender sender;
    private final JPixelScoringBotState botState;

    private final UserService userService;
    private final ReportService reportService;
    private final AnswerService answerService;
    private final QuestionService questionService;

    public JPixelScoringBot(
            TelegramBotProperties telegramBotProperties,
            UserService userService,
            ReportService reportService,
            AnswerService answerService,
            QuestionService questionService) {
        super(telegramBotProperties.getToken(), telegramBotProperties.getUsername());

        this.sender = silent;
        this.botState = new JPixelScoringBotState(db.getMap(CHAT_STATES));

        this.userService = userService;
        this.reportService = reportService;
        this.answerService = answerService;
        this.questionService = questionService;
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info(Constants.START_DESCRIPTION)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> replyToStart(ctx.chatId()))
                .build();
    }

    private void replyToStart(long chatId) {
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(START_TEXT);
        sender.execute(message);
        botState.putChatState(chatId, AWAITING_NAME);
    }

    private void promptWithKeyboardForState(
            long chatId, String text, ReplyKeyboard replyKeyboard, UserState userState) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(replyKeyboard);

        sender.execute(sendMessage);

        botState.putChatState(chatId, userState);
    }

    private void promptWithKeyboardForStateAndTimer(
            long chatId, String text, ReplyKeyboard replyKeyboard, QuestionDto question) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(replyKeyboard);

        Optional<Message> sentMessage = sender.execute(sendMessage);

        if (sentMessage.isPresent()) {
            Message message = sentMessage.get();
            botState.putTelegramMessage(chatId, message);
            if (question.timeInSeconds() != 0) {
                startTimer(chatId, text, question, message, message.getReplyMarkup());
            }
        }

        botState.putChatState(chatId, UserState.AWAITING_ANSWER);
    }

    private void promptWithKeyboardForStateAndEditMessageAndTimer(
            long chatId, Message sentMessage, String text, InlineKeyboardMarkup inlineKeyboardMarkup, QuestionDto question) {
        EditMessageText build = EditMessageText.builder()
                .text(text)
                .chatId(sentMessage.getChatId())
                .messageId(sentMessage.getMessageId())
                .replyMarkup(inlineKeyboardMarkup)
                .build();

        sender.execute(build);

        if (question.timeInSeconds() != 0) {
            startTimer(chatId, text, question, sentMessage, inlineKeyboardMarkup);
        }
    }

    private void startTimer(long chatId, String text, QuestionDto question, Message message, InlineKeyboardMarkup inlineKeyboardMarkup) {
        var virtualThread = Thread.startVirtualThread(() -> {
            boolean notCancel = true;

            for (int i = question.timeInSeconds() - 1; i >= 0; i--) {
                EditMessageText build = EditMessageText.builder()
                        .text(QuestionBuilder.replaceSeconds(text, i))
                        .chatId(message.getChatId())
                        .messageId(message.getMessageId())
                        .replyMarkup(inlineKeyboardMarkup)
                        .build();
                sender.execute(build);

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

    private void replyToName(long chatId, User user, String data) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        questionService.findQuestionTypes().forEach(
                qt -> {
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();

                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(qt.getCode());
                    inlineKeyboardButton.setCallbackData(qt.getCode());
                    rowInline.add(inlineKeyboardButton);

                    rowsInline.add(rowInline);
                }
        );

        markupInline.setKeyboard(rowsInline);

        userService.createUser(user.getId(), user.getUserName(), user.getFirstName(), user.getLastName(), data);
        botState.putTelegramUser(chatId, user);


        promptWithKeyboardForState(
                chatId,
                MessageFormat.format(HELLO_TEXT, data),
                markupInline,
                UserState.AWAITING_SELECT_SPECIALIZATION
        );
    }

    private void replyToSelectSpecialization(long chatId, String data) {
        botState.putQuestions(chatId, questionService.findQuestions());

        promptWithKeyboardForState(
                chatId,
                MessageFormat.format(START_TEST_TEXT, data),
                createInlineKeyboardMarkup(),
                UserState.AWAITING_START
        );
    }

    private InlineKeyboardMarkup createInlineKeyboardMarkup() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton(YES);
        inlineKeyboardButton1.setCallbackData(YES);
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton(NO);
        inlineKeyboardButton2.setCallbackData(NO);
        rowInline.add(inlineKeyboardButton1);
        rowInline.add(inlineKeyboardButton2);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    private void replyToStartTest(long chatId) {
        var questions = botState.getQuestions(chatId);
        var question = getQuestion(questions, 1);

        if (Objects.isNull(question)) {
            stopChat(chatId, END_TEXT);
            return;
        }

        var answerList = answerService.findByQuestionId(question.id());

        promptWithKeyboardForStateAndTimer(
                chatId,
                QuestionBuilder.build(question, answerList),
                createInlineKeyboardMarkup(answerList),
                question
        );
    }

    private void replyToAnswer(long chatId, User user, String data) {
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

        promptWithKeyboardForStateAndEditMessageAndTimer(
                chatId,
                botState.getTelegramMessage(chatId),
                QuestionBuilder.build(nextQuestion, answerList),
                createInlineKeyboardMarkup(answerList),
                nextQuestion
        );
    }

    private void replyToAnswer(long chatId, User user, QuestionDto currentQuestion) {
        botState.removeTimerThread(chatId);

        var questions = botState.getQuestions(chatId);
        var nextQuestion = getQuestion(questions, currentQuestion.questionNumber() + 1);

        reportService.createAndSaveReport(user.getId(), currentQuestion.id(), null);

        if (Objects.isNull(nextQuestion)) {
            stopChat(chatId, END_TEXT);
            return;
        }

        var answerList = answerService.findByQuestionId(nextQuestion.id());

        promptWithKeyboardForStateAndEditMessageAndTimer(
                chatId,
                botState.getTelegramMessage(chatId),
                QuestionBuilder.build(nextQuestion, answerList),
                createInlineKeyboardMarkup(answerList),
                nextQuestion
        );
    }

    private InlineKeyboardMarkup createInlineKeyboardMarkup(List<AnswerDto> answerList) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        answerList.forEach(
                a -> {
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();

                    InlineKeyboardButton inlineKeyboardButton =
                            new InlineKeyboardButton(String.valueOf(a.answerNumber()));
                    inlineKeyboardButton.setCallbackData(a.id().toString());
                    rowInline.add(inlineKeyboardButton);

                    rowsInline.add(rowInline);
                }
        );

        markupInline.setKeyboard(rowsInline);

        return markupInline;
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


    private void replyToButtons(long chatId, User user, String data) {
        if (data.equalsIgnoreCase("/stop")) {
            stopChat(chatId, STOP_TEXT);
        }

        switch (botState.getChatState(chatId)) {
            case AWAITING_NAME -> replyToName(chatId, user, data);
            case AWAITING_SELECT_SPECIALIZATION -> replyToSelectSpecialization(chatId, data);
            case AWAITING_START -> replyToStartTest(chatId);
            case AWAITING_ANSWER -> replyToAnswer(chatId, user, data);
            default -> unexpectedMessage(chatId);
        }
    }

    private void replyToCallBackQuery(long chatId, Update upd) {
        String data = upd.getCallbackQuery().getData();

        if (StringUtils.hasText(data) && !data.equals(NO)) {
            replyToButtons(chatId, upd.getCallbackQuery().getFrom(), data);
        } else if (data.equals(NO)) {
            stopChat(chatId, EXIT_TEXT);
        } else {
            stopChat(chatId, STOP_TEXT);
        }
    }

    private void unexpectedMessage(long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(UNEXPECTED_TEXT);
        sender.execute(sendMessage);
    }

    private void stopChat(long chatId, String text) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sender.execute(sendMessage);

        botState.clearState(chatId);
    }

    @Override
    public long creatorId() {
        return 942310899192L;
    }
}
