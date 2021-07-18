package de.jvstvshd.foxesbot.event;

import com.google.common.collect.Maps;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.jvstvshd.foxesbot.FoxesBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class EventSession {

    private final List<String> questions;
    private final Map<Integer, String> answers = Maps.newHashMap();
    private PrivateChannel channel;
    private boolean ended;
    private Message logMessage;
    private final EmbedBuilder logBuilder = new EmbedBuilder();
    private final FoxesBot bot;
    private final int questionNumber;
    private final Type type;
    private long sessionClosed;
    private boolean wasSessionClosed;
    private long lastEdited;
    private String currentQuestion;

    public EventSession(List<String> questions, FoxesBot bot, Type type) {
        this.questions = questions;
        this.bot = bot;
        this.ended = false;
        this.questionNumber = questions.size();
        this.type = type;
    }

    public void start(User user, Message logMessage) {
        user.openPrivateChannel().flatMap(privateChannel -> {
            this.lastEdited = System.currentTimeMillis();
            this.channel = privateChannel;
            this.logMessage = logMessage;
            bot.getScheduler().delayAsync(this::ask, 1L, TimeUnit.SECONDS);
            return channel.sendMessage(bot.getLocalizer().localize("event.school.introduction", Replacement.create("SUBJECT", type.getName(bot.getLocalizer()))));
        }).queue(message -> setBuilderData(State.NOT_CONFIRMED));

    }

    public void ask() {
        if (isEnded())
            return;
        int number = ThreadLocalRandom.current().nextInt(questions.size());
        String question = currentQuestion = questions.get(number);
        channel.sendMessage((answers.size() + 1) + ". " + question).queue(message -> questions.remove(question));
    }

    public void answer(Message message) {
        this.lastEdited = System.currentTimeMillis();
        this.answers.put(answers.size() + 1, message.getContentRaw());
        logBuilder.appendDescription("**" + answers.size() + "." + currentQuestion + "**\n" + message.getContentRaw() + "\n");
        setBuilderData(State.RUNNING);
        if (answers.size() == questionNumber) {
            this.sessionClosed = System.currentTimeMillis();
            setBuilderData(State.SUCCESS);
            channel.sendMessage(bot.getLocalizer().localize("event.school.survey.end")).queue();
            return;
        }
        ask();
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
        if (ended) {

            this.wasSessionClosed = true;
        }
    }

    public boolean isInactive() {
        if (isEnded()) {
            return true;
        }
        return timedOut();
    }

    public boolean timedOut() {
        if (logMessage == null) {
            bot.getLogger().debug(logMessage);
            return false;
        }
        return System.currentTimeMillis()-lastEdited >= bot.getConfiguration().getConfigFile().getEventSettings().getEventSessionTimeout();
    }

    public void setBuilderData(State state) {
        logMessage.editMessageEmbeds(
                logBuilder.setTitle("Befragung – Status: " + state.getName(bot.getLocalizer())).setTimestamp(Instant.now()).setColor(state.getColor()).build()).queue();
        if (state == State.RUNNING)
            wasSessionClosed = true;
        if (state == State.RUNNING || state == State.NOT_CONFIRMED)
            return;
        setEnded(true);
    }

    public enum State {
        NOT_CONFIRMED("notconfirmed", Color.ORANGE),
        RUNNING("running", Color.YELLOW),
        SUCCESS("success", Color.GREEN),
        TIMEOUT("timeout", Color.RED);

        private final String key;
        private final Color color;

        State(String key, Color color) {
            this.key = key;
            this.color = color;
        }

        public String getKey() {
            return key;
        }

        public String getName(Localizer localizer) {
            return localizer.localize("event.school.survey.state." + getKey()
            );
        }

        public Color getColor() {
            return color;
        }
    }

    public enum Type {
        GOMME("gomme"),
        HYPIXEL("hypixel"),
        MINECRAFT_NEW("minecraft.new"),
        UNKNOWN("unknown"),
        MINECRAFT_OLD("minecraft.old");

        private final String key;

        Type(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
        public String getName(Localizer localizer) {
            return localizer.localize("event.school.type." + key);
        }

        public static Optional<Type> getType(String key) {
            for (Type value : values()) {
                if (value.getKey().equalsIgnoreCase(key))
                    return Optional.of(value);
            }
            return Optional.empty();
        }
    }

    public long getSessionClosed() {
        return sessionClosed;
    }

    public boolean shouldBeRemoved() {
        long difference = System.currentTimeMillis() - getSessionClosed();
        boolean shouldRemove = difference > bot.getConfiguration().getConfigFile().getEventSettings().getEventSessionCooldown();
        return shouldRemove;
    }

    public boolean wasSessionClosed() {
        return wasSessionClosed;
    }

    public void setSessionClosed(long sessionClosed) {
        this.sessionClosed = sessionClosed;
    }
}
