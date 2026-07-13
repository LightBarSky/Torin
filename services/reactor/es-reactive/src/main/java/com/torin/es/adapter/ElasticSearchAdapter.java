package com.torin.es.adapter;

import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchClient;

import com.torin.es.port.ElasticSearchPort;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.ResponseBody;
import reactor.core.publisher.Mono;

public class ElasticSearchAdapter implements ElasticSearchPort {
    private final ReactiveElasticsearchClient reactiveElasticsearchClient;

    public ElasticSearchAdapter(ReactiveElasticsearchClient reactiveElasticsearchClient) {
        this.reactiveElasticsearchClient = reactiveElasticsearchClient;
    }

    @Override
    public <T> Mono<ResponseBody<T>> search(SearchRequest searchRequest, Class<T> clazz) {
        return reactiveElasticsearchClient.search(searchRequest, clazz);
    }
}
