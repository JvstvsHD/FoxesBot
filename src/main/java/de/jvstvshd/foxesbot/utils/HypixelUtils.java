package de.jvstvshd.foxesbot.utils;

import org.jsoup.Jsoup;

import java.io.IOException;

public class HypixelUtils {

    /**
     * Retrieves the value of the progress bar from <href="https://hypixel.net/">Hypixel.net</a>.
     * @return value of the progress bar
     */
    public static int getProgress() throws IOException {
        return Integer.parseInt(Jsoup.connect("https://hypixel.net/").get().getElementById("progress").attr("value"));
    }
}
