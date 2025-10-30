package com.ase.dms;

import com.ase.dms.config.TestJodConverterConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJodConverterConfig.class)
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
