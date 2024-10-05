package ru.gpb.jpixelscoringbot.component;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gpb.jpixelscoringbot.dto.QuestionDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JPixelScoringBotState {

    private final Map<Long, UserState> chatStates;

    private final Map<Long, User> userMap = new HashMap<>();
    private final Map<Long, Thread> timerMap = new HashMap<>();
    private final Map<Long, Message> messageMap = new HashMap<>();
    private final Map<Long, List<QuestionDto>> questionsMap = new HashMap<>();

    public JPixelScoringBotState(Map<Long, UserState> chatStates) {
        this.chatStates = chatStates;
    }

    public UserState putChatState(Long chatId, UserState userState) {
        return this.chatStates.put(chatId, userState);
    }

    public UserState getChatState(Long chatId) {
        return this.chatStates.get(chatId);
    }

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }

    public User putTelegramUser(Long chatId, User user) {
        return this.userMap.put(chatId, user);
    }

    public User getTelegramUser(Long chatId) {
        return this.userMap.get(chatId);
    }

    public Thread putTimerThread(Long chatId, Thread thread) {
        return this.timerMap.put(chatId, thread);
    }

    public Thread getTimerThread(Long chatId) {
        return this.timerMap.get(chatId);
    }

    public void cancelTimerIfWorking(Long chatId) {
        Thread timerExecutor = getTimerThread(chatId);
        if (Objects.nonNull(timerExecutor) && timerExecutor.isAlive()) {
            timerExecutor.interrupt();
            this.timerMap.remove(chatId);
        }
    }

    public void removeTimerThread(Long chatId) {
        this.timerMap.remove(chatId);
    }

    public Message putTelegramMessage(Long chatId, Message message) {
        return this.messageMap.put(chatId, message);
    }

    public Message getTelegramMessage(Long chatId) {
        return this.messageMap.get(chatId);
    }

    public List<QuestionDto> putQuestions(Long chatId, List<QuestionDto> questions) {
        return this.questionsMap.put(chatId, questions);
    }

    public List<QuestionDto> getQuestions(Long chatId) {
        return this.questionsMap.get(chatId);
    }

    public void clearState(Long chatId) {
        this.userMap.remove(chatId);
        this.timerMap.remove(chatId);
        this.messageMap.remove(chatId);
        this.chatStates.remove(chatId);
        this.questionsMap.remove(chatId);
    }
}
