package ru.alimovdev.datar.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

// Класс конфигурации Spring, который настраивает механизм кэширования
@Configuration  // Помечает класс как источник конфигурации бинов Spring
@EnableCaching  // Включает поддержку аннотаций кэширования (@Cacheable, @CacheEvict и др.)
public class CacheConfig {

    // Создает бин CacheManager - основной компонент для управления кэшем
    @Bean  // Помечает метод как создающий бин, который управляется Spring контейнером
    public CacheManager cacheManager() {
        // Создаем менеджер кэша для Caffeine (библиотека для кэширования)
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("passwordToId", "idToPassword"); // CaffeineCacheManager cacheManager = new CaffeineCacheManager("passwords");
        // Настраиваем параметры кэша с помощью Caffeine builder
        cacheManager.setCaffeine(Caffeine.newBuilder()
              //  .expireAfterWrite(5, TimeUnit.MINUTES) // Устанавливает TTL (Time To Live) -
                .expireAfterWrite(40, TimeUnit.SECONDS) // Устанавливает TTL (Time To Live) -
                // записи автоматически удаляются через 5 минут после создания
                .maximumSize(1000));                   // Максимальное количество записей в кэше -
        // при превышении старые записи удаляются
        return cacheManager; // Возвращаем сконфигурированный менеджер кэша
    }
}
