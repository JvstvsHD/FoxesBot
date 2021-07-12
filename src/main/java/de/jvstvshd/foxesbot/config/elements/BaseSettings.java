package de.jvstvshd.foxesbot.config.elements;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class BaseSettings {

    private String commandChar = "c?";
    private String token = "";
    private String githubToken = "";

    public String getCommandChar() {
        return commandChar;
    }

    public String getToken() {
        return token;
    }

    public String getGithubToken() {
        return githubToken;
    }

}
