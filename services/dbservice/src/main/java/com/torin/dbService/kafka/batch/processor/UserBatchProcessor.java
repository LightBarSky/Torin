package com.torin.dbService.kafka.batch.processor;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.torin.dbService.contracts.BatchProcessor;
import com.torin.dbService.dto.BatchResultUser;
import com.torin.dbService.dto.UserDto;
import com.torin.dbService.jdbc.port.BatchRepositoryPort;
import com.torin.dbService.kafka.service.SendLogKafkaToDbService;

@Service
public class UserBatchProcessor implements BatchProcessor<UserDto> {
    private final BatchRepositoryPort batchRepository;
    private final SendLogKafkaToDbService sendLogKafkaToDbService;

    public UserBatchProcessor(BatchRepositoryPort batchRepository, SendLogKafkaToDbService sendLogKafkaToDbService) {
        this.batchRepository = batchRepository;
        this.sendLogKafkaToDbService = sendLogKafkaToDbService;
    }

    @Override
    public Class<UserDto> getType() {
        return UserDto.class;
    }

    @Override
    public void process(List<UserDto> batch, Instant lastMessageDate) throws SQLException, JsonProcessingException {
        BatchResultUser batchResultUser = batchRepository.userBatchWrite(batch);
        sendLogKafkaToDbService.status.statUsers().addAndGet(batch.size());
        sendLogKafkaToDbService.status.statUsers().setDate(lastMessageDate);
        String mes = String
                .format("""
                        User поступило %s [InsertOrUpdate = %s, notAffected = %s, unknow = %s, failed = %s],
                        из них UserChanged [InsertOrUpdate = %s, notAffected = %s, unknow = %s, failed = %s],
                        всего %s, дата последнего %s
                        """,
                        batch.size(),
                        batchResultUser.user().getRowsUpdateOrInsert(),
                        batchResultUser.user().getRowsNotAffected(),
                        batchResultUser.user().getRowsUnknow(),
                        batchResultUser.user().getRowsFailed(),
                        batchResultUser.userChanged().getRowsUpdateOrInsert(),
                        batchResultUser.userChanged().getRowsNotAffected(),
                        batchResultUser.userChanged().getRowsUnknow(),
                        batchResultUser.userChanged().getRowsFailed(),
                        sendLogKafkaToDbService.status.statUsers().getCount(), lastMessageDate.atOffset(ZoneOffset.UTC));
        sendLogKafkaToDbService.sendLog(mes, "Info");
    }
}
