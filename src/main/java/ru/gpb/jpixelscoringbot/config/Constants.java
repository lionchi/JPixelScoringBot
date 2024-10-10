package ru.gpb.jpixelscoringbot.config;

public final class Constants {

    public static final String CHAT_STATES = "chatStates";

    public static final String YES = "Да";
    public static final String NO = "Нет";
    public static final String START_DESCRIPTION = "Запускает бота";
    public static final String RESULT_DESCRIPTION = "Получает результат тестирования";
    public static final String REMOVE_REPORT_DESCRIPTION = "Удаляет результаты тестов пользователя";
    public static final String STOP_DESCRIPTION = "Останавливает бота";
    public static final String START_TEXT = "Добро пожаловать в JPixelScoringBot. Пожалуйста укажи свое ФИО";
    public static final String STOP_TEXT = "Спасибо за уделенное время";
    public static final String REPOST_EXISTS_TEXT = "Ты уже проходил тестирование по выбранной специальности {0}. Результат живет {1} месяца. Обратитесь к HR для более подробной информации";
    public static final String EXIT_TEXT = "Жаль. Приходи, я буду ждать тебя снова";
    public static final String END_TEXT = "Спасибо что прошел все до конца! Нам нужно немного времени чтобы с тобой связаться! HR вернется к тебе с обратной связью";
    public static final String UNEXPECTED_TEXT = "Необходимо выбрать ответ из предоставленных вариантов. Писать ответ в ручную не нужно";
    public static final String ERROR_TEXT = "Упс... что то пошло не так. Приношу свои извинения";
    public static final String HELLO_TEXT = "Привет, {0}. Выбери пожалуйста специальность, по которой хочешь пройти тест";
    public static final String START_TEST_TEXT = "Супер, вы выбрали {0}. Тебя будет ждать 3 вопроса и у каждого вопроса будет свой таймер c шагом 5 секунд. Начнем?";
    public static final String REPORT_EMPTY = "Вы еще не проходили тесты ни по одной теме. Если готов это исправить запусти меня командой /start";
    public static final String NO_ACCESS_RIGHT_TO_EXECUTE_COMMAND = "Недостаточно прав для выполнения команды";
    public static final String INVALID_ARGUMENTS_FOR_REMOVE_REPORT_COMMAND = "Ожидается выполнение команды c двумя обязательными параметрами /removereport ***Логин пользователя*** ***Наименование специальности***";
    public static final String SUCCESS_REMOVE_REPORT = "Результаты тестирования успешно удалены";

    public static final String USER_CACHE_NAME = "users";
    public static final String ANSWER_CACHE_NAME = "answers";
    public static final String QUESTION_CACHE_NAME = "questions";
    public static final String QUESTION_TYPE_CACHE_NAME = "questionTypes";
}
