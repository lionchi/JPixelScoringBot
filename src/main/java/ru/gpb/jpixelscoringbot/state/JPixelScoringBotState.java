package ru.gpb.jpixelscoringbot.state;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gpb.jpixelscoringbot.dto.QuestionDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class JPixelScoringBotState {

    private final Map<Long, JPixelScoringBotStateRecord> stateMap = new ConcurrentHashMap<>();

    public JPixelScoringBotState(Map<Long, UserState> chatStates) {
        chatStates.forEach((aLong, userState) -> this.stateMap.put(aLong, new JPixelScoringBotStateRecord(userState)));
    }

    public void putChatState(Long chatId, UserState userState) {
        Optional.ofNullable(this.stateMap.get(chatId))
                .ifPresentOrElse(stateRecord -> stateRecord.setUserState(userState),
                        () -> this.stateMap.put(chatId, new JPixelScoringBotStateRecord(userState)));
    }

    public UserState getChatState(Long chatId) {
        return Optional.ofNullable(this.stateMap.get(chatId))
                .map(JPixelScoringBotStateRecord::getUserState).orElse(null);
    }

    public boolean userIsActive(Long chatId) {
        return stateMap.containsKey(chatId);
    }

    public void putTelegramUser(Long chatId, User user) {
        Optional.ofNullable(this.stateMap.get(chatId))
                .ifPresentOrElse(stateRecord -> stateRecord.setUser(user),
                        () -> this.stateMap.put(chatId, new JPixelScoringBotStateRecord(user)));
    }

    public User getTelegramUser(Long chatId) {
        return Optional.ofNullable(this.stateMap.get(chatId))
                .map(JPixelScoringBotStateRecord::getUser).orElse(null);
    }

    public void putTimerThread(Long chatId, Thread thread) {
        var stateRecord = this.stateMap.get(chatId);
        stateRecord.setThread(thread);
    }

    public Thread getTimerThread(Long chatId) {
        return Optional.ofNullable(this.stateMap.get(chatId))
                .map(JPixelScoringBotStateRecord::getThread).orElse(null);
    }

    public void cancelTimerIfWorking(Long chatId) {
        Optional.ofNullable(getTimerThread(chatId))
                .filter(Thread::isAlive)
                .ifPresent(timerExecutor -> {
                    timerExecutor.interrupt();
                    removeTimerThread(chatId);
                });
    }

    public void removeTimerThread(Long chatId) {
        var stateRecord = this.stateMap.get(chatId);
        stateRecord.setThread(null);
    }

    public void putTelegramMessage(Long chatId, Message message) {
        var stateRecord = this.stateMap.get(chatId);
        stateRecord.setTextMessage(message);
    }

    public Message getTelegramMessage(Long chatId) {
        return Optional.ofNullable(this.stateMap.get(chatId))
                .map(JPixelScoringBotStateRecord::getTextMessage).orElse(null);
    }

    public void putPhotoMessage(Long chatId, Message message) {
        var stateRecord = this.stateMap.get(chatId);
        stateRecord.setPhotoMessage(message);
    }

    public Message getPhotoMessage(Long chatId) {
        return Optional.ofNullable(this.stateMap.get(chatId))
                .map(JPixelScoringBotStateRecord::getPhotoMessage).orElse(null);
    }

    public void removePhotoMessage(Long chatId) {
        var stateRecord = this.stateMap.get(chatId);
        stateRecord.setPhotoMessage(null);
    }

    public int getAndIncrementNumberQuestion(Long chatId) {
        return Optional.ofNullable(this.stateMap.get(chatId))
                .map(JPixelScoringBotStateRecord::getAndIncrementCounterNumberQuestion).orElse(1);
    }

    public void putQuestions(Long chatId, List<QuestionDto> questions) {
        var stateRecord = this.stateMap.get(chatId);
        stateRecord.setQuestions(questions);
    }

    public List<QuestionDto> getQuestions(Long chatId) {
        return Optional.ofNullable(this.stateMap.get(chatId))
                .map(JPixelScoringBotStateRecord::getQuestions).orElse(null);
    }

    public QuestionDto getQuestion(Long chatId) {
        return Optional.ofNullable(this.stateMap.get(chatId))
                .map(JPixelScoringBotStateRecord::getQuestion).orElse(null);
    }

    public void clearState(Long chatId) {
        cancelTimerIfWorking(chatId);
        this.stateMap.remove(chatId);
    }
}
