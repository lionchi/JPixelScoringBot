package ru.gpb.jpixelscoringbot.component;

import ru.gpb.jpixelscoringbot.dto.AnswerDto;
import ru.gpb.jpixelscoringbot.dto.QuestionDto;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

public final class QuestionBuilder {

    private static final String DOT = ".";
    private static final String SEMICOLON = ";";
    private static final String LINE_BREAK = "\n";
    private static final String TEMPLATE = "{0}) {1}";
    private static final String ANSWER_OPTIONS = "Варианты ответов:";
    private static final String TEMPLATE_REMAINING_TIME = "Оставшиеся время: {0} cек.";
    private static final String TEMPLATE_TIME = "{0} cек.";

    private QuestionBuilder() {

    }

    public static String build(QuestionDto question, List<AnswerDto> answerList) {
        StringBuilder sb = new StringBuilder(
                MessageFormat.format(TEMPLATE, question.questionNumber(), question.question()));

        sb.append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append(LINE_BREAK);

        sb.append(ANSWER_OPTIONS);
        sb.append(LINE_BREAK);

        Iterator<AnswerDto> iterator = answerList.iterator();
        while (iterator.hasNext()) {
            var answer = iterator.next();
            sb.append(MessageFormat.format(TEMPLATE, answer.answerNumber(), answer.answer()));

            if (iterator.hasNext()) {
                sb.append(SEMICOLON);
                sb.append(LINE_BREAK);
                sb.append(LINE_BREAK);
            } else {
                sb.append(DOT);
            }
        }

        sb.append(insertTimerIntoText(question.timeInSeconds()));

        return sb.toString();
    }

    private static String insertTimerIntoText(Integer timer) {
        return LINE_BREAK + LINE_BREAK + MessageFormat.format(TEMPLATE_REMAINING_TIME, timer);
    }

    public static String replaceSeconds(String originText, Integer timer) {
        return originText.replaceFirst("\\d{0,2}\\scек\\.", MessageFormat.format(TEMPLATE_TIME, timer));
    }
}
