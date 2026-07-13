package com.torin.dbService.kafka.batch.processor;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.torin.dbService.contracts.BatchProcessor;
import com.torin.dbService.dto.BatchResultDto;
import com.torin.dbService.dto.ReactionsGeneralDto;
import com.torin.dbService.jdbc.port.BatchRepositoryPort;
import com.torin.dbService.kafka.service.SendLogKafkaToDbService;

@Service
public class ReactionsGeneralBatchProcessor implements BatchProcessor<ReactionsGeneralDto> {
    private final BatchRepositoryPort batchRepository;
    private final SendLogKafkaToDbService sendLogKafkaToDbService;

    public ReactionsGeneralBatchProcessor(BatchRepositoryPort batchRepository,
            SendLogKafkaToDbService sendLogKafkaToDbService) {
        this.batchRepository = batchRepository;
        this.sendLogKafkaToDbService = sendLogKafkaToDbService;
    }
    
    @Override
    public Class<ReactionsGeneralDto> getType() {
        return ReactionsGeneralDto.class;
    }

    @Override
    public void process(List<ReactionsGeneralDto> batch, Instant lastMessageDate) throws SQLException, JsonProcessingException {
        BatchResultDto batchResultDto = batchRepository.reactionsGeneralBatchWrite(batch);
        sendLogKafkaToDbService.status.statReactionGeneral().addAndGet(batch.size());
        sendLogKafkaToDbService.status.statReactionGeneral().setDate(lastMessageDate);
        String mes = String
                .format("""
                        ReactionsGeneral поступило %s [InsertOrUpdate = %s, notAffected = %s, unknow = %s, failed = %s],
                        всего %s, дата последнего %s
                        """,
                        batch.size(),
                        batchResultDto.getRowsUpdateOrInsert(),
                        batchResultDto.getRowsNotAffected(),
                        batchResultDto.getRowsUnknow(),
                        batchResultDto.getRowsFailed(),
                        sendLogKafkaToDbService.status.statReactionGeneral().getCount(), lastMessageDate.atOffset(ZoneOffset.UTC));
        sendLogKafkaToDbService.sendLog(mes, "Info");
    }
}
