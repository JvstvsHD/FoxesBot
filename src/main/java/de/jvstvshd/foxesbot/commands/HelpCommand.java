package de.jvstvshd.foxesbot.commands;

import com.google.common.collect.Sets;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.jvstvshd.foxesbot.FoxesBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HelpCommand extends SimpleCommand {

    private final FoxesBot bot;


    public HelpCommand(FoxesBot bot) {
        super("help", new String[]{"hilfe"}, "Command to show all commands of this bot.", argsBuilder()
                .add(OptionType.INTEGER, "page", "page of commands to show", false)
                .build(), Permission.UNKNOWN);
        this.bot = bot;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        int page = context.argInt(0).orElse(1);
        eventWrapper.reply(getHelp(page)).queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var page = event.getOption("page");
        var val = page == null ? 1 : page.getAsLong();
        event.replyEmbeds(getHelp((int) val)).queue();
    }

    private MessageEmbed getHelp(int page) {
        int entriesPerPage = bot.getConfiguration().getConfigFile().getBaseSettings().getHelpEntriesPerPage();
        StringBuilder helpBuilder = new StringBuilder();
        for (SimpleCommand command : getCommands(entriesPerPage, page)) {
            helpBuilder.append(command.command()).append(": ").append(command.description()).append("\n");
        }
        return new EmbedBuilder()
                .setTitle("FoxesBot: Hilfe (Seite " + page + "/" + getMaxPages(entriesPerPage) + ")")
                .appendDescription(helpBuilder.toString())
                .setTimestamp(Instant.now())
                .build();

    }

    private Set<SimpleCommand> getCommands(int entries, int page) {
        List<SimpleCommand> simpleCommands = new ArrayList<>(bot.getCommandHub().getCommands());
        if (entries > simpleCommands.size())
            return new HashSet<>(simpleCommands);
        Set<SimpleCommand> commands = Sets.newHashSet();
        int start = (page-1)*entries;
        for (int i = start; i < page*entries; i++) {
            commands.add(simpleCommands.get(i));
        }
        return commands;
    }

    private int getMaxPages(int entries) {
        int size = new ArrayList<>(bot.getCommandHub().getCommands()).size();
        int modulo = size % entries;
        if (modulo == 0)
            return size / entries;
        return ((size - modulo) / entries) + 1;

    }
}
