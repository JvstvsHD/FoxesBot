package de.jvstvshd.foxesbot.event;

import com.google.common.collect.Maps;
import de.jvstvshd.foxesbot.FoxesBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EventManager {

    private final Map<User, EventSession> eventSessions = Maps.newHashMap();
    private Map<String, Long> logChannel;
    private final FoxesBot bot;

    private EventManager(FoxesBot bot) {
        this.bot = bot;
    }

    public void init() {
        bot.getScheduler().scheduleAsync(() -> {
            for (Map.Entry<User, EventSession> entry : eventSessions.entrySet()) {
                if (entry.getValue().isInactive()) {
                    if (!entry.getValue().wasSessionClosed()) {
                        entry.getKey().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("Die Befragung wurde aufgrund von Inaktivität beendet.")).queue();
                        entry.getValue().setSessionClosed(System.currentTimeMillis());
                    }
                    entry.getValue().setBuilderData(EventSession.State.TIMEOUT);
                    if (entry.getValue().shouldBeRemoved()) {
                        eventSessions.remove(entry.getKey());
                    }
                    break;
                }
            }
        }, 1L, TimeUnit.SECONDS);

    }

    public boolean isInEventSession(User user) {
        return eventSessions.get(user) != null && !eventSessions.get(user).isEnded();
    }

    public Map<User, EventSession> getEventSessions() {
        return eventSessions;
    }

    public boolean newEventSession(User user, EventSession.Type type, Guild guild) {
        if (isOnCooldown(user))
            return false;
        String questionString = bot.getLocalizer().localize("event.school.question." + type.getKey());
        String[] questions = questionString.split("\\|");
        final TextChannel channel = guild.getTextChannelById(logChannel.get(String.valueOf(guild.getIdLong())));
        if (channel == null)
            throw new RuntimeException(new NullPointerException("The text channel with the id " + logChannel.get(guild.getId()) + " for the guild " + guild.getIdLong() + " could not be found."));
        bot.getScheduler().runAsync(() -> {
            EventSession session = new EventSession(new ArrayList<>(Arrays.asList(questions)), bot, type);
            eventSessions.put(user, session);
            channel.sendMessage("Antworten der Befragung zu " + user.getAsMention()).queue(message -> session.start(user, message));
        });
        return true;
    }

    public void setLogChannel(Map<String, Long> logChannel) {
        this.logChannel = logChannel;
    }

    public static EventManager build(FoxesBot bot) {
        EventManager eventManager = new EventManager(bot);
        eventManager.init();
        return eventManager;
    }

    public boolean isOnCooldown(User user) {
        if (eventSessions.containsKey(user))
            return true;
        return System.currentTimeMillis() - bot.getStartTime() >= bot.getConfiguration().getConfigFile().getEventSettings().getEventSessionCooldown();
    }
}
