package org.movieproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing  //  BaseEntity EntityListeners 설정 시 이 어노테이션을 명시해줘야 함
public class MovieProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieProjectApplication.class, args);
    }

}
