package ru.alimovdev.datar.config;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class KeepAliveConfig {  // TODO костыль
    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(1);
    }

    @Bean
    public ApplicationRunner keepAliveRunner(ScheduledExecutorService scheduler) {
        return args -> {
            scheduler.scheduleAtFixedRate(() -> {
                System.out.println("Application keep-alive: " + new Date());
            }, 10, 10, TimeUnit.MINUTES);
        };
    }
}


/*


Мы имеем класс с аннотацией @Configuration, который указывает, что класс содержит определения бинов (Bean Definition).
В этом классе объявлен метод с аннотацией @Bean, который возвращает объект типа ApplicationRunner.
ApplicationRunner - это функциональный интерфейс из Spring Boot, который позволяет выполнить код после того, как приложение полностью запустится.
Метод run в этом интерфейсе вызывается сразу после запуска Spring ApplicationContext.
Теперь разберем код внутри метода:
Мы возвращаем реализацию ApplicationRunner в виде лямбда-выражения.
Внутри лямбды создается планировщик (ScheduledExecutorService) с одним потоком, используя фабричный метод Executors.newScheduledThreadPool(1).
Затем мы планируем задачу с фиксированной частотой с помощью метода scheduleAtFixedRate.
Первый аргумент: задача (Runnable) для выполнения. В нашем случае это лямбда, которая выводит в консоль сообщение с текущей датой.
Второй аргумент: начальная задержка (1) перед первым выполнением.
Третий аргумент: период между последующими выполнениями (1).
Четвертый аргумент: единица измерения времени (TimeUnit.HOURS), то есть задача будет выполняться каждый час, начиная через час после запуска.
Важно отметить, что задача будет выполняться в фоновом потоке, и так как мы используем ScheduledExecutorService, то задача будет выполняться
периодически до тех пор, пока приложение не будет остановлено.
Однако, в данном коде есть потенциальная проблема: мы не закрываем планировщик при остановке приложения. Это может привести к утечке ресурсов,
так как поток не будет завершен корректно.
Для исправления этого можно зарегистрировать хук на завершение приложения, чтобы закрыть планировщик.
Пример улучшенной версии:

В этой версии мы выносим биновый метод для создания ScheduledExecutorService и указываем метод shutdown как destroyMethod, что гарантирует закрытие
планировщика при остановке контекста Spring. Но в исходном коде такой обработки нет, поэтому при остановке приложения поток планировщика может
оставаться активным, что помешает корректному завершению JVM.
Итак, код создает и запускает периодическую задачу, которая каждый час выводит сообщение в консоль, начиная через час после запуска приложения.
 */
