package de.jvstvshd.foxesbot.config;

import de.jvstvshd.foxesbot.config.elements.BaseSettings;
import de.jvstvshd.foxesbot.config.elements.EventSettings;
import de.jvstvshd.foxesbot.config.elements.StatusSettings;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})

public class ConfigFile {

    private StatusSettings statusSettings = new StatusSettings();
    private BaseSettings baseSettings = new BaseSettings();
    private EventSettings eventSettings = new EventSettings();

    public StatusSettings getStatusSettings() {
        return statusSettings;
    }

    public BaseSettings getBaseSettings() {
        return baseSettings;
    }

    public EventSettings getEventSettings() {
        return eventSettings;
    }
}
