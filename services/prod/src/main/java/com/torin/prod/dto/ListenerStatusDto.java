package com.torin.prod.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListenerStatusDto {
    private ListenerStatusSubDto statUsers;
    private ListenerStatusSubDto statChats;
    private ListenerStatusSubDto statAdminChats;
    private ListenerStatusSubDto statGifts;
    private ListenerStatusSubDto statReaction;
    private ListenerStatusSubDto statReactionGeneral;
    private ListenerStatusSubDto statMessages;
    private ListenerStatusSubDto statMessagesProp;
    private ListenerStatusSubDto statMessagesEntet;
    private ListenerStatusSubDto statTaskChats;
    private ListenerStatusSubDto statWordGroupAll;
}
