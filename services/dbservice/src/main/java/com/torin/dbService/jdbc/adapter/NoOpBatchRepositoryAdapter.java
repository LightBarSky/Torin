package com.torin.dbService.jdbc.adapter;

import java.sql.SQLException;
import java.util.List;

import com.torin.dbService.dto.AdminChatDto;
import com.torin.dbService.dto.BatchResultDto;
import com.torin.dbService.dto.BatchResultUser;
import com.torin.dbService.dto.BatchResultWordGroupAll;
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
import com.torin.dbService.jdbc.port.BatchRepositoryPort;

public class NoOpBatchRepositoryAdapter implements BatchRepositoryPort {

    @Override
    public BatchResultUser userBatchWrite(List<UserDto> users) throws SQLException {
        return new BatchResultUser(new BatchResultDto(), new BatchResultDto());
    }

    @Override
    public BatchResultWordGroupAll wordGroupAllBatchWrite(List<WordGroupAllDto> wordGroupAllDtos) throws SQLException {
        return new BatchResultWordGroupAll(new BatchResultDto(), new BatchResultDto(), new BatchResultDto());
    }

    @Override
    public BatchResultDto chatBatchWrite(List<ChatDto> chats) throws SQLException {
        return new BatchResultDto();
    }

    @Override
    public BatchResultDto adminChatsBatchWrite(List<AdminChatDto> adminChats) throws SQLException {
        return new BatchResultDto();
    }

    @Override
    public BatchResultDto giftsBatchWrite(List<GiftsDto> gifts) throws SQLException {
        return new BatchResultDto();
    }

    @Override
    public BatchResultDto reactionsBatchWrite(List<ReactionDto> batch) throws SQLException {
        return new BatchResultDto();
    }

    @Override
    public BatchResultDto reactionsGeneralBatchWrite(List<ReactionsGeneralDto> reactions) throws SQLException {
        return new BatchResultDto();
    }

    @Override
    public BatchResultDto messagesBatchWrite(List<MessageDto> channels) throws SQLException {
        return new BatchResultDto();
    }

    @Override
    public BatchResultDto messagesPropBatchWrite(List<MessagesPropertiesDto> mesProp) throws SQLException {
        return new BatchResultDto();
    }

    @Override
    public BatchResultDto messagesEntitiesBatchWrite(List<MessagesEntitiesDto> mesEntet) throws SQLException {
        return new BatchResultDto();
    }

    @Override
    public BatchResultDto taskChatsBatchWrite(List<TaskChatDto> taskChats) throws SQLException {
        return new BatchResultDto();
    }

}
