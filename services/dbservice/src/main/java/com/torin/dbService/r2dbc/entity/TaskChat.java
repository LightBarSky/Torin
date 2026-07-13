package com.torin.dbService.r2dbc.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_chats", schema = "base_handler")
public class TaskChat {
    @Id
    private Long id;
    @Column("id_chat")
    private Long idChat;
    @Column("offset_id_new_message")
    private Long offsetIdNewMessage;
    @Column("offset_id_old_message")
    private Long offsetIdOldMessage;
    @Column("date_parse_user")
    private Instant dateParseUser;
    @Column("date_of_last_record")
    public Instant dateOfLastRecord;
}
