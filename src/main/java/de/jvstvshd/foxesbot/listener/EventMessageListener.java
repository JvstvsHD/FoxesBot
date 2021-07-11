package de.jvstvshd.foxesbot.listener;

import de.jvstvshd.foxesbot.FoxesBot;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public class MessageListener implements EventListener {

    private final FoxesBot bot;

    public MessageListener(FoxesBot bot) {
        this.bot = bot;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (!(event instanceof PrivateMessageReceivedEvent))
            return;
        
    }

}
