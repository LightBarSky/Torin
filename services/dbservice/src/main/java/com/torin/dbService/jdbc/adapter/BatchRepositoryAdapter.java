package com.torin.dbService.jdbc.adapter;
import java.sql.*;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.postgresql.util.PGobject;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;

import com.torin.dbService.dto.AdminChatDto;
import com.torin.dbService.dto.BatchResultDto;
import com.torin.dbService.dto.BatchResultUser;
import com.torin.dbService.dto.BatchResultWordGroupAll;
import com.torin.dbService.dto.ChatDto;
import com.torin.dbService.dto.GiftsDto;
import com.torin.dbService.dto.MessageDto;
import com.torin.dbService.dto.MessagesEntitiesDto;
import com.torin.dbService.dto.MessagesPropertiesDto;
import com.torin.dbService.dto.ParticipantChangedDto;
import com.torin.dbService.dto.ReactionDto;
import com.torin.dbService.dto.ReactionsGeneralDto;
import com.torin.dbService.dto.TaskChatDto;
import com.torin.dbService.dto.UserDto;
import com.torin.dbService.dto.WordGroupAllChangedDto;
import com.torin.dbService.dto.WordGroupAllDto;
import com.torin.dbService.jdbc.port.BatchRepositoryPort;
import com.torin.dbService.jdbc.service.UserBatchService;
import com.torin.dbService.r2dbc.entity.UserChanged;
import com.torin.dbService.r2dbc.entity.WordGroupAll;
import com.torin.dbService.r2dbc.port.WordGroupAllPort;

public class BatchRepositoryAdapter implements BatchRepositoryPort {
    private final DataSource dataSource;
    private final WordGroupAllPort wordGroupAllPort;
    @Value("${kafka-to-db.delay-user-update-days}")
    private Integer delayUserUpdateDays;
    private final UserBatchService userBatchService;

    public BatchRepositoryAdapter(DataSource dataSource, WordGroupAllPort wordGroupAllPort,
            UserBatchService userBatchService) {
        this.wordGroupAllPort = wordGroupAllPort;
        this.dataSource = dataSource;
        this.userBatchService = userBatchService;
    }

    public BatchResultUser userBatchWrite(List<UserDto> users) throws SQLException {
        BatchResultDto batchResultUser = new BatchResultDto();
        BatchResultDto batchResultUserChanged = new BatchResultDto();

        users = new ArrayList<>(
                users.stream()
                        .collect(Collectors.toMap(UserDto::idUser, x -> x, (existing, replacement) -> existing))
                        .values());
        List<Long> idUsers = users.stream().map(UserDto::idUser).toList();
        int batchSize = users.size();
        Map<Long, UserDto> usersOld = userBatchService.fetchExistingUsers(idUsers);
        ArrayList<UserChanged> usersChanged = new ArrayList<>(batchSize);
        ArrayList<UserDto> usersUpdate = new ArrayList<>(batchSize);
        ArrayList<UserDto> usersInsert = new ArrayList<>(batchSize);

        for (UserDto u : users) {
            UserDto userOld = usersOld.get(u.idUser());
            if (userOld == null) {
                usersInsert.add(u);
            } else if (Math
                    .abs(Duration.between(userOld.updatedAt(), u.updatedAt()).toDays()) >= delayUserUpdateDays) {
                Map.Entry<Integer, UserChanged> userChangedMapEntry = userUpdate(u, usersOld.get(u.idUser()));
                if (userChangedMapEntry.getKey() == 1) {
                    usersChanged.add(userChangedMapEntry.getValue());
                    usersUpdate.add(u);
                } else if (userChangedMapEntry.getKey() == 2) {
                    usersUpdate.add(u);
                }
            }
        }

        if (usersChanged.size() > 0) {
            Set<YearMonth> ymUniq = new HashSet<>();
            for (UserChanged wc : usersChanged) {
                ZonedDateTime zdt = wc.getUpdatedAt().atZone(ZoneOffset.UTC);
                ymUniq.add(YearMonth.of(zdt.getYear(), zdt.getMonth()));
            }
            partitonCreate("SELECT base_changed.create_month_partition_user(?::timestamptz)", ymUniq);
            String sqlUserChanged = """
                    INSERT INTO base_changed.user_changed
                    (id_user,first_name,last_name,username,number,user_photo,updated_at,birthday,flags,flags2,
                    about,flags_full,flags2_full,bot_info,personal_channel_id,location_address,location_lat,location_lon,location_radius)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlUserChanged)) {
                conn.setAutoCommit(false);
                for (UserChanged user : usersChanged) {

                    ps.setLong(1, user.getIdUser());
                    ps.setString(2, user.getFirstName());
                    ps.setString(3, user.getLastName());
                    ps.setString(4, user.getUsername());
                    ps.setString(5, user.getNumber());
                    ps.setString(6, user.getUserPhoto());
                    ps.setObject(7, user.getUpdatedAt().atOffset(ZoneOffset.UTC),
                            java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.setString(8, user.getBirthday());
                    ps.setString(9, user.getFlags());
                    ps.setString(10, user.getFlags2());
                    ps.setString(11, user.getAbout());
                    ps.setString(12, user.getFlagsFull());
                    ps.setString(13, user.getFlags2Full());
                    ps.setString(14, user.getBotInfo());
                    ps.setObject(15, user.getPersonalChannelId(), java.sql.Types.BIGINT);
                    ps.setString(16, user.getLocationAddress());
                    ps.setObject(17, user.getLocationLat(), java.sql.Types.DOUBLE);
                    ps.setObject(18, user.getLocationLon(), java.sql.Types.DOUBLE);
                    ps.setObject(19, user.getLocationRadius(), java.sql.Types.INTEGER);
                    ps.addBatch();
                }
                batchResultUserChanged.update(ps.executeBatch());
                conn.commit();
            }
        }

        if (usersUpdate.size() > 0) {
            String sql = """
                    UPDATE base_data.user
                    SET
                        first_name = ?,
                        last_name = ?,
                        username = ?,
                        number = ?,
                        user_photo = ?,
                        is_geo = ?,
                        updated_at = ?,
                        birthday = ?,
                        flags = ?,
                        flags2 = ?,
                        about = ?,
                        flags_full = ?,
                        flags2_full = ?,
                        is_bot = ?,
                        bot_info = ?,
                        personal_channel_id = ?,
                        location_address = ?,
                        location_lat = ?,
                        location_lon = ?,
                        location_radius = ?
                    WHERE id_user = ?
                    """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (UserDto user : usersUpdate) {

                    ps.setString(1, user.firstName());
                    ps.setString(2, user.lastName());
                    ps.setString(3, user.username());
                    ps.setString(4, user.number());
                    ps.setString(5, user.userPhoto());
                    ps.setBoolean(6, user.isGeo());
                    ps.setObject(7, user.updatedAt().atOffset(ZoneOffset.UTC),
                            java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.setString(8, user.birthday());
                    ps.setString(9, user.flags());
                    ps.setString(10, user.flags2());
                    ps.setString(11, user.about());
                    ps.setString(12, user.flagsFull());
                    ps.setString(13, user.flags2Full());
                    ps.setBoolean(14, user.isBot());
                    ps.setString(15, user.botInfo());
                    ps.setObject(16, user.personalChannelId(), java.sql.Types.BIGINT);
                    ps.setString(17, user.locationAddress());
                    ps.setObject(18, user.locationLat(), java.sql.Types.DOUBLE);
                    ps.setObject(19, user.locationLon(), java.sql.Types.DOUBLE);
                    ps.setObject(20, user.locationRadius(), java.sql.Types.INTEGER);
                    ps.setLong(21, user.idUser());
                    ps.addBatch();
                }
                batchResultUser.update(ps.executeBatch());
                conn.commit();
            }
        }

        if (usersInsert.size() > 0) {
            String sql = """
                    INSERT INTO base_data.user
                    (id_user,first_name,last_name,username,number,user_photo,is_geo,updated_at,birthday,flags,flags2,
                    about,flags_full,flags2_full,is_bot,bot_info,personal_channel_id,location_address,location_lat,location_lon,location_radius)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id_user) DO NOTHING
                    """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (UserDto user : usersInsert) {

                    ps.setLong(1, user.idUser());
                    ps.setString(2, user.firstName());
                    ps.setString(3, user.lastName());
                    ps.setString(4, user.username());
                    ps.setString(5, user.number());
                    ps.setString(6, user.userPhoto());
                    ps.setBoolean(7, user.isGeo());
                    ps.setObject(8, user.updatedAt().atOffset(ZoneOffset.UTC),
                            java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.setString(9, user.birthday());
                    ps.setString(10, user.flags());
                    ps.setString(11, user.flags2());
                    ps.setString(12, user.about());
                    ps.setString(13, user.flagsFull());
                    ps.setString(14, user.flags2Full());
                    ps.setBoolean(15, user.isBot());
                    ps.setString(16, user.botInfo());
                    ps.setObject(17, user.personalChannelId(), java.sql.Types.BIGINT);
                    ps.setString(18, user.locationAddress());
                    ps.setObject(19, user.locationLat(), java.sql.Types.DOUBLE);
                    ps.setObject(20, user.locationLon(), java.sql.Types.DOUBLE);
                    ps.setObject(21, user.locationRadius(), java.sql.Types.INTEGER);
                    ps.addBatch();
                }
                batchResultUser.update(ps.executeBatch());
                conn.commit();
            }
        }
        return new BatchResultUser(batchResultUser, batchResultUserChanged);
    }

    public BatchResultWordGroupAll wordGroupAllBatchWrite(List<WordGroupAllDto> wordGroupAllDtos)
            throws SQLException {
        BatchResultDto batchResultW = new BatchResultDto();
        BatchResultDto batchResultP = new BatchResultDto();
        BatchResultDto batchResultWC = new BatchResultDto();

        Long[] idGroups = wordGroupAllDtos.stream().map(WordGroupAllDto::getIdGroup).toList().toArray(new Long[0]);
        int batchSize = wordGroupAllDtos.size();
        Map<Long, WordGroupAll> wordGroupAllOld = wordGroupAllPort.findByIdGroupIn(idGroups)
                .collectMap(WordGroupAll::getIdGroup, x -> x, HashMap::new).block();

        ArrayList<WordGroupAllChangedDto> wordGroupAllChangedInsert = new ArrayList<>(batchSize);
        ArrayList<ParticipantChangedDto> participantChangedInsert = new ArrayList<>(batchSize);
        ArrayList<WordGroupAllDto> wordGroupAllDtoUpdate = new ArrayList<>(batchSize);

        for (WordGroupAllDto w : wordGroupAllDtos) {
            if (wordGroupAllOld.containsKey(w.getIdGroup())) {
                Map.Entry<Integer, WordGroupAllChangedDto> wordGroupAllChangedMapEntry = wordGroupUpdate(w,
                        wordGroupAllOld.get(w.getIdGroup()));
                if (wordGroupAllChangedMapEntry.getKey() == 1) {
                    wordGroupAllDtoUpdate.add(w);
                    wordGroupAllChangedInsert.add(wordGroupAllChangedMapEntry.getValue());
                } else if (wordGroupAllChangedMapEntry.getKey() == 2) {
                    wordGroupAllDtoUpdate.add(w);
                }
            }
            if (w.getParticipantsCount() != null && w.getParticipantsCount() > 0) {
                participantChangedInsert.add(new ParticipantChangedDto(w.getIdGroup(),
                        w.getParticipantsCount(),
                        w.getLastUpdate()));
            }
        }

        if (wordGroupAllChangedInsert.size() > 0) {

            Set<YearMonth> ymUniq = new HashSet<>();

            for (WordGroupAllChangedDto wc : wordGroupAllChangedInsert) {
                ZonedDateTime zdt = wc.getDate().atZone(ZoneOffset.UTC);
                ymUniq.add(YearMonth.of(zdt.getYear(), zdt.getMonth()));
            }
            partitonCreate("SELECT base_changed.create_month_partition_group(?::timestamptz)", ymUniq);

            String sql = """
                    INSERT INTO base_changed.word_group_all_changed
                    (id_group,info_group,title_group,find_group,hash_group,type,date,linked_id,flags,flags2)
                    VALUES (?,?,?,?,?,?,?,?,?,?)
                    """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (WordGroupAllChangedDto wc : wordGroupAllChangedInsert) {

                    ps.setLong(1, wc.getIdGroup());
                    ps.setString(2, wc.getInfoGroup());
                    ps.setString(3, wc.getTitleGroup());
                    ps.setString(4, wc.getFindGroup());
                    ps.setString(5, wc.getHashGroup());
                    ps.setObject(6, wc.getType(), java.sql.Types.INTEGER);
                    ps.setObject(7, wc.getDate().atOffset(ZoneOffset.UTC), java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.setObject(8, wc.getLinkedId(), java.sql.Types.BIGINT);
                    ps.setString(9, wc.getFlags());
                    ps.setString(10, wc.getFlags2());
                    ps.addBatch();
                }
                batchResultWC.update(ps.executeBatch());
                conn.commit();
            }
        }

        if (participantChangedInsert.size() > 0) {

            Set<YearMonth> ymUniq = new HashSet<>();

            for (ParticipantChangedDto p : participantChangedInsert) {
                ZonedDateTime zdt = p.date().atZone(ZoneOffset.UTC);
                ymUniq.add(YearMonth.of(zdt.getYear(), zdt.getMonth()));
            }
            partitonCreate("SELECT base_changed.create_month_partition_participant(?::timestamptz)", ymUniq);

            String sql = """
                    INSERT INTO base_changed.participant_changed
                    (id_group,participants_count,date)
                    VALUES (?,?,?)
                    """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (ParticipantChangedDto p : participantChangedInsert) {

                    ps.setLong(1, p.idGroup());
                    ps.setLong(2, p.participantsCount());
                    ps.setObject(3, p.date().atOffset(ZoneOffset.UTC), java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.addBatch();
                }
                batchResultP.update(ps.executeBatch());
                conn.commit();
            }

            String sqlWGAUpdateParticipantsCount = """
                        UPDATE base_group.word_group_all SET
                            participants_count = ?
                        WHERE id_group = ?
                    """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlWGAUpdateParticipantsCount)) {
                conn.setAutoCommit(false);
                for (ParticipantChangedDto p : participantChangedInsert) {

                    ps.setObject(1, p.participantsCount(), java.sql.Types.BIGINT);
                    ps.setLong(2, p.idGroup());

                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            }
        }

        if (wordGroupAllDtoUpdate.size() > 0) {
            String sqlUserUpdate = """
                        UPDATE base_group.word_group_all SET
                            info_group = ?,
                            title_group = ?,
                            flags = ?,
                            flags2 = ?,
                            find_group = ?,
                            hash_group = ?,
                            participants_count = COALESCE (?, participants_count),
                            type = ?,
                            created_date = COALESCE (created_date, ?),
                            last_update = ?,
                            linked_id = ?
                        WHERE id_group = ?
                    """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sqlUserUpdate)) {
                conn.setAutoCommit(false);
                for (WordGroupAllDto w : wordGroupAllDtoUpdate) {

                    ps.setString(1, w.getInfoGroup());
                    ps.setString(2, w.getTitleGroup());
                    ps.setString(3, w.getFlags());
                    ps.setString(4, w.getFlags2());
                    ps.setString(5, w.getFindGroup());
                    ps.setString(6, w.getHashGroup());
                    ps.setObject(7, w.getParticipantsCount(), java.sql.Types.BIGINT);
                    ps.setObject(8, w.getType(), java.sql.Types.INTEGER);
                    ps.setObject(9, w.getCreatedDate() == null ? null : w.getCreatedDate().atOffset(ZoneOffset.UTC),
                            java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.setObject(10, w.getLastUpdate().atOffset(ZoneOffset.UTC),
                            java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.setObject(11, w.getLinkedId(), java.sql.Types.BIGINT);

                    ps.setLong(12, w.getIdGroup());

                    ps.addBatch();
                }
                batchResultW.update(ps.executeBatch());
                conn.commit();
            }
        }
        return new BatchResultWordGroupAll(batchResultW, batchResultWC, batchResultP);
    }

    public BatchResultDto chatBatchWrite(List<ChatDto> chats) throws SQLException {
        BatchResultDto batchResultDto = new BatchResultDto();

        if (chats.size() > 0) {
            String sql = """
                    INSERT INTO base_data.chat
                    (id_user,id_group, date_joined)
                    VALUES (?,?,?) ON CONFLICT (id_group,id_user) DO UPDATE
                    SET
                    date_joined = EXCLUDED.date_joined
                    WHERE base_data.chat.date_joined IS NULL
                      AND EXCLUDED.date_joined IS NOT NULL;
                    """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (ChatDto chat : chats) {

                    ps.setObject(1, chat.idUser(), java.sql.Types.BIGINT);
                    ps.setObject(2, chat.idGroup(), java.sql.Types.BIGINT);
                    ps.setObject(3, chat.dateJoined() == null ? null : chat.dateJoined().atOffset(ZoneOffset.UTC),
                            java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.addBatch();
                }
                batchResultDto.update(ps.executeBatch());
                conn.commit();
            }
        }

        return batchResultDto;
    }

    public BatchResultDto adminChatsBatchWrite(List<AdminChatDto> adminChats) throws SQLException {
        BatchResultDto batchResultDto = new BatchResultDto();
        String sql = """
                INSERT INTO base_data.admin_chats
                (id_user,id_group,status)
                 VALUES (?,?,?) ON CONFLICT (id_group,id_user) DO NOTHING
                """;
        if (adminChats.size() > 0) {
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (AdminChatDto adminChat : adminChats) {

                    ps.setObject(1, adminChat.idUser(), java.sql.Types.BIGINT);
                    ps.setObject(2, adminChat.idGroup(), java.sql.Types.BIGINT);
                    ps.setString(3, adminChat.status());
                    ps.addBatch();
                }
                batchResultDto.update(ps.executeBatch());
                conn.commit();
            }
        }

        return batchResultDto;
    }

    public BatchResultDto giftsBatchWrite(List<GiftsDto> gifts) throws SQLException {
        BatchResultDto batchResultDto = new BatchResultDto();
        String sql = """
                INSERT INTO base_data.gifts
                (id_group,id_from,id_gift,message,title_gift,stars,convert_stars,date,flags,flags2,availability_total)
                VALUES (?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id_group,id_from,id_gift,date) DO NOTHING
                """;
        if (gifts.size() > 0) {
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (GiftsDto gift : gifts) {

                    ps.setObject(1, gift.idGroup(), java.sql.Types.BIGINT);
                    ps.setObject(2, gift.idFrom(), java.sql.Types.BIGINT);
                    ps.setString(3, gift.idGift());
                    ps.setString(4, gift.message());
                    ps.setString(5, gift.titleGift());
                    ps.setObject(6, gift.stars(), java.sql.Types.BIGINT);
                    ps.setObject(7, gift.convertStars(), java.sql.Types.BIGINT);
                    ps.setObject(8, gift.date().atOffset(ZoneOffset.UTC), java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.setString(9, gift.flags());
                    ps.setString(10, gift.flags2());
                    ps.setObject(11, gift.availabilityTotal(), java.sql.Types.INTEGER);
                    ps.addBatch();
                }
                batchResultDto.update(ps.executeBatch());
                conn.commit();
            }
        }

        return batchResultDto;
    }

    public BatchResultDto reactionsBatchWrite(List<ReactionDto> batch) throws SQLException {
        BatchResultDto batchResultDto = new BatchResultDto();

        Set<YearMonth> ymUniq = new HashSet<>();

        for (ReactionDto reaction : batch) {
            ZonedDateTime zdt = reaction.date().atZone(ZoneOffset.UTC);
            ymUniq.add(YearMonth.of(zdt.getYear(), zdt.getMonth()));
        }
        partitonCreate("SELECT base_data.create_month_partition_reactions(?::timestamptz)", ymUniq);

        String sql = """
                INSERT INTO base_data.reactions
                (id_group,id_message,id_user,reaction,date)
                 VALUES (?,?,?,?,?) ON CONFLICT (id_group,id_message,id_user,reaction,date) DO NOTHING
                """;
        if (batch.size() > 0) {
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (ReactionDto reaction : batch) {

                    ps.setObject(1, reaction.idGroup(), java.sql.Types.BIGINT);
                    ps.setObject(2, reaction.idMessage(), java.sql.Types.BIGINT);
                    ps.setObject(3, reaction.idUser(), java.sql.Types.BIGINT);
                    ps.setString(4, reaction.reaction());
                    ps.setObject(5, reaction.date().atOffset(ZoneOffset.UTC),
                            java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.addBatch();
                }
                batchResultDto.update(ps.executeBatch());
                conn.commit();
            }
        }

        return batchResultDto;
    }

    public BatchResultDto reactionsGeneralBatchWrite(List<ReactionsGeneralDto> reactions) throws SQLException {
        BatchResultDto batchResultDto = new BatchResultDto();
        Set<YearMonth> ymUniq = new HashSet<>();

        for (ReactionsGeneralDto reaction : reactions) {
            ZonedDateTime zdt = reaction.date().atZone(ZoneOffset.UTC);
            ymUniq.add(YearMonth.of(zdt.getYear(), zdt.getMonth()));
        }
        partitonCreate("SELECT base_data.create_month_partition_reactions_general(?::timestamptz)", ymUniq);

        String sql = """
                INSERT INTO base_data.reactions_general
                (id_group,id_message,is_comments,reaction,count,date)
                 VALUES (?,?,?,?,?,?) ON CONFLICT (id_group,id_message,is_comments,reaction,date) DO NOTHING
                """;
        if (reactions.size() > 0) {
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (ReactionsGeneralDto reaction : reactions) {

                    ps.setObject(1, reaction.idGroup(), java.sql.Types.BIGINT);
                    ps.setObject(2, reaction.idMessage(), java.sql.Types.BIGINT);
                    ps.setObject(3,
                            reaction.isComments() == null ? 2 : reaction.isComments(), java.sql.Types.INTEGER);
                    ps.setString(4, reaction.reaction());
                    ps.setObject(5, reaction.count(), java.sql.Types.INTEGER);
                    ps.setObject(6, reaction.date().atOffset(ZoneOffset.UTC),
                            java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.addBatch();
                }
                batchResultDto.update(ps.executeBatch());
                conn.commit();
            }
        }

        return batchResultDto;
    }

    public BatchResultDto messagesBatchWrite(List<MessageDto> channels)
            throws SQLException {
        BatchResultDto batchResultDto = new BatchResultDto();
        Set<YearMonth> ymUniq = new HashSet<>();

        for (MessageDto channel : channels) {
            ZonedDateTime zdt = channel.date().atZone(ZoneOffset.UTC);
            ymUniq.add(YearMonth.of(zdt.getYear(), zdt.getMonth()));
        }
        partitonCreate("SELECT base_data.create_month_partition(?::timestamptz)", ymUniq);

        if (channels.size() > 0) {

            String sql = """
                    INSERT INTO base_data."messages"
                    (id_group,is_comments,reply_to_post,id_message,id_user,id_grouped_message,content_text,content_media,id_reply,date)
                    VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id_group,id_message,is_comments,date) DO NOTHING
                    """;
            if (channels.size() > 0) {
                try (Connection conn = dataSource.getConnection();
                        PreparedStatement ps = conn.prepareStatement(sql)) {
                    conn.setAutoCommit(false);
                    for (MessageDto channel : channels) {
                        ps.setObject(1, channel.idGroup());
                        ps.setObject(2, channel.isComments() == null ? 2 : channel.isComments(),
                                java.sql.Types.INTEGER);
                        ps.setObject(3, channel.replyToPost(), java.sql.Types.BIGINT);
                        ps.setObject(4, channel.idMessage(), java.sql.Types.BIGINT);
                        ps.setObject(5, channel.idUser(), java.sql.Types.BIGINT);
                        ps.setObject(6, channel.idGroupedMessage(), java.sql.Types.BIGINT);
                        ps.setString(7, channel.contentText());
                        ps.setString(8, channel.contentMedia());
                        ps.setObject(9, channel.idReply(), java.sql.Types.BIGINT);
                        ps.setObject(10, channel.date().atOffset(ZoneOffset.UTC),
                                java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                        ps.addBatch();
                    }
                    batchResultDto.update(ps.executeBatch());
                    conn.commit();
                }
            }

        }
        return batchResultDto;
    }

    public BatchResultDto messagesPropBatchWrite(List<MessagesPropertiesDto> mesProp)
            throws SQLException {
        BatchResultDto batchResultDto = new BatchResultDto();
        Set<YearMonth> ymUniq = new HashSet<>();

        for (MessagesPropertiesDto prop : mesProp) {
            ZonedDateTime zdt = prop.date().atZone(ZoneOffset.UTC);
            ymUniq.add(YearMonth.of(zdt.getYear(), zdt.getMonth()));
        }
        partitonCreate("SELECT base_data.create_month_partition_properties(?::timestamptz)", ymUniq);

        if (mesProp.size() > 0) {

            String sql = """
                    INSERT INTO base_data."messages_properties"
                    (id_group,is_comments,id_message,grouped_id,flags,flags2,has_text,has_media,media_type,media_value,forwards,
                    is_forward,fwd_value,views,replies,via_bot_id,via_business_bot_id,edit_date,date,id_from)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id_group,id_message,is_comments,date) DO NOTHING
                    """;
            if (mesProp.size() > 0) {
                try (Connection conn = dataSource.getConnection();
                        PreparedStatement ps = conn.prepareStatement(sql)) {
                    conn.setAutoCommit(false);
                    for (MessagesPropertiesDto prop : mesProp) {
                        ps.setObject(1, prop.idGroup());
                        ps.setObject(2, prop.isComments() == null ? 2 : prop.isComments(),
                                java.sql.Types.INTEGER);
                        ps.setObject(3, prop.idMessage(), java.sql.Types.BIGINT);
                        ps.setObject(4, prop.groupedId(), java.sql.Types.BIGINT);
                        ps.setString(5, prop.flags());
                        ps.setString(6, prop.flags2());
                        ps.setObject(7, prop.hasText(), java.sql.Types.BOOLEAN);
                        ps.setObject(8, prop.hasMedia(), java.sql.Types.BOOLEAN);
                        ps.setString(9, prop.mediaType());
                        ps.setObject(10, createJsonB(prop.mediaValue()));
                        ps.setObject(11, prop.forwards(), java.sql.Types.INTEGER);
                        ps.setObject(12, prop.isForward() == null ? false : prop.isForward(),
                                java.sql.Types.BOOLEAN);
                        ps.setObject(13, createJsonB(prop.fwdValue()));
                        ps.setObject(14, prop.views(), java.sql.Types.INTEGER);
                        ps.setObject(15, prop.replies(), java.sql.Types.INTEGER);
                        ps.setObject(16, prop.viaBotId(), java.sql.Types.BIGINT);
                        ps.setObject(17, prop.viaBusinessBotId(), java.sql.Types.BIGINT);
                        ps.setObject(18,
                                prop.editDate() == null ? null : prop.editDate().atOffset(ZoneOffset.UTC),
                                java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                        ps.setObject(19, prop.date().atOffset(ZoneOffset.UTC),
                                java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                        ps.setObject(20, prop.idFrom(), java.sql.Types.BIGINT);
                        ps.addBatch();
                    }
                    batchResultDto.update(ps.executeBatch());
                    conn.commit();
                }
            }

        }
        return batchResultDto;
    }

    public BatchResultDto messagesEntitiesBatchWrite(List<MessagesEntitiesDto> mesEntet)
            throws SQLException {
        BatchResultDto batchResultDto = new BatchResultDto();
        Set<YearMonth> ymUniq = new HashSet<>();

        for (MessagesEntitiesDto entet : mesEntet) {
            ZonedDateTime zdt = entet.date().atZone(ZoneOffset.UTC);
            ymUniq.add(YearMonth.of(zdt.getYear(), zdt.getMonth()));
        }
        partitonCreate("SELECT base_data.create_month_partition_entities(?::timestamptz)", ymUniq);

        if (mesEntet.size() > 0) {

            String sql = """
                    INSERT INTO base_data."messages_entities"
                    (id_group,is_comments,id_message,type,entity_offset,length,value,date)
                     VALUES (?,?,?,?,?,?,?,?) ON CONFLICT (id_group,id_message,is_comments,entity_offset,date) DO NOTHING
                    """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (MessagesEntitiesDto entet : mesEntet) {
                    ps.setObject(1, entet.idGroup());
                    ps.setObject(2, entet.isComments() == null ? 2 : entet.isComments(), java.sql.Types.INTEGER);
                    ps.setObject(3, entet.idMessage(), java.sql.Types.BIGINT);
                    ps.setString(4, entet.type());
                    ps.setObject(5, entet.entityOffset(), java.sql.Types.INTEGER);
                    ps.setObject(6, entet.length(), java.sql.Types.INTEGER);
                    ps.setString(7, entet.value());
                    ps.setObject(8, entet.date().atOffset(ZoneOffset.UTC), java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.addBatch();
                }
                batchResultDto.update(ps.executeBatch());
                conn.commit();
            }
        }
        return batchResultDto;
    }

    public BatchResultDto taskChatsBatchWrite(List<TaskChatDto> taskChats) throws SQLException {
        BatchResultDto batchResultDto = new BatchResultDto();

        if (taskChats.size() > 0) {
            String sql = """
                    UPDATE base_handler.task_chats SET
                        offset_id_new_message = COALESCE(?, offset_id_new_message),
                        offset_id_old_message = COALESCE(?, offset_id_old_message)
                    WHERE id_chat = ?
                    """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (TaskChatDto taskChat : taskChats) {
                    ps.setObject(1, taskChat.getOffsetIdNewMessage(), java.sql.Types.BIGINT);
                    ps.setObject(2, taskChat.getOffsetIdOldMessage(), java.sql.Types.BIGINT);
                    ps.setObject(3, taskChat.getIdChat(), java.sql.Types.BIGINT);
                    ps.addBatch();
                }
                batchResultDto.update(ps.executeBatch());
                conn.commit();
            }
        }

        return batchResultDto;
    }

    private PGobject createJsonB(String val) throws SQLException {
        PGobject jsonb = new PGobject();
        jsonb.setType("jsonb");
        jsonb.setValue(val);
        return jsonb;
    }

    private Map.Entry<Integer, UserChanged> userUpdate(UserDto userDto, UserDto userOldDto) {
        UserChanged result = new UserChanged();
        result.setIdUser(userOldDto.idUser());
        result.setUpdatedAt(userOldDto.updatedAt());
        int flag = 0;

        //Если вдруг когда-то это понадобится, надо переделывать
        // if (!isNullOrEmpty(userOldDto.getUserPhoto())
        //         &&
        //         !isNullOrEmpty(userDto.getUserPhoto())) {
        //     List<String> listNew = new ArrayList<>();
        //     List<String> listOld = Arrays.stream(userOldDto.getUserPhoto().split(","))
        //             .map(this::getWithoutExtensions)
        //             .filter(p -> !p.isEmpty())
        //             .toList();

        //     for (String photoNew : Arrays.stream(userDto.getUserPhoto().split(","))
        //             .map(this::getWithoutExtensions)
        //             .filter(p -> !p.isEmpty())
        //             .toArray(String[]::new)) {
        //         if (!listOld.contains(photoNew)) {
        //             listNew.add(photoNew);
        //         }
        //     }

        //     if (listNew.size() > 0) {
        //         if (userOldDto.getUserPhoto() != null &&
        //                 !userOldDto.getUserPhoto().isEmpty()) {
        //             result.setUserPhoto(userOldDto.getUserPhoto());
        //             flag = 1;
        //         }
        //     }
        // }
        // else if (isNullOrEmpty(userOldDto.getUserPhoto()) && !isNullOrEmpty(userDto.getUserPhoto())) {
        //     flag = 2;
        // }

        if (!isNullOrEmpty(userDto.username())) {
            if (!isNullOrEmpty(userOldDto.username()) &&
                    !userDto.username().toLowerCase().equals(userOldDto.username().toLowerCase())) {
                result.setUsername(userOldDto.username());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.username())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(userDto.number())) {
            if (!isNullOrEmpty(userOldDto.number()) &&
                    !userDto.number().equals(userOldDto.number())) {
                result.setNumber(userOldDto.number());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.number())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(userDto.firstName())) {
            if (!isNullOrEmpty(userOldDto.firstName()) &&
                    !userDto.firstName().toLowerCase().equals(userOldDto.firstName().toLowerCase())) {
                result.setFirstName(userOldDto.firstName());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.firstName())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(userDto.lastName())) {
            if (!isNullOrEmpty(userOldDto.lastName()) &&
                    !userDto.lastName().toLowerCase().equals(userOldDto.lastName().toLowerCase())) {
                result.setLastName(userOldDto.lastName());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.lastName())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(userDto.birthday())) {
            if (!isNullOrEmpty(userOldDto.birthday()) &&
                    !userDto.birthday().equals(userOldDto.birthday())) {
                result.setBirthday(userOldDto.birthday());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.birthday())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(userDto.flags())) {
            if (!isNullOrEmpty(userOldDto.flags()) &&
                    !userDto.flags().equals(userOldDto.flags())) {
                result.setFlags(userOldDto.flags());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.flags())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(userDto.flags2())) {
            if (!isNullOrEmpty(userOldDto.flags2()) &&
                    !userDto.flags2().equals(userOldDto.flags2())) {
                result.setFlags2(userOldDto.flags2());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.flags2())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(userDto.flagsFull())) {
            if (!isNullOrEmpty(userOldDto.flagsFull()) &&
                    !userDto.flagsFull().equals(userOldDto.flagsFull())) {
                result.setFlagsFull(userOldDto.flagsFull());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.flagsFull())) {
                flag = 2;
            }
        }
        if (!isNullOrEmpty(userDto.flags2Full())) {
            if (!isNullOrEmpty(userOldDto.flags2Full()) &&
                    !userDto.flags2Full().equals(userOldDto.flags2Full())) {
                result.setFlags2Full(userOldDto.flags2Full());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.flags2Full())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(userDto.about())) {
            if (!isNullOrEmpty(userOldDto.about()) &&
                    !userDto.about().toLowerCase().equals(userOldDto.about().toLowerCase())) {
                result.setAbout(userOldDto.about());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.about())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(userDto.botInfo())) {
            if (!isNullOrEmpty(userOldDto.botInfo()) &&
                    !userDto.botInfo().toLowerCase().equals(userOldDto.botInfo().toLowerCase())) {
                result.setBotInfo(userOldDto.botInfo());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.botInfo())) {
                flag = 2;
            }
        }

        if (userDto.personalChannelId() != null) {
            if (userOldDto.personalChannelId() != null &&
                    !userDto.personalChannelId().equals(userOldDto.personalChannelId())) {
                result.setPersonalChannelId(userOldDto.personalChannelId());
                flag = 1;
            } else if (userOldDto.personalChannelId() == null) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(userDto.locationAddress())) {
            if (!isNullOrEmpty(userOldDto.locationAddress()) &&
                    !userDto.locationAddress().toLowerCase().equals(userOldDto.locationAddress().toLowerCase())) {
                result.setLocationAddress(userOldDto.locationAddress());
                flag = 1;
            } else if (isNullOrEmpty(userOldDto.locationAddress())) {
                flag = 2;
            }
        }

        if (userDto.locationLat() != null) {
            if (userOldDto.locationLat() != null &&
                    !userDto.locationLat().equals(userOldDto.locationLat())) {
                result.setLocationLat(userOldDto.locationLat());
                flag = 1;
            } else if (userOldDto.locationLat() == null) {
                flag = 2;
            }
        }

        if (userDto.locationLon() != null) {
            if (userOldDto.locationLon() != null &&
                    !userDto.locationLon().equals(userOldDto.locationLon())) {
                result.setLocationLon(userOldDto.locationLon());
                flag = 1;
            } else if (userOldDto.locationLon() == null) {
                flag = 2;
            }
        }

        if (userDto.locationRadius() != null) {
            if (userOldDto.locationRadius() != null &&
                    !userDto.locationRadius().equals(userOldDto.locationRadius())) {
                result.setLocationRadius(userOldDto.locationRadius());
                flag = 1;
            } else if (userOldDto.locationRadius() == null) {
                flag = 2;
            }
        }

        return Map.entry(flag, result);
    }

    private void partitonCreate(String query, Set<YearMonth> ymUniq) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (YearMonth ym : ymUniq) {
                    OffsetDateTime startOfMonth = ym.atDay(1)
                            .atStartOfDay(ZoneOffset.UTC)
                            .toOffsetDateTime();
                    ps.setObject(1, startOfMonth, java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
                    ps.execute();
                }
            }
        }
    }

    private Map.Entry<Integer, WordGroupAllChangedDto> wordGroupUpdate(WordGroupAllDto wordGroupAllDto,
            WordGroupAll wordGroupAll) {
        WordGroupAllChangedDto resultW = new WordGroupAllChangedDto();
        resultW.setIdGroup(wordGroupAll.getIdGroup());
        resultW.setDate(wordGroupAll.getLastUpdate());
        int flag = 0;

        if (!isNullOrEmpty(wordGroupAllDto.getInfoGroup())) {
            if (!isNullOrEmpty(wordGroupAll.getInfoGroup()) &&
                    !wordGroupAllDto.getInfoGroup().toLowerCase().equals(wordGroupAll.getInfoGroup().toLowerCase())) {
                resultW.setInfoGroup(wordGroupAll.getInfoGroup());
                flag = 1;
            } else if (isNullOrEmpty(wordGroupAll.getInfoGroup())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(wordGroupAllDto.getFlags())) {
            if (!isNullOrEmpty(wordGroupAll.getFlags()) &&
                    !wordGroupAllDto.getFlags().equals(wordGroupAll.getFlags())) {
                resultW.setFlags(wordGroupAll.getFlags());
                flag = 1;
            } else if (isNullOrEmpty(wordGroupAll.getFlags())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(wordGroupAllDto.getFlags2())) {
            if (!isNullOrEmpty(wordGroupAll.getFlags2()) &&
                    !wordGroupAllDto.getFlags2().equals(wordGroupAll.getFlags2())) {
                resultW.setFlags2(wordGroupAll.getFlags2());
                flag = 1;
            } else if (isNullOrEmpty(wordGroupAll.getFlags2())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(wordGroupAllDto.getFindGroup())) {
            if (!isNullOrEmpty(wordGroupAll.getFindGroup()) &&
                    !wordGroupAllDto.getFindGroup().toLowerCase().equals(wordGroupAll.getFindGroup().toLowerCase())) {
                resultW.setFindGroup(wordGroupAll.getFindGroup());
                flag = 1;
            } else if (isNullOrEmpty(wordGroupAll.getFindGroup())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(wordGroupAllDto.getHashGroup())) {
            if (!isNullOrEmpty(wordGroupAll.getHashGroup()) &&
                    !wordGroupAllDto.getHashGroup().equals(wordGroupAll.getHashGroup())) {
                resultW.setHashGroup(wordGroupAll.getHashGroup());
                flag = 1;
            } else if (isNullOrEmpty(wordGroupAll.getHashGroup())) {
                flag = 2;
            }
        }

        if (!isNullOrEmpty(wordGroupAllDto.getTitleGroup())) {
            if (!isNullOrEmpty(wordGroupAll.getTitleGroup()) &&
                    !wordGroupAllDto.getTitleGroup().toLowerCase().equals(wordGroupAll.getTitleGroup().toLowerCase())) {
                resultW.setTitleGroup(wordGroupAll.getTitleGroup());
                flag = 1;
            } else if (isNullOrEmpty(wordGroupAll.getTitleGroup())) {
                flag = 2;
            }
        }

        if (wordGroupAllDto.getType() != null) {
            if (wordGroupAll.getType() != null &&
                    !wordGroupAllDto.getType().equals(wordGroupAll.getType())) {
                resultW.setType(wordGroupAll.getType());
                flag = 1;
            } else if (wordGroupAll.getType() == null) {
                flag = 2;
            }
        }

        if (wordGroupAllDto.getLinkedId() != null) {
            if (wordGroupAll.getLinkedId() != null &&
                    !wordGroupAllDto.getLinkedId().equals(wordGroupAll.getLinkedId())) {
                resultW.setLinkedId(wordGroupAll.getLinkedId());
                flag = 1;
            } else if (wordGroupAll.getLinkedId() == null) {
                flag = 2;
            }
        }
        return Map.entry(flag, resultW);
    }

    // private String getWithoutExtensions(String fileName) {
    //     int dotInd = fileName.lastIndexOf(".");

    //     return (dotInd == -1) ? fileName.trim() : fileName.substring(0, dotInd).trim();
    // }

    private Boolean isNullOrEmpty(String val) {
        if (val == null || val.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
}
