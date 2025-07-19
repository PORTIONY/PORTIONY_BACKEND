package com.portiony.portiony;

import io.github.cdimascio.dotenv.Dotenv; // ✅ 이거 추가!
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PortionyApplication {

	public static void main(String[] args) {

		// .env 파일 로딩
		Dotenv dotenv = Dotenv.load();
		System.setProperty("KAKAO_REST_API_KEY", dotenv.get("KAKAO_REST_API_KEY"));
		System.setProperty("JWT_SECRET_KEY", dotenv.get("JWT_SECRET_KEY"));

		SpringApplication.run(PortionyApplication.class, args);
	}

}
