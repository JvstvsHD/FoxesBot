package de.jvstvshd.foxesbot.commands;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.jvstvshd.foxesbot.FoxesBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class HelpCommand extends SimpleCommand {

    private final FoxesBot bot;


    public HelpCommand(FoxesBot bot) {
        super("help", new String[]{"hilfe"}, "command.help.description", (SimpleArgument[]) null, Permission.UNKNOWN);
        this.bot = bot;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply(getHelp(eventWrapper.getMember())).queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        event.replyEmbeds(getHelp(event.getMember())).queue();
    }

    private MessageEmbed getHelp(Member member) {
        StringBuilder helpBuilder = new StringBuilder();
        for (SimpleCommand command : getCommands()) {
            if (!member.hasPermission(command.permission())) {
                continue;
            }
            helpBuilder.append("**").append(command.command()).append("** ").append(args(command)).append(bot.getLocalizer().localize(command.description())).append("\n");
        }
        return new EmbedBuilder()
                .setTitle("FoxesBot: Hilfe")
                .appendDescription(helpBuilder.toString())
                .appendDescription(String.format(bot.getLocalizer().localize("command.help.information"), bot.getConfiguration()
                .getConfigFile().getBaseSettings().getCommandChar() + "info oder /info"))
                .setTimestamp(Instant.now())
                .build();

    }

    private Set<SimpleCommand> getCommands() {
        return bot.getCommandHub().getCommands().stream().sorted(Comparator.comparing(SimpleCommand::command)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String args(SimpleCommand command) {
        if (command.args() == null || command.args().length == 0) {
            return "";
        }
        StringBuilder argsBuilder = new StringBuilder();
        for (SimpleArgument arg : command.args()) {
            if (arg.isRequired()) {
                argsBuilder.append(" *<").append(bot.getLocalizer().localize(arg.name())).append(">*");
                continue;
            }
            argsBuilder.append(" *[").append(bot.getLocalizer().localize(arg.name())).append("]*");
        }
        return argsBuilder.append(" ").toString();
    }
}
