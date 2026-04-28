package edu.tcu.projectpulse;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectPulseApplication {
    public static void main(String[] args) {
        // Ralph: Local runs may provide config through shell environment instead of a checked-in .env file.
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        setPropertyIfPresent("MONGODB_URI", dotenv.get("MONGODB_URI"));
        setPropertyIfPresent("MONGODB_DATABASE", dotenv.get("MONGODB_DATABASE"));
        setPropertyIfPresent("PORT", dotenv.get("PORT"));
        setPropertyIfPresent("CORS_ALLOWED_ORIGINS", dotenv.get("CORS_ALLOWED_ORIGINS"));
        
        SpringApplication.run(ProjectPulseApplication.class, args);
    }

    private static void setPropertyIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            System.setProperty(key, value);
        }
    }
}
