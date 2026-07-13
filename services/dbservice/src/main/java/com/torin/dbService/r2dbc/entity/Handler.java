package com.torin.dbService.r2dbc.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "base_handler", name = "handlers")
public class Handler {
    @org.springframework.data.annotation.Id
    private Long id;

    @Column("api_id")
    private Long apiId;

    @Column("hash")
    private String hash;

    @Column("phone")
    private String phone;

    @Column("directory_for_user_photo")
    private String directoryForUserPhoto;

    @Column("directory_for_media")
    private String directoryForMedia;

    @Column("category")
    private String category;

    @Column("count_group")
    private Integer countGroup;

    @Column("name_handler")
    private String nameHandler;
}
