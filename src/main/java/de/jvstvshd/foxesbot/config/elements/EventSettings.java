package de.jvstvshd.foxesbot.config.elements;

import com.google.common.collect.Maps;

import java.util.Map;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class EventSettings {

    private long eventSessionTimeout = 0L;
    private Map<String, Long> guildChannels = Maps.newHashMap();

    public long getEventSessionTimeout() {
        return eventSessionTimeout;
    }

    public Map<String, Long> getGuildChannels() {
        return guildChannels;
    }
}
