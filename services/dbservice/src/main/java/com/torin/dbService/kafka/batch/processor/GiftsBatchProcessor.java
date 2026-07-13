package com.torin.dbService.kafka.batch.processor;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.torin.dbService.contracts.BatchProcessor;
import com.torin.dbService.dto.BatchResultDto;
import com.torin.dbService.dto.GiftsDto;
import com.torin.dbService.jdbc.port.BatchRepositoryPort;
import com.torin.dbService.kafka.service.SendLogKafkaToDbService;

@Service
public class GiftsBatchProcessor implements BatchProcessor<GiftsDto> {
    private final BatchRepositoryPort batchRepository;
    private final SendLogKafkaToDbService sendLogKafkaToDbService;

    public GiftsBatchProcessor(BatchRepositoryPort batchRepository, SendLogKafkaToDbService sendLogKafkaToDbService) {
        this.batchRepository = batchRepository;
        this.sendLogKafkaToDbService = sendLogKafkaToDbService;
    }

    @Override
    public Class<GiftsDto> getType() {
        return GiftsDto.class;
    }

    @Override
    public void process(List<GiftsDto> batch, Instant lastMessageDate) throws SQLException, JsonProcessingException {
        BatchResultDto batchResultDto = batchRepository.giftsBatchWrite(batch);
        sendLogKafkaToDbService.status.statGifts().addAndGet(batch.size());
        sendLogKafkaToDbService.status.statGifts().setDate(lastMessageDate);
        String mes = String
                .format("""
                        Gifts поступило %s [InsertOrUpdate = %s, notAffected = %s, unknow = %s, failed = %s],
                        всего %s, дата последнего %s
                        """,
                        batch.size(),
                        batchResultDto.getRowsUpdateOrInsert(),
                        batchResultDto.getRowsNotAffected(),
                        batchResultDto.getRowsUnknow(),
                        batchResultDto.getRowsFailed(),
                        sendLogKafkaToDbService.status.statGifts().getCount(), lastMessageDate.atOffset(ZoneOffset.UTC));
        sendLogKafkaToDbService.sendLog(mes, "Info");
    }
}
