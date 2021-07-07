package de.jvstvshd.foxesbot.commands;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.temporal.ChronoUnit;

public class InfoCommand extends SimpleCommand {
    public InfoCommand() {
        super("info", new String[]{"information", "infos"}, "Gibt Informationen zum Bot aus", new SimpleArgument[]{}, Permission.UNKNOWN);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply(new EmbedBuilder().setTitle("FoxesBot").build()).queue(message -> {
            long ping = eventWrapper.getMessage().getTimeCreated().until(message.getTimeCreated(), ChronoUnit.MILLIS);
            message.editMessageEmbeds(new EmbedBuilder().appendDescription("Ping: ").appendDescription("" + ping).appendDescription("ms").build()).queue();
        });
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {

    }
}
