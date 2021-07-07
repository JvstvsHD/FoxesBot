package de.jvstvshd.foxesbot.status;

public record IssueStatusSubcomponent(IssueStatusComponent headComponent,
                                      String subName,
                                      StatusState state) {

    public IssueStatusComponent getHeadComponent() {
        return headComponent;
    }

    public String getSubName() {
        return subName;
    }

    public StatusState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "IssueStatusSubComponent{" +
                "headComponent=" + headComponent.getTitle() +
                ", subName='" + subName + '\'' +
                ", state=" + state +
                '}';
    }
}
