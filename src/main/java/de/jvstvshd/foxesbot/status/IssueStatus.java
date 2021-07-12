package de.jvstvshd.foxesbot.status;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.jvstvshd.foxesbot.FoxesBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class IssueStatus extends StatusObject {

    private final FoxesBot bot;
    private List<IssueStatusComponent> issues = Lists.newArrayList();
    private final String subcomponentSplitter;
    private final String user;
    private final String repository;

    protected IssueStatus(FoxesBot bot, String subcomponentSplitter, String user, String repository, String title, String url) {
        super(bot, title, url);
        this.bot = bot;
        this.subcomponentSplitter = subcomponentSplitter;
        this.user = user;
        this.repository = repository;
    }

    protected IssueStatus(FoxesBot bot, String subcomponentSplitter, String url, String title) {
        super(bot, title, url);
        this.bot = bot;
        this.subcomponentSplitter = subcomponentSplitter;
        this.user = getUser(url);
        this.repository = getRepository(url);
    }

    public void update() throws IOException {
        GitHubClient client = bot.getGithubClient();
        IssueService issueService = new IssueService(client);
        List<Issue> issueList = issueService.getIssues(user, repository, null);
        HashMap<String, StatusState> subcomponents = Maps.newHashMap();
        List<IssueStatusComponent> components = Lists.newArrayList();
        for (Issue issue : issueList) {
            boolean isStatusIssue = false;
            boolean isComponent = false;
            boolean isSubComponent = false;
            for (Label label : issue.getLabels()) {
                if (label.getName().equalsIgnoreCase("issue status")) {
                    isStatusIssue = true;
                }
                if (label.getName().equalsIgnoreCase("component"))
                    isComponent = true;
                if (label.getName().equalsIgnoreCase("subcomponent"))
                    isSubComponent = true;
            }
            StatusState state = getState(issue.getLabels());
            if (!isStatusIssue) {
                continue;
            }
            if (isSubComponent) {
                subcomponents.put(issue.getTitle(), state);
                continue;
            }
            if (isComponent)
                components.add(new IssueStatusComponent(issue.getTitle(), state));
        }
        subcomponents.forEach((name, state) -> {
            for (IssueStatusComponent component : components) {
                if (name.toLowerCase().startsWith(component.getTitle().toLowerCase())) {
                    String subName;
                    try {
                        subName = name.split(subcomponentSplitter)[1];
                    } catch (Exception exception) {
                        subName = name;
                    }
                    component.addSubcomponent(subName, state);
                }
            }
        });
        this.issues = components;
    }

    @Override
    public MessageEmbed show() {
        try {
            update();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("GommeHD.net - Status").setDescription("");
        for (IssueStatusComponent issue : getIssues()) {
            if (issue.getSubComponents().size() == 0) {
                eb.appendDescription(issue.getTitle() + ": " + issue.getState().getTranslation() + "\n");
                continue;
            }
            StringBuilder status = new StringBuilder();
            for (IssueStatusSubcomponent subComponent : issue.getSubComponents()) {
                status.append(subComponent.getSubName()).append(": ").append(subComponent.getState().getTranslation()).append("\n");
            }
            MessageEmbed.Field field =
                    new MessageEmbed.Field(issue.getTitle() + ": " + issue.getState().getTranslation(),
                            status.toString(),
                            false);
            eb.addField(field).setTimestamp(Instant.now());

        }
        return eb.build();
    }

    public List<IssueStatusComponent> getIssues() {
        return new ArrayList<>(issues);
    }

    public StatusState getState(Collection<Label> labels) {
        for (Label label : labels) {
            if (isStatus(label.getName())) {
                Optional<StatusState> optionalState = StatusState.getByName(label.getName());
                return optionalState.orElse(StatusState.UNKNOWN);
            }
        }
        return StatusState.UNKNOWN;
    }

    private boolean isStatus(String label) {
        try {
            return StatusState.getByName(label).isPresent();
        } catch (Exception exception) {
            bot.getLogger().warn("Exception in thread " + Thread.currentThread().getName() + ": ", exception);
            return false;
        }
    }

    public static IssueStatus create(FoxesBot bot, String url, String splitter, String title) {
        return new IssueStatus(bot, splitter, url, title);
    }

    private String getRepository(String url) {
        String[] parts = split(url);
        return parts[parts.length-1];
    }

    private String getUser(String url) {
        String[] parts = split(url);
        return parts[parts.length-2];
    }

    private String[] split(String url) {
        if (url.charAt(url.length()-1) == '/')
            url = url.substring(0, url.length()-2);
        return url.split("/");
    }
}
