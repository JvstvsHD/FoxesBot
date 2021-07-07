package de.jvstvshd.foxesbot;

public class Launcher {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Missing token as argument.");
        }
        FoxesBot bot = new FoxesBot(args[0]);
        bot.start();
    }
}
