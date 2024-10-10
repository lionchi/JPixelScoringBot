package ru.gpb.jpixelscoringbot.state;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gpb.jpixelscoringbot.dto.QuestionDto;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class JPixelScoringBotStateRecord {

    private User user;
    private Thread thread;
    private Message textMessage;
    private UserState userState;
    private Message photoMessage;
    private int counterNumberQuestion = 1;
    private List<QuestionDto> questions;

    public JPixelScoringBotStateRecord(User user) {
        this.user = user;
    }

    public JPixelScoringBotStateRecord(UserState userState) {
        this.userState = userState;
    }

    public int getAndIncrementCounterNumberQuestion() {
        return counterNumberQuestion++;
    }

    public QuestionDto getQuestion() {
        if (CollectionUtils.isEmpty(questions) || counterNumberQuestion > questions.size()) {
            return null;
        }

        var index = counterNumberQuestion - 1;

        return Optional.of(questions)
                .map(list -> list.get(index))
                .orElse(null);
    }
}
