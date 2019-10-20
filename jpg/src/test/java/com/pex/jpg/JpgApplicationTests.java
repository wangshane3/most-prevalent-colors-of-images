package com.pex.jpg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JpgApplicationTests {
	@Test
	void contextLoads() {
	}

	@Test
	void find3Max() {
		assert "0x000004,0x000005,0x000007"
				.equals(JpgApplication.findKmax(new int[] { 2, 3, 3, 5, 3, 4, 1, 7, 3, 4 }, 3));
	}
}