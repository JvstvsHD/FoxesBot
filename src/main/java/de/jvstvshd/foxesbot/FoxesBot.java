package de.jvstvshd.foxesbot;

import com.google.common.collect.Maps;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Language;
import de.jvstvshd.foxesbot.commands.ExitCommand;
import de.jvstvshd.foxesbot.commands.HelpCommand;
import de.jvstvshd.foxesbot.commands.InfoCommand;
import de.jvstvshd.foxesbot.commands.StatusCommand;
import de.jvstvshd.foxesbot.commands.event.SimpleEventCommand;
import de.jvstvshd.foxesbot.commands.moderation.BanCommand;
import de.jvstvshd.foxesbot.config.Configuration;
import de.jvstvshd.foxesbot.event.EventManager;
import de.jvstvshd.foxesbot.listener.EventMessageListener;
import de.jvstvshd.foxesbot.listener.ReadyListener;
import de.jvstvshd.foxesbot.scheduling.FoxesScheduler;
import de.jvstvshd.foxesbot.scheduling.Scheduler;
import de.jvstvshd.foxesbot.utils.Logging;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.client.GitHubClient;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class FoxesBot {

    private final static Logger logger = LogManager.getLogger();
    private ShardManager shardManager;
    private String token;
    private final Scheduler scheduler;
    private final GitHubClient githubClient;
    private Configuration configuration;
    private CommandHub<SimpleCommand> commandHub;
    private Localizer localizer;
    private final EventManager eventManager;

    public FoxesBot() {
        this.scheduler = new FoxesScheduler(this);
        this.githubClient = new GitHubClient();
        this.eventManager = EventManager.build(this);
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
        this.token = configuration.getConfigFile().getBaseSettings().getToken();
    }

    private void initBot(String token) throws LoginException {
        this.shardManager = DefaultShardManagerBuilder.createDefault(token)
                .addEventListeners(new EventMessageListener(this), new ReadyListener(this))
                .setEnableShutdownHook(false)
                .setActivity(Activity.playing("c?help"))
                .build();
        Map<Language, ResourceBundle> resourceBundleMap;
        try {
            resourceBundleMap = new HashMap<>() {
                {
                    put(Language.GERMAN, getResourceBundle("de_DE.properties"));
                    put(Language.ENGLISH, getResourceBundle("en_US.properties"));
                }
            };
        } catch (Exception exception) {
            exception.printStackTrace();
            resourceBundleMap = Maps.newHashMap();
        }

        Function<Guild, Optional<String>> function = guild -> Optional.of("en_US");
        this.localizer = new Localizer(resourceBundleMap, function, Language.GERMAN);
        var command = CommandHub.builder(shardManager, configuration.getConfigFile().getBaseSettings().getCommandChar())
                .receiveGuildCommands()
                .receiveGuildMessagesUpdates()
                .withConversationSystem()
                .receiveGuildCommands()
                .withTextCommands()
                .withSlashCommands()
                .withLocalizer(localizer)
                .withCommands(
                        new StatusCommand(this),
                        new ExitCommand(this),
                        new BanCommand(),
                        new HelpCommand(this),
                        new InfoCommand(this),
                        new SimpleEventCommand(this)
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down shard manager...");
            shardManager.shutdown();
            logger.info("Shutting down scheduler...");
            getScheduler().runAsync(() -> {
                try {
                    scheduler.shutdown();
                } catch (InterruptedException e) {
                    logger.error("Could not shutdown scheduler", e);
                }
            });

            logger.info("Saving config...");
            try {
                configuration.save();
            } catch (IOException exception) {
                logger.error("Could not save configuration", exception);
            }
            logger.info("Shutting down LogManager");
            LogManager.shutdown();
        }));
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

    private ResourceBundle getResourceBundle(String name) {
        try {
            return new PropertyResourceBundle(Objects.requireNonNull(FoxesBot.class.getResourceAsStream("/" + name), name + " could not be found."));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public Localizer getLocalizer() {
        return localizer;
    }

    public EventManager getEventManager() {
        return eventManager;
    }
}
