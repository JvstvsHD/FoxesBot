package de.jvstvshd.foxesbot.status;

import de.jvstvshd.foxesbot.FoxesBot;
import de.jvstvshd.foxesbot.config.elements.StatusSettings;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.IOException;
import java.util.Optional;

public abstract class StatusObject {

    private final FoxesBot bot;
    private final String title;
    private final String url;

    public StatusObject(FoxesBot bot, String title, String url) {
        this.bot = bot;
        this.title = title;
        this.url = url;
    }

    public FoxesBot getBot() {
        return bot;
    }

    public abstract MessageEmbed show();

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public static Optional<StatusObject> create(String query, FoxesBot bot) {
        StatusSettings statusSettings = bot.getConfiguration().getConfigFile().getStatusSettings();
        if (statusSettings.getIssueStatusWrapper().containsKey(query.toLowerCase())) {
            return Optional.of(IssueStatus.create(bot, statusSettings.getIssueStatusWrapper().get(query.toLowerCase()), ":", statusSettings.getIssueStatusWrapper().get(query.toLowerCase())));
        } else if (statusSettings.getStatusPageWrapper().containsKey(query.toLowerCase())) {
            try {
                return Optional.of(StatusPage.fromURL(statusSettings.getStatusPageWrapper().get(query.toLowerCase()), query));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
        return Optional.empty();
    }
}
