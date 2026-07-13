package com.torin.dbService.kafka.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.torin.dbService.dto.AdminChatDto;
import com.torin.dbService.dto.ChatDto;
import com.torin.dbService.dto.GiftsDto;
import com.torin.dbService.dto.MessageDto;
import com.torin.dbService.dto.MessagesEntitiesDto;
import com.torin.dbService.dto.MessagesPropertiesDto;
import com.torin.dbService.dto.ReactionDto;
import com.torin.dbService.dto.ReactionsGeneralDto;
import com.torin.dbService.dto.TaskChatDto;
import com.torin.dbService.dto.UserDto;
import com.torin.dbService.dto.WordGroupAllDto;
import com.torin.dbService.kafka.batch.buffer.BatchBuffer;
import com.torin.dbService.kafka.batch.buffer.BatchBufferRegistry;
import com.torin.dbService.kafka.batch.processor.AdminChatsBatchProcessor;
import com.torin.dbService.kafka.batch.processor.ChatBatchProcessor;
import com.torin.dbService.kafka.batch.processor.GiftsBatchProcessor;
import com.torin.dbService.kafka.batch.processor.MessagesBatchProcessor;
import com.torin.dbService.kafka.batch.processor.MessagesEntitiesBatchProcessor;
import com.torin.dbService.kafka.batch.processor.MessagesPropertiesBatchProcessor;
import com.torin.dbService.kafka.batch.processor.ReactionsBatchProcessor;
import com.torin.dbService.kafka.batch.processor.ReactionsGeneralBatchProcessor;
import com.torin.dbService.kafka.batch.processor.TaskChatsBatchProcessor;
import com.torin.dbService.kafka.batch.processor.UserBatchProcessor;
import com.torin.dbService.kafka.batch.processor.WordGroupAllBatchProcessor;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KafkaToDbProperties.class)
public class BatchBufferConfig {
    private final BatchBufferRegistry batchBufferRegistry;

    public BatchBufferConfig(BatchBufferRegistry batchBufferRegistry) {
        this.batchBufferRegistry = batchBufferRegistry;
    }

    @Bean
    public BatchBuffer<UserDto> userBatchBuffer(KafkaToDbProperties props, UserBatchProcessor processor) {
        BatchBuffer<UserDto> buffer = new BatchBuffer<>(props.getUserSize(), processor);
        batchBufferRegistry.register(UserDto.class, buffer);
        return buffer;
    }

    @Bean
    public BatchBuffer<ChatDto> chatBatchBuffer(KafkaToDbProperties props, ChatBatchProcessor processor) {
        BatchBuffer<ChatDto> buffer = new BatchBuffer<>(props.getChatSize(), processor);
        batchBufferRegistry.register(ChatDto.class, buffer);
        return buffer;
    }

    @Bean
    public BatchBuffer<AdminChatDto> adminChatsBatchBuffer(KafkaToDbProperties props,
            AdminChatsBatchProcessor processor) {
        BatchBuffer<AdminChatDto> buffer = new BatchBuffer<>(props.getAdminChatsSize(), processor);
        batchBufferRegistry.register(AdminChatDto.class, buffer);
        return buffer;
    }

    @Bean
    public BatchBuffer<GiftsDto> giftsBatchBuffer(KafkaToDbProperties props, GiftsBatchProcessor processor) {
        BatchBuffer<GiftsDto> buffer = new BatchBuffer<>(props.getGiftsSize(), processor);
        batchBufferRegistry.register(GiftsDto.class, buffer);
        return buffer;
    }

    @Bean
    public BatchBuffer<ReactionDto> reactionsBatchBuffer(KafkaToDbProperties props,
            ReactionsBatchProcessor processor) {
        BatchBuffer<ReactionDto> buffer = new BatchBuffer<>(props.getReactionsSize(), processor);
        batchBufferRegistry.register(ReactionDto.class, buffer);
        return buffer;
    }

    @Bean
    public BatchBuffer<ReactionsGeneralDto> reactionsGeneralBatchBuffer(KafkaToDbProperties props,
            ReactionsGeneralBatchProcessor processor) {
        BatchBuffer<ReactionsGeneralDto> buffer = new BatchBuffer<>(props.getReactionsGeneralSize(), processor);
        batchBufferRegistry.register(ReactionsGeneralDto.class, buffer);
        return buffer;
    }

    @Bean
    public BatchBuffer<MessageDto> messagesBatchBuffer(KafkaToDbProperties props, MessagesBatchProcessor processor) {
        BatchBuffer<MessageDto> buffer = new BatchBuffer<>(props.getMessagesSize(), processor);
        batchBufferRegistry.register(MessageDto.class, buffer);
        return buffer;
    }

    @Bean
    public BatchBuffer<MessagesPropertiesDto> messagesPropertiesBatchBuffer(KafkaToDbProperties props,
            MessagesPropertiesBatchProcessor processor) {
        BatchBuffer<MessagesPropertiesDto> buffer = new BatchBuffer<>(props.getMessagesPropertiesSize(), processor);
        batchBufferRegistry.register(MessagesPropertiesDto.class, buffer);
        return buffer;
    }

    @Bean
    public BatchBuffer<MessagesEntitiesDto> messagesEntitiesBatchBuffer(KafkaToDbProperties props,
            MessagesEntitiesBatchProcessor processor) {
        BatchBuffer<MessagesEntitiesDto> buffer = new BatchBuffer<>(props.getMessagesEntitiesSize(), processor);
        batchBufferRegistry.register(MessagesEntitiesDto.class, buffer);
        return buffer;
    }

    @Bean
    public BatchBuffer<TaskChatDto> taskChatsBatchBuffer(KafkaToDbProperties props,
            TaskChatsBatchProcessor processor) {
        BatchBuffer<TaskChatDto> buffer = new BatchBuffer<>(props.getTaskChatsSize(), processor);
        batchBufferRegistry.register(TaskChatDto.class, buffer);
        return buffer;
    }

    @Bean
    public BatchBuffer<WordGroupAllDto> wordGroupAllBatchBuffer(KafkaToDbProperties props,
            WordGroupAllBatchProcessor processor) {
        BatchBuffer<WordGroupAllDto> buffer = new BatchBuffer<>(props.getWordGroupAllSize(), processor);
        batchBufferRegistry.register(WordGroupAllDto.class, buffer);
        return buffer;
    }
}
