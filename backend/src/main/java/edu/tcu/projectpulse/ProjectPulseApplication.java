package edu.tcu.projectpulse;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectPulseApplication {
    public static void main(String[] args) {
        // Ralph: Local runs may provide config through shell environment instead of a checked-in .env file.
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .ignoreIfMalformed()
                .load();

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
