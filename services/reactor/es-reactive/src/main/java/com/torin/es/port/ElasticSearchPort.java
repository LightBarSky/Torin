package com.torin.es.port;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.ResponseBody;
import reactor.core.publisher.Mono;

public interface ElasticSearchPort {
    <T> Mono<ResponseBody<T>> search(SearchRequest searchRequest, Class<T> clazz);
}
