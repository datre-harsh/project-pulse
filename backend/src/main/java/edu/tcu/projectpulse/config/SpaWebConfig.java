package edu.tcu.projectpulse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path:^(?!api$)[^\\.]*}")
                .setViewName("forward:/index.html");
        registry.addViewController("/register-instructor/{token:[^\\.]*}")
                .setViewName("forward:/index.html");
    }
}
