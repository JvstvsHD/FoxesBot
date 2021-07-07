package de.jvstvshd.foxesbot.status;

import com.google.common.collect.Lists;

import java.util.List;

public class StatusPageComponent {
    private String name;
    private StatusState statusState;
    private List<StatusPageComponent> childComponents;
    private boolean childComponent;

    public StatusPageComponent(String name, StatusState statusState, List<StatusPageComponent> childComponents, boolean childComponent) {
        this.name = name;
        this.statusState = statusState;
        this.childComponents = childComponents;
        this.childComponent = childComponent;
    }

    public StatusPageComponent(String name, StatusState statusState, boolean childComponent) {
        this.name = name;
        this.statusState = statusState;
        this.childComponents = Lists.newArrayList();
        this.childComponent = childComponent;
    }

    public StatusPageComponent(boolean childComponent) {
        this.name = childComponent ? "Unbekannte Sub-Komponente." : "Unbekannte Komponente.";
        this.statusState = StatusState.UNKNOWN;
        this.childComponents = Lists.newArrayList();
        this.childComponent =  childComponent;
    }

    public String getName() {
        return name;
    }

    public StatusState getStatusState() {
        return statusState;
    }

    public List<StatusPageComponent> getChildComponents() {
        if (this.childComponent)
            throw new UnsupportedOperationException("Cannot retrieve child components from child component.");
        return childComponents;
    }

    public boolean addChildComponent(StatusPageComponent childComponent) {
        if (!hasChildComponents())
            throw new UnsupportedOperationException("Cannot add a child component to an existing child component.");
        return childComponents.add(childComponent);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatusState(StatusState statusState) {
        this.statusState = statusState;
    }

    public void setChildComponents(List<StatusPageComponent> childComponents) {
        this.childComponents = childComponents;
    }

    public void setChildComponent(boolean childComponent) {
        this.childComponent = childComponent;
    }

    public boolean hasChildComponents() {
        return !childComponent && childComponents.size() > 0;
    }

    @Override
    public String toString() {
        return "StatusPageComponent{" +
                "name='" + name + '\'' +
                ", statusState=" + statusState +
                ", childComponents=" + childComponents +
                ", childComponent=" + childComponent +
                '}';
    }

    public String toPrettyString() {
        return name + ": " + statusState.getName();
    }
}
