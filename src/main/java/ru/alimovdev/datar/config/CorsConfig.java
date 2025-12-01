package ru.alimovdev.datar.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") //registry.addMapping("/api/**")
                        .allowedOrigins("*") // TODO ONLY FOR TESTS!    .allowedOrigins("*")     .allowedOriginPatterns("*")  .allowedOriginPatterns("null", "*")
                        /*
                        .allowedOrigins(
                                "https://dmitriy-al.github.io",
                                "https://alimovdev.ru"
                        )
                         */
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false)
                        .maxAge(3600);

            }
        };
    }
}

/*

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);
        config.addAllowedOrigin("*"); // Разрешаем все origins
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Применяем ко всем путям

        return new CorsFilter(source);
    }
 */