package ru.markovav.excursionbot.models;

import lombok.Data;

import java.util.List;

@Data
public class CreateRoute {
    private String name;
    private String welcomeMessage;
    private List<Task> tasks;

    @Data
    public static class Task {
        private String name;
        private String location;
        private String text;
        private List<AnswerVariant> answerVariants;
        private List<Hint> hints;
    }

    @Data
    public static class AnswerVariant {
        private String text;
        private boolean isCorrect;
    }

    @Data
    public static class Hint {
        private String text;
    }
}
