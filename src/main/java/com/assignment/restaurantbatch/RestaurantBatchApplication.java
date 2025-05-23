package com.assignment.restaurantbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 기반의 Restaurant Batch 애플리케이션의 진입점 클래스입니다.
 * <p>
 * 애플리케이션 실행 시, ApplicationRunner에 등록된 Job이 자동 실행됩니다.
 */
@SpringBootApplication
public class RestaurantBatchApplication {

    /**
     * 애플리케이션 시작 메서드
     */
    public static void main(String[] args) {
        SpringApplication.run(RestaurantBatchApplication.class, args);
    }
}
