package de.jvstvshd.foxesbot.event;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.apache.logging.log4j.LogManager.getLogger;

public class QuestionSystem {

    private final static Logger logger = getLogger(QuestionSystem.class);
    private final InputStream is;
    private final Properties properties;

    private QuestionSystem(InputStream is) {
        this.is = is;
        this.properties = new Properties();
    }

    public void init() throws IOException {
        this.properties.load(is);
    }

    public static Optional<QuestionSystem> loadQuestions(InputStream is) {
        QuestionSystem q = new QuestionSystem(is);
        try {
            q.init();
        } catch (IOException exception) {
            logger.error("Could not load questions: " + exception);
            return Optional.empty();
        }
        return Optional.of(q);
    }

    public List<String> getQuestions(String key) {
        int questionSize = Integer.parseInt((String) properties.get("question." + key + ".size"));
        if (questionSize == 0)
            return Collections.emptyList();
        List<String> questions = Lists.newArrayList();
        for (int i = 1; i <= questionSize; i++) {
            questions.add(properties.getProperty("question." + key + "." + i, "Bitte melde dich im Support"));
        }
        return questions;
    }
}
