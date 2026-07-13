package com.torin.postgres.service;

import com.torin.core.dto.WordGroupAllDto;
import com.torin.postgres.helper.Mapper;
import com.torin.postgres.port.WordGroupAllPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WordGroupAllService {

        private WordGroupAllPort wordGroupAllPort;

        public WordGroupAllService(WordGroupAllPort wordGroupAllPort) {
                this.wordGroupAllPort = wordGroupAllPort;
        }

        public Mono<WordGroupAllDto> findByIdGroup(Long idGroup) {
                return wordGroupAllPort.findByIdGroup(idGroup)
                                .map(Mapper::mapperToDto);
        }

        public Flux<WordGroupAllDto> findByFindGroup(String findGroup) {
                return wordGroupAllPort.findByFindGroup(findGroup)
                                .filter(wg -> wg.getIdGroup() != null)
                                .map(Mapper::mapperToDto);
        }

        public Flux<WordGroupAllDto> findByIdGroupIn(Long[] ids) {
                return wordGroupAllPort.findByIdGroupIn(ids)
                                .map(Mapper::mapperToDto);
        }
}
