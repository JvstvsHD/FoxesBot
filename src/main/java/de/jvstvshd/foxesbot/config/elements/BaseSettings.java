package de.jvstvshd.foxesbot.config.elements;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class BaseSettings {

    private String commandChar = "c?";
    private String token = "";
    private String githubToken = "";
    private int helpEntriesPerPage = 0;

    public String getCommandChar() {
        return commandChar;
    }

    public String getToken() {
        return token;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public int getHelpEntriesPerPage() {
        return helpEntriesPerPage;
    }
}
