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
@Table(name = "notifications", schema = "systems")
public class Notifications {
    @Id
    private Long id;
    @Column("timestamp")
    private Instant timestamp;
    @Column("type")
    private String type;
    @Column("message")
    private String message;
    @Column("read")
    private Boolean read;
}
