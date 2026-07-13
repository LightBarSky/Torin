package com.torin.postgres.helper;

import com.torin.core.dto.UserChangedDto;
import com.torin.core.dto.WordGroupAllChangedDto;
import com.torin.core.dto.WordGroupAllDto;
import com.torin.postgres.entity.UserChanged;
import com.torin.postgres.entity.WordGroupAll;
import com.torin.postgres.entity.WordGroupAllChanged;

public class Mapper {
    public static UserChangedDto mapperToDto(UserChanged userChanged) {
        return new UserChangedDto(userChanged.getId(), userChanged.getIdUser(), userChanged.getFirstName(),
                userChanged.getLastName(), userChanged.getUsername(), userChanged.getNumber(),
                userChanged.getUserPhoto(), userChanged.getUpdatedAt(), userChanged.getBirthday(),
                userChanged.getFlags(), userChanged.getFlags2(), userChanged.getFlagsFull(),
                userChanged.getFlags2Full(), userChanged.getAbout(), userChanged.getBotInfo(),
                userChanged.getPersonalChannelId(), userChanged.getLocationAddress(), userChanged.getLocationLat(),
                userChanged.getLocationLon(), userChanged.getLocationRadius());
    }

    public static WordGroupAllChangedDto mapperToDto(WordGroupAllChanged wordGroupAllChanged) {
        return new WordGroupAllChangedDto(wordGroupAllChanged.getId(), wordGroupAllChanged.getIdGroup(),
                wordGroupAllChanged.getInfoGroup(), wordGroupAllChanged.getTitleGroup(),
                wordGroupAllChanged.getFindGroup(), wordGroupAllChanged.getHashGroup(), wordGroupAllChanged.getType(),
                wordGroupAllChanged.getDate(), wordGroupAllChanged.getLinkedId(), wordGroupAllChanged.getFlags(),
                wordGroupAllChanged.getFlags2());
    }

    public static WordGroupAllDto mapperToDto(WordGroupAll wg) {
                return new WordGroupAllDto(wg.getId(), wg.getIdGroup(), wg.getInfoGroup(),
                                wg.getTitleGroup(),
                                wg.getFindGroup(), wg.getHashGroup(), wg.getType(),
                                wg.getHandlersId() == -1 ? false : true,
                                wg.getLastUpdate(),
                                wg.getLinkedId(), wg.getParticipantsCount(), wg.getCreatedDate(),
                                wg.getFlags(),
                                wg.getFlags2(), null);
        }
}
