package com.torin.dbService.elasticsearch.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "logs_handlers_read")
public class LogHandler {

    @Id
    private String id;

    @Field(name = "level", type = FieldType.Keyword)
    private String level;

    @Field(name = "handler_id", type = FieldType.Keyword)
    private String handlerId;

    @Field(name = "message", type = FieldType.Text)
    private String message;

    @Field(name = "timestamp", type = FieldType.Date, pattern = {
            "uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSSX",
            "uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSX",
            "uuuu-MM-dd'T'HH:mm:ss.SSSSSSSX",
            "uuuu-MM-dd'T'HH:mm:ss.SSSSSSX",
            "uuuu-MM-dd'T'HH:mm:ss.SSSSSX",
            "uuuu-MM-dd'T'HH:mm:ss.SSSSX",
            "uuuu-MM-dd'T'HH:mm:ss.SSSX",
            "uuuu-MM-dd'T'HH:mm:ss.SSX",
            "uuuu-MM-dd'T'HH:mm:ss.SX",
            "uuuu-MM-dd'T'HH:mm:ss.X",
            "uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSS",
            "uuuu-MM-dd'T'HH:mm:ss.SSSSSSSS",
            "uuuu-MM-dd'T'HH:mm:ss.SSSSSSS",
            "uuuu-MM-dd'T'HH:mm:ss.SSSSSS",
            "uuuu-MM-dd'T'HH:mm:ss.SSSSS",
            "uuuu-MM-dd'T'HH:mm:ss.SSSS",
            "uuuu-MM-dd'T'HH:mm:ss.SSS",
            "uuuu-MM-dd'T'HH:mm:ss.SS",
            "uuuu-MM-dd'T'HH:mm:ss.S",
            "uuuu-MM-dd'T'HH:mm:ss",
    }, format = { DateFormat.strict_date_optional_time_nanos })
    private Instant timestamp;
}
