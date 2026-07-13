package com.torin.dbService.jdbc.port;

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

public interface BatchRepositoryPort {

    public BatchResultUser userBatchWrite(List<UserDto> users) throws SQLException;

    public BatchResultWordGroupAll wordGroupAllBatchWrite(List<WordGroupAllDto> wordGroupAllDtos) throws SQLException;

    public BatchResultDto chatBatchWrite(List<ChatDto> chats) throws SQLException;

    public BatchResultDto adminChatsBatchWrite(List<AdminChatDto> adminChats) throws SQLException;

    public BatchResultDto giftsBatchWrite(List<GiftsDto> gifts) throws SQLException;

    public BatchResultDto reactionsBatchWrite(List<ReactionDto> batch) throws SQLException;

    public BatchResultDto reactionsGeneralBatchWrite(List<ReactionsGeneralDto> reactions) throws SQLException;

    public BatchResultDto messagesBatchWrite(List<MessageDto> channels) throws SQLException;

    public BatchResultDto messagesPropBatchWrite(List<MessagesPropertiesDto> mesProp) throws SQLException;

    public BatchResultDto messagesEntitiesBatchWrite(List<MessagesEntitiesDto> mesEntet) throws SQLException;

    public BatchResultDto taskChatsBatchWrite(List<TaskChatDto> taskChats) throws SQLException;
}
