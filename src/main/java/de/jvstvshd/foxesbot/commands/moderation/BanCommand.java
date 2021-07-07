package de.jvstvshd.foxesbot.commands.moderation;

import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class BanCommand extends SimpleModerationCommand {

    public BanCommand() {
        super("ban", new String[]{"permban"}, "Bans the user from the arguments for a specific reason permanent.",
                argsBuilder().add(OptionType.USER, "user", "User to ban")
                .add(OptionType.STRING, "reason", "Reason for the ban").
                        add(OptionType.INTEGER, "del", "Duration of messages sent by the user which should be deleted", false).build(), Permission.BAN_MEMBERS);
    }

    @Override
    public void punish(Member member, String reason) {
        member.ban(1, reason).queue();
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {

    }
}
