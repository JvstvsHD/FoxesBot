package de.jvstvshd.foxesbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.jvstvshd.foxesbot.FoxesBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.Instant;

public class ExitCommand extends SimpleCommand {

    private final FoxesBot bot;

    public ExitCommand(FoxesBot bot) {
        super("exit", new String[]{"shutdown"}, "Shutdowns the bot.",
                argsBuilder().build(), Permission.ADMINISTRATOR);
        this.bot = bot;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.answer(reply()).queue();
        bot.shutdown();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        event.replyEmbeds(reply()).queue();
        bot.shutdown();
    }
    private MessageEmbed reply() {
        return new EmbedBuilder()
                .setTitle("Fahre Bot herunter...")
                .appendDescription("Der Bot wird nun heruntergefahren")
                .setTimestamp(Instant.now())
                .build();
    }
}
