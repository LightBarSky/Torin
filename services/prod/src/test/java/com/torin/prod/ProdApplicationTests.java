package com.torin.prod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ProdApplicationTests {
	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldLoadTestProperties() {
		assertTrue(
				Arrays.asList(environment.getActiveProfiles())
						.contains("test"),
				"Профиль test не активен");

		assertEquals(
				"false",
				environment.getProperty("kafka.enabled"));
	}

}
