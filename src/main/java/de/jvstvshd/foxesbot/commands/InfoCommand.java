package de.jvstvshd.foxesbot.commands;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.jvstvshd.foxesbot.FoxesBot;
import de.jvstvshd.foxesbot.utils.UserUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.time.Duration;

public class InfoCommand extends SimpleCommand {

    private final FoxesBot bot;

    public InfoCommand(FoxesBot bot) {
        super("info", new String[]{"information", "infos"}, "command.info.description", (SimpleArgument[]) null, Permission.UNKNOWN);
        this.bot = bot;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply(build(eventWrapper.getMember())).queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        event.replyEmbeds(build(event.getMember())).queue();
    }

    private MessageEmbed build(Member member) {
        MessageEmbed message;
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder uptimeBuilder = new StringBuilder();
        Duration duration = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
        uptimeBuilder.append(duration.toDaysPart())
                .append(":")
                .append(format(duration.toHoursPart()))
                .append(":")
                .append(format(duration.toMinutesPart()))
                .append(":")
                .append(format(duration.toSecondsPart()));
        String uptime = UserUtils.isPermitted(member, Permission.ADMINISTRATOR) ? "Uptime: " + uptimeBuilder + "\n" : "";
        long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        DecimalFormat decimalFormat = new DecimalFormat("####,##");
        String memory = UserUtils.isPermitted(member, Permission.ADMINISTRATOR) ? "Memory usage: " + decimalFormat.format((double) memoryUsage/(1000.0*1000.0)) + "MB/" + decimalFormat.format(Runtime.getRuntime().maxMemory()/1000000.0) + "MB\n" : "";
        builder
                .setTitle("FoxesBot", "https://github.com/JvstvsHD/FoxesBot")
                .setAuthor("FoxesBot", "https://github.com/JvstvsHD/FoxesBot")
                .appendDescription("__**FoxesBot**__\n")
                .appendDescription(bot.getLocalizer().localize("command.info.help") + "\n")
                .appendDescription(bot.getLocalizer().localize("command.info.issues") + "\n")
                .appendDescription(uptime)
                .appendDescription(memory);
        message = builder.build();
        return message;
    }

    private String format(int number) {
        String s = String.valueOf(number);
        if (s.length() <= 1)
            s = "0" + s;
        return s;
    }
}
