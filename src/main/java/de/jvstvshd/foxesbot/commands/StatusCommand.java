package de.jvstvshd.foxesbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.jvstvshd.foxesbot.FoxesBot;
import de.jvstvshd.foxesbot.status.StatusObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.time.Instant;
import java.util.Optional;

public class StatusCommand extends SimpleCommand {

    private final FoxesBot bot;

    public StatusCommand(FoxesBot bot) {
        super("status",
                new String[]{},
                "command.status.description",
                argsBuilder().add(OptionType.STRING, "query", "Name of the object from which the status should be read.", true).build(),
                Permission.UNKNOWN);
        this.bot = bot;
    }


    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        String query = context.argString(0).orElse("null");
        eventWrapper.reply(status(query)).queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var query = event.getOption("query");
        event.replyEmbeds(status(query == null ? "null" : query.getAsString())).queue();
    }

    private MessageEmbed status(String query) {
        Optional<StatusObject> optionalStatusObject = StatusObject.create(query, bot);
        if (optionalStatusObject.isPresent())
            return optionalStatusObject.get().show();
        return new EmbedBuilder()
                .setTitle("Fehler!")
                .setDescription("Der Status für *" + query + "* konnte nicht abgefragt werden: Keine passende URL gefunden.")
                .setTimestamp(Instant.now())
                .build();

    }
}
