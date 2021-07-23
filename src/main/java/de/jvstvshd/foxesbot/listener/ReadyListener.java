package de.jvstvshd.foxesbot.listener;

import de.jvstvshd.foxesbot.FoxesBot;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public class ReadyListener implements EventListener {

    private final FoxesBot bot;

    public ReadyListener(FoxesBot bot) {
        this.bot = bot;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof GuildReadyEvent readyEvent) {
            bot.getEventManager().setLogChannel(bot.getConfiguration().getConfigFile().getEventSettings().getGuildChannels());
        }
    }
}