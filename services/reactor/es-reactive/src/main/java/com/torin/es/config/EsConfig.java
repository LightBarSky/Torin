package com.torin.es.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;

import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.ClientConfiguration.MaybeSecureClientConfigurationBuilder;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EsProperties.class)
@EnableReactiveElasticsearchRepositories
public class EsConfig extends ReactiveElasticsearchConfiguration {
	private final EsProperties properties;

	public EsConfig(EsProperties properties) {
		this.properties = properties;
	}

	@Bean
	@Override
	public ClientConfiguration clientConfiguration() {
		MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
				.connectedTo(properties.getUris());
		if (properties.getHttps()) {
			builder.usingSsl();
		}
		if (properties.getUsername() != null && !properties.getUsername().isEmpty()
				&& properties.getPassword() != null) {
			builder.withBasicAuth(properties.getUsername(), properties.getPassword());
		}
		return builder.build();
	}

	@Bean
	@Override
	public JsonpMapper jsonpMapper() {
		ObjectMapper mapper = (new ObjectMapper()).configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.registerModule(new JavaTimeModule());
		mapper.findAndRegisterModules();

		JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(mapper);
		return jsonpMapper;
	}
}
