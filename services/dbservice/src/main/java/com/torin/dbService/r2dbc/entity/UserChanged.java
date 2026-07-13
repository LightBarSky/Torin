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
@Table(name = "user_changed", schema = "base_changed")
public class UserChanged {
    @Id
    @Column("id")
    private Long id;

    @Column("id_user")
    private Long idUser;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("username")
    private String username;

    @Column("number")
    private String number;

    @Column("user_photo")
    private String userPhoto;

    @Column("updated_at")
    private Instant updatedAt;

    @Column("birthday")
    private String birthday;

    @Column("flags")
    private String flags;

    @Column("flags2")
    private String flags2;

    @Column("flags_full")
    private String flagsFull;

    @Column("flags2_full")
    private String flags2Full;

    @Column("about")
    private String about;

    @Column("bot_info")
    private String botInfo;

    @Column("personal_channel_id")
    private Long personalChannelId;

    @Column("location_address")
    private String locationAddress;

    @Column("location_lat")
    private Double locationLat;

    @Column("location_lon")
    private Double locationLon;

    @Column("location_radius")
    private Integer locationRadius;
}
