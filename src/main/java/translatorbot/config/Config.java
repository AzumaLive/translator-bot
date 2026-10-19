package translatorbot.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {

    private static final String FILE_NAME = "config.properties";

    private final Properties properties = new Properties();

    public Config() {
        try (InputStream in = Files.newInputStream(Path.of(FILE_NAME))) {
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать " + FILE_NAME, e);
        }
    }

    public String getTelegramToken() {
        return properties.getProperty("telegram.bot.token");
    }
}
