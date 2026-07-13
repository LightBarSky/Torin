package com.torin.dbService.kafka.batch.processor;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.torin.dbService.contracts.BatchProcessor;
import com.torin.dbService.dto.BatchResultDto;
import com.torin.dbService.dto.TaskChatDto;
import com.torin.dbService.jdbc.port.BatchRepositoryPort;
import com.torin.dbService.kafka.service.SendLogKafkaToDbService;

@Service
public class TaskChatsBatchProcessor implements BatchProcessor<TaskChatDto> {
    private final BatchRepositoryPort batchRepository;
    private final SendLogKafkaToDbService sendLogKafkaToDbService;

    public TaskChatsBatchProcessor(BatchRepositoryPort batchRepository, SendLogKafkaToDbService sendLogKafkaToDbService) {
        this.batchRepository = batchRepository;
        this.sendLogKafkaToDbService = sendLogKafkaToDbService;
    }

    @Override
    public Class<TaskChatDto> getType() {
        return TaskChatDto.class;
    }

    @Override
    public void process(List<TaskChatDto> batch, Instant lastMessageDate) throws SQLException, JsonProcessingException {
        BatchResultDto batchResultDto = batchRepository.taskChatsBatchWrite(batch);
        sendLogKafkaToDbService.status.statTaskChats().addAndGet(batch.size());
        sendLogKafkaToDbService.status.statTaskChats().setDate(lastMessageDate);
        String mes = String
                .format("""
                        TaskChats поступило %s [InsertOrUpdate = %s, notAffected = %s, unknow = %s, failed = %s],
                        всего %s, дата последнего %s
                        """,
                        batch.size(),
                        batchResultDto.getRowsUpdateOrInsert(),
                        batchResultDto.getRowsNotAffected(),
                        batchResultDto.getRowsUnknow(),
                        batchResultDto.getRowsFailed(),
                        sendLogKafkaToDbService.status.statTaskChats().getCount(), lastMessageDate.atOffset(ZoneOffset.UTC));
        sendLogKafkaToDbService.sendLog(mes, "Info");
    }
}
