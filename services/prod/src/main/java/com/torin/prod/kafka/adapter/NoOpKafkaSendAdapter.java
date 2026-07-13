package com.torin.prod.kafka.adapter;

import com.torin.prod.kafka.port.KafkaSendPort;

public class NoOpKafkaSendAdapter implements KafkaSendPort {

    @Override
    public void send(String topic, String payload) {

    }

}
