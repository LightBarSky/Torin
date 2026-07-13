package com.torin.dbService.kafka.batch.processor;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.torin.dbService.contracts.BatchProcessor;
import com.torin.dbService.dto.AdminChatDto;
import com.torin.dbService.dto.BatchResultDto;
import com.torin.dbService.jdbc.port.BatchRepositoryPort;
import com.torin.dbService.kafka.service.SendLogKafkaToDbService;

@Service
public class AdminChatsBatchProcessor implements BatchProcessor<AdminChatDto> {
    private final BatchRepositoryPort batchRepository;
    private final SendLogKafkaToDbService sendLogKafkaToDbService;

    public AdminChatsBatchProcessor(BatchRepositoryPort batchRepository, SendLogKafkaToDbService sendLogKafkaToDbService) {
        this.batchRepository = batchRepository;
        this.sendLogKafkaToDbService = sendLogKafkaToDbService;
    }

    @Override
    public Class<AdminChatDto> getType() {
        return AdminChatDto.class;
    }

    @Override
    public void process(List<AdminChatDto> batch, Instant lastMessageDate) throws SQLException, JsonProcessingException {
        BatchResultDto batchResultDto = batchRepository.adminChatsBatchWrite(batch);
        sendLogKafkaToDbService.status.statAdminChats().addAndGet(batch.size());
        sendLogKafkaToDbService.status.statAdminChats().setDate(lastMessageDate);
        String mes = String
                .format("""
                        AdminChats поступило %s [InsertOrUpdate = %s, notAffected = %s, unknow = %s, failed = %s],
                        всего %s, дата последнего %s
                        """,
                        batch.size(),
                        batchResultDto.getRowsUpdateOrInsert(),
                        batchResultDto.getRowsNotAffected(),
                        batchResultDto.getRowsUnknow(),
                        batchResultDto.getRowsFailed(),
                        sendLogKafkaToDbService.status.statAdminChats().getCount(), lastMessageDate.atOffset(ZoneOffset.UTC));
        sendLogKafkaToDbService.sendLog(mes, "Info");
    }
}
