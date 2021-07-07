package de.jvstvshd.foxesbot.config;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.slf4j.LoggerFactory.getLogger;

public class Configuration {

    private static final Logger logger = getLogger(Configuration.class);

    private final ObjectMapper objectMapper;
    private ConfigFile configFile;

    private Configuration() {
        this.objectMapper = new ObjectMapper()
                .setDefaultPrettyPrinter(new DefaultPrettyPrinter());
    }

    public static Configuration create() {
        if (System.getProperty("foxesbot.config") == null) {
            System.setProperty("foxesbot.config", "config.json");
        }
        var configuration = new Configuration();
        configuration.reload();
        return configuration;
    }

    public void reload() {
        try {
            reloadFile();
        } catch (IOException exception) {
            logger.error("Could not load config, e");
            throw new ConfigException("Could not load config file", exception);
        }

        try {
            save();
        } catch (IOException exception) {
            logger.error("Could not save config.");
        }
    }

    public void save() throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValues(getConfig().toFile()).write(configFile);
    }

    private void ensureFile() throws IOException {
        Files.createDirectories(getConfig().getParent());
        if (!getConfig().toFile().exists()) {
            if (getConfig().toFile().createNewFile()) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValues(getConfig().toFile()).write(new ConfigFile());
            }
        }
    }

    private void reloadFile() throws IOException {
        ensureFile();
        this.configFile = objectMapper.readValue(getConfig().toFile(), ConfigFile.class);
    }

    private Path getConfig() {
        var home = new File(".").getAbsoluteFile().getParentFile().toPath();
        var property = System.getProperty("foxesbot.config");
        return Paths.get(home.toString(), property);
    }

    public ConfigFile getConfigFile() {
        return configFile;
    }
}
