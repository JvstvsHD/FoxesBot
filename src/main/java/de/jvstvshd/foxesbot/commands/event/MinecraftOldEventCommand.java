package de.jvstvshd.foxesbot.commands.event;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.jvstvshd.foxesbot.FoxesBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class MinecraftEventCommand extends SimpleCommand {

    private final FoxesBot bot;

    public MinecraftEventCommand(FoxesBot bot) {
        super("minecraft", null, "command.event.minecraft", (SimpleArgument[]) null, Permission.UNKNOWN);
        this.bot = bot;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply(startEvent(eventWrapper.getAuthor())).queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        event.replyEmbeds(startEvent(event.getUser())).queue();
    }

    private MessageEmbed startEvent(User user) {
        bot.getEventManager().newEventSession(user, "minecraft");
        return new EmbedBuilder()
                .setDescription("Starte Konversation...")
                .build();
    }
}
