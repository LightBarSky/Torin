package com.torin.dbService.kafka.batch.processor;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.torin.dbService.contracts.BatchProcessor;
import com.torin.dbService.dto.BatchResultWordGroupAll;
import com.torin.dbService.dto.WordGroupAllDto;
import com.torin.dbService.jdbc.port.BatchRepositoryPort;
import com.torin.dbService.kafka.service.SendLogKafkaToDbService;

@Service
public class WordGroupAllBatchProcessor implements BatchProcessor<WordGroupAllDto> {
    private final BatchRepositoryPort batchRepository;
    private final SendLogKafkaToDbService sendLogKafkaToDbService;

    public WordGroupAllBatchProcessor(BatchRepositoryPort batchRepository,
            SendLogKafkaToDbService sendLogKafkaToDbService) {
        this.batchRepository = batchRepository;
        this.sendLogKafkaToDbService = sendLogKafkaToDbService;
    }
    
    @Override
    public Class<WordGroupAllDto> getType() {
        return WordGroupAllDto.class;
    }

    @Override
    public void process(List<WordGroupAllDto> batch, Instant lastMessageDate) throws SQLException, JsonProcessingException {
        BatchResultWordGroupAll batchResultWordGroupAll = batchRepository.wordGroupAllBatchWrite(batch);
        sendLogKafkaToDbService.status.statWordGroupAll().addAndGet(batch.size());
        sendLogKafkaToDbService.status.statWordGroupAll().setDate(lastMessageDate);
        String mes = String
                .format("""
                        WordGroupAll поступило %s [InsertOrUpdate = %s, notAffected = %s, unknow = %s, failed = %s],
                        из них WordGroupAllChanged [InsertOrUpdate = %s, notAffected = %s, unknow = %s, failed = %s],
                        из них Participants [InsertOrUpdate = %s, notAffected = %s, unknow = %s, failed = %s],
                        всего %s, дата последнего %s
                        """,
                        batch.size(),
                        batchResultWordGroupAll.resultW().getRowsUpdateOrInsert(),
                        batchResultWordGroupAll.resultW().getRowsNotAffected(),
                        batchResultWordGroupAll.resultW().getRowsUnknow(),
                        batchResultWordGroupAll.resultW().getRowsFailed(),
                        batchResultWordGroupAll.resultWC().getRowsUpdateOrInsert(),
                        batchResultWordGroupAll.resultWC().getRowsNotAffected(),
                        batchResultWordGroupAll.resultWC().getRowsUnknow(),
                        batchResultWordGroupAll.resultWC().getRowsFailed(),
                        batchResultWordGroupAll.resultP().getRowsUpdateOrInsert(),
                        batchResultWordGroupAll.resultP().getRowsNotAffected(),
                        batchResultWordGroupAll.resultP().getRowsUnknow(),
                        batchResultWordGroupAll.resultP().getRowsFailed(),
                        sendLogKafkaToDbService.status.statWordGroupAll().getCount(), lastMessageDate.atOffset(ZoneOffset.UTC));
        sendLogKafkaToDbService.sendLog(mes, "Info");
    }
}
