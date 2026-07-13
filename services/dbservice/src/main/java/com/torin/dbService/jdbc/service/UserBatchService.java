package com.torin.dbService.jdbc.service;

import org.springframework.stereotype.Service;

import com.torin.dbService.dto.UserDto;
import com.torin.dbService.jdbc.port.JdbcQueryPort;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserBatchService {

    private final JdbcQueryPort jdbcQueryPort;

    public UserBatchService(JdbcQueryPort jdbcQueryPort) {
        this.jdbcQueryPort = jdbcQueryPort;
    }

    public Map<Long, UserDto> fetchExistingUsers(List<Long> idUsers) {
        if (idUsers.isEmpty()) {
            return Map.of();
        }

        String sql = "SELECT * FROM base_data.user WHERE id_user = ANY(?)";

        List<UserDto> users = jdbcQueryPort.query(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            Array sqlArray = con.createArrayOf("bigint", idUsers.toArray(new Long[0]));
            ps.setArray(1, sqlArray);
            return ps;
        }, (rs, rowNum) -> new UserDto(
                rs.getLong("id_user"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("username"),
                rs.getString("number"),
                rs.getString("user_photo"),
                rs.getString("pg_tags"),
                rs.getBoolean("is_geo"),
                rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toInstant()
                        : null,
                rs.getString("birthday"),
                rs.getString("flags"),
                rs.getString("flags2"),
                rs.getString("flags_full"),
                rs.getString("flags2_full"),
                rs.getString("about"),
                rs.getBoolean("is_bot"),
                rs.getString("bot_info"),
                rs.getLong("personal_channel_id"),
                rs.getString("location_address"),
                rs.getDouble("location_lat"),
                rs.getDouble("location_lon"),
                rs.getInt("location_radius")));

        return users.stream()
                .collect(Collectors.toMap(UserDto::idUser, u -> u));
    }
}