package com.torin.dbService.service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.torin.dbService.dto.HandlerDto;
import com.torin.dbService.dto.NotificationsDto;
import com.torin.dbService.dto.TaskChatDto;
import com.torin.dbService.dto.UserDto;
import com.torin.dbService.dto.WordGroupAllChangedDto;
import com.torin.dbService.dto.WordGroupAllDto;
import com.torin.dbService.r2dbc.entity.Handler;
import com.torin.dbService.r2dbc.entity.Notifications;
import com.torin.dbService.r2dbc.entity.TaskChat;
import com.torin.dbService.r2dbc.entity.User;
import com.torin.dbService.r2dbc.entity.WordGroupAll;
import com.torin.dbService.r2dbc.entity.WordGroupAllChanged;

@Service
public class MapperService {

    public WordGroupAllDto toDto(WordGroupAll entity) {
        return new WordGroupAllDto(
                entity.getId(),
                entity.getIdGroup(),
                entity.getInfoGroup(),
                entity.getTitleGroup(),
                entity.getFindGroup(),
                entity.getHashGroup(),
                entity.getIdUserJoin(),
                entity.getType(),
                entity.getHandlersId(),
                entity.getLastUpdate(),
                entity.getLastHandle(),
                entity.getTotalSendRequest(),
                entity.getTotalDetectPrivate(),
                entity.getLinkedId(),
                entity.getParticipantsCount(),
                entity.getCreatedDate(),
                entity.getFlags(),
                entity.getFlags2());
    }

    public TaskChat toEntity(TaskChatDto dto) {
        return new TaskChat(
                dto.getId(),
                dto.getIdChat(),
                dto.getOffsetIdNewMessage(),
                dto.getOffsetIdOldMessage(),
                dto.getDateParseUser(),
                dto.getDateOfLastRecord());
    }

    public TaskChatDto toDto(TaskChat entity) {
        return new TaskChatDto(
                entity.getId(),
                entity.getIdChat(),
                entity.getOffsetIdNewMessage(),
                entity.getOffsetIdOldMessage(),
                entity.getDateParseUser(),
                entity.getDateOfLastRecord());
    }

    public WordGroupAll toEntity(WordGroupAllDto dto) {
        return new WordGroupAll(
                dto.getId(),
                dto.getIdGroup(),
                dto.getInfoGroup(),
                dto.getTitleGroup(),
                dto.getFindGroup(),
                dto.getHashGroup(),
                dto.getIdUserJoin(),
                dto.getType(),
                dto.getHandlersId(),
                dto.getLastUpdate(),
                dto.getLastHandle(),
                dto.getTotalSendRequest(),
                dto.getTotalDetectPrivate(),
                dto.getLinkedId(),
                dto.getParticipantsCount(),
                dto.getCreatedDate(),
                dto.getFlags(),
                dto.getFlags2());
    }

    public WordGroupAllChangedDto toDto(WordGroupAllChanged entity) {
        return new WordGroupAllChangedDto(
                entity.getId(),
                entity.getIdGroup(),
                entity.getInfoGroup(),
                entity.getTitleGroup(),
                entity.getFindGroup(),
                entity.getHashGroup(),
                entity.getType(),
                entity.getDate(),
                entity.getLinkedId(),
                entity.getFlags(),
                entity.getFlags2());
    }

    public WordGroupAllChanged toEntity(WordGroupAllChangedDto dto) {
        return new WordGroupAllChanged(
                dto.getId(),
                dto.getIdGroup(),
                dto.getInfoGroup(),
                dto.getTitleGroup(),
                dto.getFindGroup(),
                dto.getHashGroup(),
                dto.getType(),
                dto.getDate(),
                dto.getLinkedId(),
                dto.getFlags(),
                dto.getFlags2());
    }

    public HandlerDto toDto(Handler entity) {
        return new HandlerDto(
                entity.getId(),
                entity.getApiId(),
                entity.getHash(),
                entity.getPhone(),
                entity.getDirectoryForUserPhoto(),
                entity.getDirectoryForMedia(),
                entity.getCategory(),
                entity.getCountGroup(),
                entity.getNameHandler());
    }

    public Handler toEntity(HandlerDto dto) {
        return new Handler(
                dto.getId(),
                dto.getApiId(),
                dto.getHash(),
                dto.getPhone(),
                dto.getDirectoryForUserPhoto(),
                dto.getDirectoryForMedia(),
                dto.getCategory(),
                dto.getCountGroup(),
                dto.getNameHandler());
    }

    public UserDto toDto(User entity) {
        return new UserDto(
                entity.getIdUser(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getUsername(),
                entity.getNumber(),
                entity.getUserPhoto(),
                entity.getPgTags(),
                entity.isGeo(),
                entity.getUpdatedAt(),
                entity.getBirthday(),
                entity.getFlags(),
                entity.getFlags2(),
                entity.getFlagsFull(),
                entity.getFlags2Full(),
                entity.getAbout(),
                entity.isBot(),
                entity.getBotInfo(),
                entity.getPersonalChannelId(),
                entity.getLocationAddress(),
                entity.getLocationLat(),
                entity.getLocationLon(),
                entity.getLocationRadius());
    }

    public User toEntity(UserDto dto) {
        return new User(
                dto.idUser(),
                dto.firstName(),
                dto.lastName(),
                dto.username(),
                dto.number(),
                dto.userPhoto(),
                dto.pgTags(),
                dto.isGeo(),
                dto.updatedAt(),
                dto.birthday(),
                dto.flags(),
                dto.flags2(),
                dto.flagsFull(),
                dto.flags2Full(),
                dto.about(),
                dto.isBot(),
                dto.botInfo(),
                dto.personalChannelId(),
                dto.locationAddress(),
                dto.locationLat(),
                dto.locationLon(),
                dto.locationRadius());
    }

    public Notifications toEntity(NotificationsDto dto) {
        return new Notifications(
                dto.id(),
                dto.timestamp(),
                dto.type(),
                dto.message(),
                dto.read());
    }

    public NotificationsDto toDto(Notifications entity) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        return new NotificationsDto(
                entity.getId(),
                entity.getTimestamp(),
                entity.getType(),
                entity.getMessage(),
                entity.getRead(),
                entity.getTimestamp().atZone(ZoneOffset.UTC).format(formatter));
    }
}
