package com.torin.dbService.r2dbc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.torin.dbService.r2dbc.adapter.HandlerAdapter;
import com.torin.dbService.r2dbc.adapter.NoOpHandlerAdapter;
import com.torin.dbService.r2dbc.adapter.NoOpNotificationsAdapter;
import com.torin.dbService.r2dbc.adapter.NoOpTaskChatAdapter;
import com.torin.dbService.r2dbc.adapter.NoOpWordGroupAllAdapter;
import com.torin.dbService.r2dbc.adapter.NotificationsAdapter;
import com.torin.dbService.r2dbc.adapter.TaskChatAdapter;
import com.torin.dbService.r2dbc.adapter.WordGroupAllAdapter;
import com.torin.dbService.r2dbc.port.HandlerPort;
import com.torin.dbService.r2dbc.port.NotificationsPort;
import com.torin.dbService.r2dbc.port.TaskChatPort;
import com.torin.dbService.r2dbc.port.WordGroupAllPort;
import com.torin.dbService.r2dbc.repository.HandlerRepository;
import com.torin.dbService.r2dbc.repository.NotificationsRepository;
import com.torin.dbService.r2dbc.repository.TaskChatRepository;
import com.torin.dbService.r2dbc.repository.WordGroupAllRepository;

@Configuration
public class R2dbcModuleConfig {
    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
    HandlerPort handlerAdapter(HandlerRepository handlerRepository) {
        return new HandlerAdapter(handlerRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "false")
    HandlerPort noopHandlerAdapter() {
        return new NoOpHandlerAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
    NotificationsPort notificationsAdapter(NotificationsRepository notificationsRepository) {
        return new NotificationsAdapter(notificationsRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "false")
    NotificationsPort noopNotificationsAdapter() {
        return new NoOpNotificationsAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
    TaskChatPort taskChatAdapter(TaskChatRepository taskChatRepository) {
        return new TaskChatAdapter(taskChatRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "false")
    TaskChatPort noopTaskChatAdapter() {
        return new NoOpTaskChatAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
    WordGroupAllPort wordGroupAllAdapter(WordGroupAllRepository wordGroupAllRepository) {
        return new WordGroupAllAdapter(wordGroupAllRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "false")
    WordGroupAllPort noopWordGroupAllAdapter() {
        return new NoOpWordGroupAllAdapter();
    }
}
