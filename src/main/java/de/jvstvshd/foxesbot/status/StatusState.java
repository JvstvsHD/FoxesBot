package de.jvstvshd.foxesbot.status;

import com.google.common.collect.Maps;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum StatusState {

    MAINTENANCE("maintenance", false, "Wartungsarbeiten"),
    OPERATIONAL("operational", true, "betriebsbereit"),
    PARTIAL_OUTAGE("partial outage", false, "teilweise ausgefallen"),
    MAJOR_OUTAGE("major outage", false, "Großausfall"),
    UNKNOWN("unknown", false, "Unbekannt.");


    private final String name;
    private final boolean operational;
    private final String translation;

    StatusState(String name, boolean operational, String translation) {
        this.name = name.toLowerCase();
        this.operational = operational;
        if (States.states == null)
            States.states = new HashMap<>();
        States.states.put(name, this);
        this.translation = new String(translation.getBytes(), StandardCharsets.UTF_8);
    }

    public static Optional<StatusState> getByName(String name) {
        return Optional.ofNullable(States.states.get(name.toLowerCase()));
    }

    public String getName() {
        return name;
    }

    public boolean isWorking() {
        return operational;
    }

    public String getTranslation() {
        return translation;
    }

    private static final class States {
        private static Map<String, StatusState> states = Maps.newHashMap();
    }
}
