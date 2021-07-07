package de.jvstvshd.foxesbot.config.elements;

import com.google.common.collect.Maps;

import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
public class StatusSettings {

    private Map<String, String> issueStatusWrapper = Maps.newHashMap();
    private Map<String, String> statusPageWrapper = Maps.newHashMap();
    private Map<String, String> subcomponentSplitters = Maps.newHashMap();

    public Map<String, String> getIssueStatusWrapper() {
        return issueStatusWrapper;
    }

    public Map<String, String> getStatusPageWrapper() {
        return statusPageWrapper;
    }

    public Map<String, String> getSubcomponentSplitters() {
        return subcomponentSplitters;
    }
}
