package com.torin.postgres.entity;

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
@Table(name = "word_group_all", schema = "base_group")
public class WordGroupAll {

    @Id
    @Column("id")
    private Long id;

    @Column("id_group")
    private Long idGroup;

    @Column("info_group")
    private String infoGroup;

    @Column("title_group")
    private String titleGroup;

    @Column("find_group")
    private String findGroup;

    @Column("hash_group")
    private String hashGroup;

    @Column("id_user_join")
    private Long idUserJoin;

    @Column("type")
    private Integer type;

    @Column("handlers_id")
    private Long handlersId;

    @Column("last_update")
    private Instant lastUpdate;

    @Column("last_handle")
    private Instant lastHandle;

    @Column("total_send_request")
    private Integer totalSendRequest = 0;

    @Column("total_detect_private")
    private Integer totalDetectPrivate = 0;

    @Column("linked_id")
    private Long linkedId;

    @Column("participants_count")
    private Long participantsCount;

    @Column("created_date")
    private Instant createdDate;
    
    @Column("flags")
    private String flags;

    @Column("flags2")
    private String flags2;
}
