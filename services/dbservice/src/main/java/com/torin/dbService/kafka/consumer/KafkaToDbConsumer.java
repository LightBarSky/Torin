package com.torin.dbService.kafka.consumer;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

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
import com.torin.dbService.kafka.service.KafkaToDbService;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaToDbConsumer {
    private final KafkaToDbService kafkaToDbService;

    public KafkaToDbConsumer(KafkaToDbService kafkaToDbService) {
        this.kafkaToDbService = kafkaToDbService;
    }

    @KafkaListener(topics = "${kafka-to-db.user-topic}", groupId = "${kafka-to-db.user-group-id}", id = "kafkaToDbUserListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenUser(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, UserDto.class);
    }

    @KafkaListener(topics = "${kafka-to-db.chat-topic}", groupId = "${kafka-to-db.chat-group-id}", id = "kafkaToDbChatListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenChat(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, ChatDto.class);
    }

    @KafkaListener(topics = "${kafka-to-db.admin-chats-topic}", groupId = "${kafka-to-db.admin-chats-group-id}", id = "kafkaToDbAdminChatsListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenAdminChats(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, AdminChatDto.class);
    }

    @KafkaListener(topics = "${kafka-to-db.gifts-topic}", groupId = "${kafka-to-db.gifts-group-id}", id = "kafkaToDbGiftsListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenGifts(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, GiftsDto.class);
    }

    @KafkaListener(topics = "${kafka-to-db.reactions-topic}", groupId = "${kafka-to-db.reactions-group-id}", id = "kafkaToDbReactionsListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenReactions(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, ReactionDto.class);
    }

    @KafkaListener(topics = "${kafka-to-db.reactions-general-topic}", groupId = "${kafka-to-db.reactions-general-group-id}", id = "kafkaToDbReactionsGeneralListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenReactionsGeneral(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, ReactionsGeneralDto.class);
    }

    @KafkaListener(topics = "${kafka-to-db.messages-topic}", groupId = "${kafka-to-db.messages-group-id}", id = "kafkaToDbMessagesListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenMessages(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, MessageDto.class);
    }

    @KafkaListener(topics = "${kafka-to-db.messages-properties-topic}", groupId = "${kafka-to-db.messages-properties-group-id}", id = "kafkaToDbMessagesPropListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenMessagesProp(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, MessagesPropertiesDto.class);
    }

    @KafkaListener(topics = "${kafka-to-db.messages-entities-topic}", groupId = "${kafka-to-db.messages-entities-group-id}", id = "kafkaToDbMessagesEntitListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenMessagesEntet(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, MessagesEntitiesDto.class);
    }

    @KafkaListener(topics = "${kafka-to-db.task-chats-topic}", groupId = "${kafka-to-db.task-chats-group-id}", id = "kafkaToDbTaskChatsListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenTaskChats(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, TaskChatDto.class);
    }

    @KafkaListener(topics = "${kafka-to-db.word-group-all-topic}", groupId = "${kafka-to-db.word_group_all-group-id}", id = "kafkaToDbWordGroupAllListener", autoStartup = "false", containerFactory = "kafkaToDbContainerFactory")
    public void listenWordGroupAll(List<ConsumerRecord<String, String>> records, Acknowledgment ack)
            throws Exception {
        kafkaToDbService.process(records, ack, WordGroupAllDto.class);
    }
}