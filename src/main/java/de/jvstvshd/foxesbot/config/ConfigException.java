package de.jvstvshd.foxesbot.config;

public class ConfigException extends RuntimeException{

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
