package com.torin.prod.kafka.adapter;

import java.util.function.Function;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.TransactionManager;

public class NoOpKafkaReceiver<K, V> implements KafkaReceiver<K,V> {

    @Override
    public Flux<ReceiverRecord<K, V>> receive(Integer prefetch) {
        return Flux.empty();
    }

    @Override
    public Flux<Flux<ReceiverRecord<K, V>>> receiveBatch(Integer prefetch) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'receiveBatch'");
    }

    @Override
    public Flux<Flux<ConsumerRecord<K, V>>> receiveAutoAck(Integer prefetch) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'receiveAutoAck'");
    }

    @Override
    public Flux<ConsumerRecord<K, V>> receiveAtmostOnce(Integer prefetch) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'receiveAtmostOnce'");
    }

    @Override
    public Flux<Flux<ConsumerRecord<K, V>>> receiveExactlyOnce(TransactionManager transactionManager,
            Integer prefetch) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'receiveExactlyOnce'");
    }

    @Override
    public <T> Mono<T> doOnConsumer(Function<Consumer<K, V>, ? extends T> function) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doOnConsumer'");
    }
    
}
