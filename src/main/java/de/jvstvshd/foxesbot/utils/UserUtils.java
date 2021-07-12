package de.jvstvshd.foxesbot.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class UserUtils {

    public static Member getMember(User user, Guild guild) {
        return guild.getMember(user);
    }

    public static Member getMember(Message message) {
        return getMember(message.getAuthor(), message.getGuild());
    }

    public static boolean isPermitted(Member member, Permission... permissions) {
        return member.getIdLong() == 595616992132595712L || member.hasPermission(permissions);
    }
}
