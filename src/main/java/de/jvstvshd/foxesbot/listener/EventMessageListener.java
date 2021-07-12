package de.jvstvshd.foxesbot.listener;

import de.jvstvshd.foxesbot.FoxesBot;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public class EventMessageListener implements EventListener {

    private final FoxesBot bot;

    public EventMessageListener(FoxesBot bot) {
        this.bot = bot;
    }

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if (!(genericEvent instanceof PrivateMessageReceivedEvent event))
            return;
        if (bot.getEventManager().isInEventSession(event.getAuthor())) {
            bot.getEventManager().getEventSessions().get(event.getAuthor()).answer(event.getMessage());
        }
    }

}
