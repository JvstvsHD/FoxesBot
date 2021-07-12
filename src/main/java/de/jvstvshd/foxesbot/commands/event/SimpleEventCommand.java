package de.jvstvshd.foxesbot.commands.event;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.jvstvshd.foxesbot.FoxesBot;
import de.jvstvshd.foxesbot.event.EventSession;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SimpleEventCommand extends SimpleCommand {

    private final FoxesBot bot;

    public SimpleEventCommand(FoxesBot bot) {
        super("school", new String[]{}, "command.event.mcschool.description",
                argsBuilder().add(OptionType.STRING, "type", "command.event.mcschool.type", true).build(),
                Permission.UNKNOWN);
        this.bot = bot;
    }

    protected final String startEvent(User user, String typeString, Guild guild) {
        if (typeString.equalsIgnoreCase("help")) {
            return getHelp();
        }
        EventSession.Type type = EventSession.Type.getType(typeString).orElse(EventSession.Type.UNKNOWN);
        if (type == EventSession.Type.UNKNOWN)
            return "Bitte benutze eines der folgenden Argumente: gomme, hypixel, minecraft.old, minecraft.new\nBenutze /school help für weiter Hilfe";
        String lastPart = bot.getEventManager().newEventSession(user, type, guild) ? "success" : "failure";
        return bot.getLocalizer().localize("event.school.begin." + lastPart);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        String type = context.argString(0).orElse("unknown");
        eventWrapper.reply(startEvent(eventWrapper.getAuthor(), type, eventWrapper.getGuild())).queue();;
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        String type;
        OptionMapping option = event.getOption("type");
        if (option == null)
            type = "unknown";
        else
            type = option.getAsString();
        event.reply(startEvent(event.getUser(), type, event.getGuild())).queue();
    }

    private String getHelp() {
        return """
                **MC-Schule: Hilfe**
                */school <gomme, hypixel, minecraft.new, minecraft.old>*
                **gomme:** Test zum GommeHD.net Server
                **hypixel:** Test über den Hypixel Server
                **minecraft.new:** Test über die Minecraft Versionen 1.16+
                **minecraft.old:** Test über die Minecraft Version 1.8""";
    }
}
