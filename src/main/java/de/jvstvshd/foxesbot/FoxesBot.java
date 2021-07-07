package de.jvstvshd.foxesbot;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.command.dispatching.CommandHub;
import de.jvstvshd.foxesbot.commands.ExitCommand;
import de.jvstvshd.foxesbot.commands.HelpCommand;
import de.jvstvshd.foxesbot.commands.InfoCommand;
import de.jvstvshd.foxesbot.commands.StatusCommand;
import de.jvstvshd.foxesbot.commands.moderation.BanCommand;
import de.jvstvshd.foxesbot.config.ConfigException;
import de.jvstvshd.foxesbot.config.Configuration;
import de.jvstvshd.foxesbot.scheduling.FoxesScheduler;
import de.jvstvshd.foxesbot.scheduling.Scheduler;
import de.jvstvshd.foxesbot.utils.Logging;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.client.GitHubClient;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class FoxesBot {

    private final static Logger logger = LogManager.getLogger();
    private ShardManager shardManager;
    private final String token;
    private final Scheduler scheduler;
    private final GitHubClient githubClient;
    private Configuration configuration;
    private CommandHub<SimpleCommand> commandHub;

    public FoxesBot(String token) {
        this.token = token;
        this.scheduler = new FoxesScheduler(this);
        this.githubClient = new GitHubClient();
    }

    private void init() throws Exception {
        Logging logging = new Logging(getLogger(), this);
        logging.init();
        loadConfiguration();
        initBot(getToken());
        initShutdownHook();
    }

    protected void start() throws Exception {
        init();
    }

    private void loadConfiguration() {
        this.configuration = Configuration.create();
        this.githubClient.setOAuth2Token(configuration.getConfigFile().getBaseSettings().getGithubToken());
    }

    private void initBot(String token) throws LoginException {
        this.shardManager = DefaultShardManagerBuilder.createDefault(token)
                .setEnableShutdownHook(false)
                .setActivity(Activity.of(Activity.ActivityType.WATCHING, " at your personal information."))
                .build();

        var command = CommandHub.builder(shardManager, configuration.getConfigFile().getBaseSettings().getCommandChar())
                .receiveGuildCommands()
                .receiveGuildMessagesUpdates()
                .withConversationSystem()
                .withSlashCommands()
                .withCommands(
                        new StatusCommand(this),
                        new ExitCommand(this),
                        new BanCommand(),
                        new HelpCommand(this),
                        new InfoCommand()
                );
        this.commandHub = command.build();
    }

    public String getToken() {
        return token;
    }

    public void shutdown() {
        logger.info("-----------------");
        logger.info("Shutting down bot....");
        logger.info("-----------------");
        System.exit(0);
    }

    private void initShutdownHook() {
        var shutdown = new Thread(() -> {
            shardManager.shutdown();
            scheduler.runAsync(() -> {
                try {
                    scheduler.shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Runtime.getRuntime().halt(0);
            });
            LogManager.shutdown();
            try {
                configuration.save();
            } catch (IOException exception) {
                throw new ConfigException("Could not save config", exception);
            }

        });
        Runtime.getRuntime().addShutdownHook(shutdown);
    }

    public Logger getLogger() {
        return logger;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public GitHubClient getGithubClient() {
        return githubClient;
    }

    public CommandHub<SimpleCommand> getCommandHub() {
        return commandHub;
    }
}
