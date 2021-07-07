package de.jvstvshd.foxesbot.commands.moderation;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.command.SimpleSubCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleModerationCommand extends SimpleCommand {

    protected SimpleModerationCommand(String command, @Nullable String[] alias, String description, @Nullable SimpleSubCommand[] subCommands, Permission permission) {
        super(command, alias, description, subCommands, permission);
    }

    protected SimpleModerationCommand(String command, @Nullable String[] alias, String description, @Nullable SimpleArgument[] args, Permission permission) {
        super(command, alias, description, args, permission);
    }

    public abstract void punish(Member member, String reason);
}
