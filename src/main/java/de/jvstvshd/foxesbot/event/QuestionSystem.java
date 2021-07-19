package de.jvstvshd.foxesbot.event;

import com.google.common.collect.Lists;
import de.chojo.jdautil.localization.Localizer;
import de.jvstvshd.foxesbot.utils.EnglishNumberToWords;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.List;

public class QuestionSystem {

    private final Localizer localizer;

    public QuestionSystem(Localizer localizer) {
        this.localizer = localizer;
    }

    public List<String> getQuestions(String key) {
        int questionSize = Integer.parseInt(localizer.localize("question." + key + ".size"));
        if (questionSize == 0)
            return Collections.emptyList();
        List<String> questions = Lists.newArrayList();
        String questionPath = "";
        for (int i = 1; i <= questionSize; i++) {
            questionPath = "question." + key + "." + EnglishNumberToWords.convert(i);
            final String question = localizer.localize(questionPath);
            if (question.equalsIgnoreCase(questionPath)) {
                questions.add("Bitte melde dich im Support!");
                LogManager.getLogger(QuestionSystem.class).warn("Cannot find question for " + questionPath);
                continue;
            }
            questions.add(question);
        }
        return questions;
    }
}
