package ru.alimovdev.datar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


// 5. Настройка CORS
//Вместо аннотации @CrossOrigin на контроллере, можно настроить глобально в конфигурации
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedOrigins("https://dmitriy-al.github.io")
                .allowedMethods("GET", "POST", "PUT", "DELETE") // "PATCH",
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
