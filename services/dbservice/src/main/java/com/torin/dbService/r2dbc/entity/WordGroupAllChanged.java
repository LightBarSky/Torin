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
@Table(name = "word_group_all_changed", schema = "base_changed")
public class WordGroupAllChanged {

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

    @Column("type")
    private Integer type;

    @Column("date")
    private Instant date = Instant.now();

    @Column("linked_id")
    private Long linkedId;

    @Column("flags")
    private String flags;

    @Column("flags2")
    private String flags2;
}
