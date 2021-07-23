package de.jvstvshd.foxesbot.commands;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class InviteCommand extends SimpleCommand {

    public InviteCommand() {
        super("invite",
                null,
                "command.invite.description",
                (SimpleArgument[]) null,
                Permission.UNKNOWN
        );
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply("Invite-Link: https://discord.gg/K5rhddJtyW").queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        event.reply("Invite-Link: https://discord.gg/K5rhddJtyW").queue();
    }
}
