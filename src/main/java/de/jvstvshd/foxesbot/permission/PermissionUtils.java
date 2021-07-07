package de.jvstvshd.foxesbot.permission;

import net.dv8tion.jda.api.entities.Member;

import java.util.Arrays;
import java.util.List;

public class PermissionUtils {

    private static final List<Long> specialAccessUsers = Arrays.asList(595616992132595712L, 653681927706116106L, 591674741782216717L);

    public static boolean isSpecialPermitted(Member member) {
        return member != null && specialAccessUsers.contains(member.getIdLong());
    }
}
