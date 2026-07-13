package com.torin.es.adapter;

import com.torin.es.port.ElasticSearchPort;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.ResponseBody;
import reactor.core.publisher.Mono;

public class NoOpElasticSearchAdadpter implements ElasticSearchPort {

    @Override
    public <T> Mono<ResponseBody<T>> search(SearchRequest searchRequest, Class<T> clazz) {
        return Mono.empty();
    }
}
