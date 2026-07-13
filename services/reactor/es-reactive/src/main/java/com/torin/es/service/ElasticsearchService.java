package com.torin.es.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;

import com.torin.core.dto.ChatDto;
import com.torin.core.dto.GiftsDto;
import com.torin.core.dto.MessagesPropertiesDto;
import com.torin.core.dto.ParticipantChangedDto;
import com.torin.core.dto.ReactionsDto;
import com.torin.core.dto.ReactionsGeneralDto;
import com.torin.core.dto.UserDto;
import com.torin.es.port.ElasticSearchPort;

public class ElasticsearchService {

        private int SIZE_CHAT_BATCH = 1000;

        private final ElasticSearchPort elasticSearchPort;

        public ElasticsearchService(ElasticSearchPort elasticSearchPort) {
                this.elasticSearchPort = elasticSearchPort;
        }

        public Flux<GiftsDto> searchGiftsStream(Long idFrom, Long idGroup, Instant from, Instant to) {
                return fetchPageGifts(idFrom, idGroup, from, to, null)
                                .expand(response -> {

                                        List<Hit<GiftsDto>> hits = response.hits().hits();

                                        if (hits.isEmpty()) {
                                                return Mono.empty();
                                        }

                                        Hit<GiftsDto> lastHit = hits.get(hits.size() - 1);

                                        List<FieldValue> nextSearchAfter = lastHit.sort();

                                        return fetchPageGifts(idFrom, idGroup, from, to, nextSearchAfter);
                                })
                                .flatMapIterable(r -> r.hits().hits())
                                .map(Hit::source);
        }

        public Flux<MessagesPropertiesDto> searchMessagesPropertiesStream(Long idGroup, Long idUser, Instant from,
                        Instant to) {
                return fetchPageMessagesProperties(idGroup, idUser, from, to, null)
                                .expand(response -> {

                                        List<Hit<MessagesPropertiesDto>> hits = response.hits().hits();

                                        if (hits.isEmpty()) {
                                                return Mono.empty();
                                        }

                                        Hit<MessagesPropertiesDto> lastHit = hits.get(hits.size() - 1);

                                        List<FieldValue> nextSearchAfter = lastHit.sort();

                                        return fetchPageMessagesProperties(idGroup, idUser, from, to, nextSearchAfter);
                                })
                                .flatMapIterable(r -> r.hits().hits())
                                .map(Hit::source);
        }

        public Flux<ReactionsDto> searchReactionsStream(Long idGroup, Long idUser, Instant from, Instant to) {
                return fetchPageReactions(idGroup, idUser, from, to, null)
                                .expand(response -> {

                                        List<Hit<ReactionsDto>> hits = response.hits().hits();

                                        if (hits.isEmpty()) {
                                                return Mono.empty();
                                        }

                                        Hit<ReactionsDto> lastHit = hits.get(hits.size() - 1);

                                        List<FieldValue> nextSearchAfter = lastHit.sort();

                                        return fetchPageReactions(idGroup, idUser, from, to, nextSearchAfter);
                                })
                                .flatMapIterable(r -> r.hits().hits())
                                .map(Hit::source);
        }

        public Flux<ReactionsGeneralDto> searchReactionsGeneralStream(Long idGroup, Instant from, Instant to) {
                return fetchPageReactionsGeneral(idGroup, from, to, null)
                                .expand(response -> {

                                        List<Hit<ReactionsGeneralDto>> hits = response.hits().hits();

                                        if (hits.isEmpty()) {
                                                return Mono.empty();
                                        }

                                        Hit<ReactionsGeneralDto> lastHit = hits.get(hits.size() - 1);

                                        List<FieldValue> nextSearchAfter = lastHit.sort();

                                        return fetchPageReactionsGeneral(idGroup, from, to, nextSearchAfter);
                                })
                                .flatMapIterable(r -> r.hits().hits())
                                .map(Hit::source);
        }

        public Flux<ParticipantChangedDto> searchParticipantChangedStream(Long idGroup, Instant from, Instant to) {
                return fetchPageParticipantChanged(idGroup, from, to, null)
                                .expand(response -> {

                                        List<Hit<ParticipantChangedDto>> hits = response.hits().hits();

                                        if (hits.isEmpty()) {
                                                return Mono.empty();
                                        }

                                        Hit<ParticipantChangedDto> lastHit = hits.get(hits.size() - 1);

                                        List<FieldValue> nextSearchAfter = lastHit.sort();

                                        return fetchPageParticipantChanged(idGroup, from, to, nextSearchAfter);
                                })
                                .flatMapIterable(r -> r.hits().hits())
                                .map(Hit::source);
        }

        public Flux<ChatDto> searchChatStreamUnique(Long idUser, Long idGroup) {
                return searchChatStream(idUser, idGroup)
                                .filter(c -> c.id() != null)
                                .collect(
                                                HashMap<Long, ChatDto>::new,
                                                (map, incoming) -> {
                                                        ChatDto existing = map.get(incoming.id());

                                                        if (existing == null) {
                                                                map.put(incoming.id(), incoming);
                                                                return;
                                                        }

                                                        Long ev = existing.version();
                                                        Long iv = incoming.version();

                                                        if (ev == null || (iv != null && iv > ev)) {
                                                                map.put(incoming.id(), incoming);
                                                        }
                                                })
                                .flatMapMany(map -> Flux.fromIterable(map.values()));
        }

        public Mono<UserDto> searchUser(
                        Long idUser, String username) {
                if (username == null && idUser == null) {
                        return Mono.empty();
                }
                List<Query> filters = new ArrayList<>();
                if (idUser != null) {
                        TermQuery termQuery = TermQuery.of(t -> t
                                        .field("id_user")
                                        .value(idUser));
                        filters.add(termQuery._toQuery());
                }

                if (username != null) {
                        TermQuery termQuery = TermQuery.of(t -> t
                                        .field("username")
                                        .value(username));
                        filters.add(termQuery._toQuery());
                }

                Query query = Query.of(q -> q
                                .bool(b -> b.filter(filters)));

                SearchRequest searchRequest = SearchRequest.of(s -> {
                        s.index("user_read")
                                        .query(query)
                                        .sort(sort -> sort
                                                        .field(f -> f
                                                                        .field("version")
                                                                        .order(SortOrder.Desc)))
                                        .collapse(c -> c
                                                        .field("id_user"))
                                        .size(1);
                        return s;
                });

                return elasticSearchPort
                                .search(searchRequest, UserDto.class)
                                .map(r -> r.hits().hits())
                                .flatMap(hits -> Mono.justOrEmpty(
                                                hits.stream()
                                                                .findFirst()
                                                                .map(Hit::source)));
        }

        private Flux<ChatDto> searchChatStream(Long idUser, Long idGroup) {

                return fetchPageChat(idUser, idGroup, null)
                                .expand(response -> {

                                        List<Hit<ChatDto>> hits = response.hits().hits();

                                        if (hits.isEmpty() || hits.size() < SIZE_CHAT_BATCH) {
                                                return Mono.empty();
                                        }

                                        Hit<ChatDto> lastHit = hits.get(hits.size() - 1);

                                        List<FieldValue> nextSearchAfter = lastHit.sort();

                                        return fetchPageChat(idUser, idGroup, nextSearchAfter);
                                })
                                .flatMapIterable(r -> r.hits().hits())
                                .map(Hit::source);
        }

        private Mono<ResponseBody<GiftsDto>> fetchPageGifts(Long idFrom, Long idGroup, Instant from,
                        Instant to,
                        List<FieldValue> searchAfter) {
                if (idGroup == null && idFrom == null) {
                        return Mono.empty();
                }
                List<Query> filters = new ArrayList<>();
                if (from != null && to != null) {
                        RangeQuery rangeQuery = RangeQuery.of(r -> r.date(v -> v.field("date")
                                        .gte(from.toString())
                                        .lte(to.toString())));
                        filters.add(rangeQuery._toQuery());
                }

                if (idFrom != null) {
                        TermQuery termQuery = TermQuery.of(t -> t
                                        .field("id_from")
                                        .value(idFrom));
                        filters.add(termQuery._toQuery());
                }

                if (idGroup != null) {
                        TermQuery termQuery = TermQuery.of(t -> t
                                        .field("id_group")
                                        .value(idGroup));
                        filters.add(termQuery._toQuery());
                }

                Query query = Query.of(q -> q
                                .bool(b -> b
                                                .filter(filters)));

                SearchRequest searchRequest = SearchRequest.of(s -> {
                        s.index("gifts_read")
                                        .size(1000)
                                        .query(query)
                                        .sort(so -> so.field(f -> f.field("id").order(SortOrder.Asc)));

                        if (searchAfter != null && !searchAfter.isEmpty()) {
                                s.searchAfter(searchAfter);
                        }

                        return s;
                });

                return elasticSearchPort.search(searchRequest, GiftsDto.class);
        }

        private Mono<ResponseBody<MessagesPropertiesDto>> fetchPageMessagesProperties(Long idGroup, Long idFrom,
                        Instant from,
                        Instant to,
                        List<FieldValue> searchAfter) {
                if (idGroup == null && idFrom == null) {
                        return Mono.empty();
                }
                List<Query> filters = new ArrayList<>();
                if (from != null && to != null) {
                        RangeQuery rangeQuery = RangeQuery.of(r -> r.date(v -> v.field("date")
                                        .gte(from.toString())
                                        .lte(to.toString())));
                        filters.add(rangeQuery._toQuery());
                }

                if (idGroup != null) {
                        TermQuery termQuery = TermQuery.of(t -> t
                                        .field("id_group")
                                        .value(idGroup));
                        filters.add(termQuery._toQuery());
                }

                if (idFrom != null) {
                        TermQuery termQuery = TermQuery.of(t -> t
                                        .field("id_from")
                                        .value(idFrom));
                        filters.add(termQuery._toQuery());
                }

                Query query = Query.of(q -> q
                                .bool(b -> b
                                                .filter(filters)));

                SearchRequest searchRequest = SearchRequest.of(s -> {
                        s.index("messages_properties_read")
                                        .size(1000)
                                        .query(query)
                                        .sort(so -> so.field(f -> f.field("id").order(SortOrder.Asc)));

                        if (searchAfter != null && !searchAfter.isEmpty()) {
                                s.searchAfter(searchAfter);
                        }

                        return s;
                });

                return elasticSearchPort.search(searchRequest, MessagesPropertiesDto.class);
        }

        private Mono<ResponseBody<ReactionsDto>> fetchPageReactions(Long idGroup, Long idUser, Instant from,
                        Instant to,
                        List<FieldValue> searchAfter) {
                if (idGroup == null && idUser == null) {
                        return Mono.empty();
                }
                List<Query> filters = new ArrayList<>();
                if (from != null && to != null) {
                        RangeQuery rangeQuery = RangeQuery.of(r -> r.date(v -> v.field("date")
                                        .gte(from.toString())
                                        .lte(to.toString())));
                        filters.add(rangeQuery._toQuery());
                }

                if (idGroup != null) {
                        TermQuery termQuery = TermQuery.of(t -> t
                                        .field("id_group")
                                        .value(idGroup));
                        filters.add(termQuery._toQuery());
                }

                if (idUser != null) {
                        TermQuery termQuery = TermQuery.of(t -> t
                                        .field("id_user")
                                        .value(idUser));
                        filters.add(termQuery._toQuery());
                }

                Query query = Query.of(q -> q
                                .bool(b -> b
                                                .filter(filters)));

                SearchRequest searchRequest = SearchRequest.of(s -> {
                        s.index("reactions_read")
                                        .size(1000)
                                        .query(query)
                                        .sort(so -> so.field(f -> f.field("id").order(SortOrder.Asc)));

                        if (searchAfter != null && !searchAfter.isEmpty()) {
                                s.searchAfter(searchAfter);
                        }

                        return s;
                });

                return elasticSearchPort.search(searchRequest, ReactionsDto.class);
        }

        private Mono<ResponseBody<ReactionsGeneralDto>> fetchPageReactionsGeneral(Long idGroup, Instant from,
                        Instant to,
                        List<FieldValue> searchAfter) {
                RangeQuery rangeQuery = RangeQuery.of(r -> r.date(v -> v.field("date")
                                .gte(from.toString())
                                .lte(to.toString())));

                TermQuery termQuery = TermQuery.of(t -> t
                                .field("id_group")
                                .value(idGroup));

                Query query = Query.of(q -> q
                                .bool(b -> b
                                                .filter(rangeQuery._toQuery())
                                                .filter(termQuery._toQuery())));

                SearchRequest searchRequest = SearchRequest.of(s -> {
                        s.index("reactions_general_read")
                                        .size(1000)
                                        .query(query)
                                        .sort(so -> so.field(f -> f.field("id").order(SortOrder.Asc)));

                        if (searchAfter != null && !searchAfter.isEmpty()) {
                                s.searchAfter(searchAfter);
                        }

                        return s;
                });

                return elasticSearchPort.search(searchRequest, ReactionsGeneralDto.class);
        }

        private Mono<ResponseBody<ChatDto>> fetchPageChat(
                        Long idUser,
                        Long idGroup,
                        List<FieldValue> searchAfter) {
                if (idGroup == null && idUser == null) {
                        return Mono.empty();
                }
                List<Query> filters = new ArrayList<>();
                if (idUser != null) {
                        TermQuery termQuery = TermQuery.of(t -> t
                                        .field("id_user")
                                        .value(idUser));
                        filters.add(termQuery._toQuery());
                }

                if (idGroup != null) {
                        TermQuery termQuery = TermQuery.of(t -> t
                                        .field("id_group")
                                        .value(idGroup));
                        filters.add(termQuery._toQuery());
                }

                Query query = Query.of(q -> q
                                .bool(b -> b.filter(filters)));

                SearchRequest searchRequest = SearchRequest.of(s -> {
                        s.index("chat_read")
                                        .size(SIZE_CHAT_BATCH)
                                        .query(query)

                                        // сортировка
                                        .sort(so -> so.field(f -> f.field("id").order(SortOrder.Asc)))
                                        .sort(so -> so.field(f -> f.field("_index").order(SortOrder.Asc)));

                        if (searchAfter != null && !searchAfter.isEmpty()) {
                                s.searchAfter(searchAfter);
                        }

                        return s;
                });

                return elasticSearchPort.search(searchRequest, ChatDto.class);
        }

        private Mono<ResponseBody<ParticipantChangedDto>> fetchPageParticipantChanged(
                        Long idGroup, Instant from, Instant to,
                        List<FieldValue> searchAfter) {

                RangeQuery rangeQuery = RangeQuery.of(r -> r.date(v -> v.field("date")
                                .gte(from.toString())
                                .lte(to.toString())));

                TermQuery termQuery = TermQuery.of(t -> t
                                .field("id_group")
                                .value(idGroup));

                Query query = Query.of(q -> q
                                .bool(b -> b
                                                .filter(rangeQuery._toQuery())
                                                .filter(termQuery._toQuery())));

                SearchRequest searchRequest = SearchRequest.of(s -> {
                        s.index("participant_changed_read")
                                        .size(1000)
                                        .query(query)
                                        .sort(so -> so.field(f -> f.field("id").order(SortOrder.Asc)));

                        if (searchAfter != null && !searchAfter.isEmpty()) {
                                s.searchAfter(searchAfter);
                        }

                        return s;
                });

                return elasticSearchPort.search(searchRequest, ParticipantChangedDto.class);
        }
}
