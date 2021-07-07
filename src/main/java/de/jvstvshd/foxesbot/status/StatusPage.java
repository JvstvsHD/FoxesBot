package de.jvstvshd.foxesbot.status;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;

public class StatusPage extends StatusObject {

    private final List<StatusPageComponent> components;

    public StatusPage(List<StatusPageComponent> components, String title, String url) {
        super(null, title, url);
        this.components = components;
    }

    @Override
    public MessageEmbed show() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(getTitle() + " - Status", getUrl());
        for (StatusPageComponent component : components) {
            if (component.hasChildComponents()) {
                StringBuilder fieldValueBuilder = new StringBuilder();
                for (StatusPageComponent childComponent : component.getChildComponents()) {
                    fieldValueBuilder.append("**").append(childComponent.getName()).append("**: ").append(childComponent.getStatusState().getTranslation()).append("\n");
                }
                MessageEmbed.Field field = new MessageEmbed.Field("__" + component.getName() + "__: " + component.getStatusState().getTranslation(), fieldValueBuilder.toString(), true);
                builder.addField(field);
            } else {
                builder.appendDescription("**").appendDescription(component.getName()).appendDescription("**: ").appendDescription(component.getStatusState().getTranslation()).appendDescription("\n");
            }
        }
        return builder.build();
    }

    public static StatusPage fromURL(String url, String title) throws IOException {
        List<StatusPageComponent> components = Lists.newArrayList();
        Document document = Jsoup.connect(url).get();
        for (Element elementsByClass : document.getElementsByClass("components-section")) {
            for (Element byClass : elementsByClass.getElementsByClass("component-container")) {
                StatusPageComponent statusPageComponent = new StatusPageComponent(false);
                List<StatusPageComponent> childComponents = Lists.newArrayList();
                for (Element name : byClass.getElementsByClass("name")) {
                    String nameText = name.text();
                    if (!isUsable(nameText))
                        continue;
                    statusPageComponent.setName(nameText);
                    break;
                }
                for (Element aClass : byClass.getElementsByClass("component-status")) {
                    if (isUsable(aClass.text()))
                        statusPageComponent.setStatusState(StatusState.getByName(aClass.text()).orElse(StatusState.UNKNOWN));
                }

                for (Element aClass : byClass.getElementsByClass("child-components-container ")) {
                    for (Element elements : aClass.getElementsByClass("component-inner-container")) {
                        StatusPageComponent childComponent = new StatusPageComponent(true);
                        for (Element name : elements.getElementsByClass("name")) {
                            childComponent.setName(name.text());
                        }
                        for (Element elementsElementsByClass : elements.getElementsByClass("component-status")) {
                            childComponent.setStatusState(StatusState.getByName(elementsElementsByClass.text()).orElse(StatusState.UNKNOWN));
                        }
                        childComponents.add(childComponent);
                    }
                }
                statusPageComponent.setChildComponents(childComponents);
                components.add(statusPageComponent);
            }
        }
        components.sort((o1, o2) -> Boolean.compare(o1.hasChildComponents(), o2.hasChildComponents()));
        return new StatusPage(components, title, url);
    }

    public static StatusPage fromURL(String url) throws IOException {
        return fromURL(url, url);
    }

    private static boolean isUsable(String string) {
        return string != null && !string.isEmpty() && !string.isBlank();
    }
}
