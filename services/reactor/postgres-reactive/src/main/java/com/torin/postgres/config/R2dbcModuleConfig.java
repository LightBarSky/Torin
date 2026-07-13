package com.torin.postgres.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.torin.postgres.adapter.NoOpUserChangedAdapter;
import com.torin.postgres.adapter.NoOpWordGroupAllAdapter;
import com.torin.postgres.adapter.NoOpWordGroupAllChangedAdapter;
import com.torin.postgres.adapter.UserChangedAdapter;
import com.torin.postgres.adapter.WordGroupAllAdapter;
import com.torin.postgres.adapter.WordGroupAllChangedAdapter;
import com.torin.postgres.port.UserChangedPort;
import com.torin.postgres.port.WordGroupAllChangedPort;
import com.torin.postgres.port.WordGroupAllPort;
import com.torin.postgres.repository.UserChangedRepository;
import com.torin.postgres.repository.WordGroupAllChangedRepository;
import com.torin.postgres.repository.WordGroupAllRepository;
import com.torin.postgres.service.UserChangedService;
import com.torin.postgres.service.WordGroupAllChangedService;
import com.torin.postgres.service.WordGroupAllService;

@Configuration(proxyBeanMethods = false)
public class R2dbcModuleConfig {
    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
    public UserChangedPort userChangedAdapter(UserChangedRepository userChangedRepository) {
        return new UserChangedAdapter(userChangedRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "false")
    public UserChangedPort noopUserChangedAdapter() {
        return new NoOpUserChangedAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
    public WordGroupAllPort wordGroupAllAdapter(WordGroupAllRepository wordGroupAllRepository) {
        return new WordGroupAllAdapter(wordGroupAllRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "false")
    public WordGroupAllPort noopWordGroupAllAdapter() {
        return new NoOpWordGroupAllAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "true", matchIfMissing = true)
    public WordGroupAllChangedPort wordGroupAllChangedAdapter(
            WordGroupAllChangedRepository wordGroupAllChangedRepository) {
        return new WordGroupAllChangedAdapter(wordGroupAllChangedRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "postgres.enabled", havingValue = "false")
    public WordGroupAllChangedPort noopWordGroupAllChangedAdapter() {
        return new NoOpWordGroupAllChangedAdapter();
    }

    @Bean
    WordGroupAllChangedService wordGroupAllChangedService(WordGroupAllChangedPort port) {
        return new WordGroupAllChangedService(port);
    }

    @Bean
    WordGroupAllService wordGroupAllService(WordGroupAllPort port) {
        return new WordGroupAllService(port);
    }

    @Bean
    UserChangedService userChangedService(UserChangedPort port) {
        return new UserChangedService(port);
    }
}
