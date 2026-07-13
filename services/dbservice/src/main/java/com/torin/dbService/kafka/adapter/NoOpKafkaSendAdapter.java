package com.torin.dbService.kafka.adapter;

import com.torin.dbService.kafka.port.KafkaSendPort;

public class NoOpKafkaSendAdapter implements KafkaSendPort {
    @Override
    public void send(String topic, String key, String payload) {
        return;
    }
}
