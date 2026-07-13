package com.torin.dbService;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.DockerClientFactory;

@Disabled
@SpringBootTest
public class CheckDockerTest {
    
	@Test
	public void checkDocker() {
		System.out.println(DockerClientFactory.instance().isDockerAvailable());
	}

}
