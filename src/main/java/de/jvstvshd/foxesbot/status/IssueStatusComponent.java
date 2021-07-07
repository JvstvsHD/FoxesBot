package de.jvstvshd.foxesbot.status;

import com.google.common.collect.Lists;

import java.util.List;

public class IssueStatusComponent {

    private final String title;
    private final StatusState state;
    private final List<IssueStatusSubcomponent> subComponents;

    public IssueStatusComponent(String title, StatusState state) {
        this.title = title;
        this.state = state;
        this.subComponents = Lists.newArrayList();
    }

    public String getTitle() {
        return title;
    }

    public StatusState getState() {
        return state;
    }

    public List<IssueStatusSubcomponent> getSubComponents() {
        return subComponents;
    }

    @Override
    public String toString() {
        return "IssueStatusComponent{" +
                "title='" + title + '\'' +
                ", state=" + state +
                ", subComponents=" + subComponents.toString() +
                '}';
    }

    public boolean addSubcomponent(IssueStatusSubcomponent subcomponent) {
        return subComponents.add(subcomponent);
    }

    public boolean addSubcomponent(String subName, StatusState state) {
        return addSubcomponent(new IssueStatusSubcomponent(this, subName, state));
    }
}
