package de.jvstvshd.foxesbot.utils;

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
}
