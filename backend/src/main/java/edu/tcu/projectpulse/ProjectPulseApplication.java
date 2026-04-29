package edu.tcu.projectpulse;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectPulseApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        applyDotenvValue(dotenv, "MONGODB_URI");
        applyDotenvValue(dotenv, "MONGODB_DATABASE");
        applyDotenvValue(dotenv, "PORT");
        applyDotenvValue(dotenv, "CORS_ALLOWED_ORIGINS");

        SpringApplication.run(ProjectPulseApplication.class, args);
    }

    private static void applyDotenvValue(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null && !value.isBlank() && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }
}
