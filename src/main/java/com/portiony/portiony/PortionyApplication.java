package com.portiony.portiony;

//import io.github.cdimascio.dotenv.Dotenv; // ✅ 이거 추가!
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PortionyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortionyApplication.class, args);
	}

}
