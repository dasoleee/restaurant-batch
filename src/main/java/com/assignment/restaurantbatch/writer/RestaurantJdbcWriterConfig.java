package com.assignment.restaurantbatch.writer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * MultiInsertWriter를 Spring Bean으로 등록하는 설정 클래스입니다.
 * <p>
 * 슬레이브 스텝에서 DB 저장을 담당하는 Writer로 사용됩니다.
 */
@Configuration
public class RestaurantJdbcWriterConfig {

    /**
     * DB 연결을 기반으로 하는 MultiInsertWriter Bean 등록
     *
     * @param dataSource Spring에서 관리하는 DataSource
     * @return MultiInsertWriter 인스턴스
     */
    @Bean
    public MultiInsertWriter restaurantItemWriter(DataSource dataSource) {
        return new MultiInsertWriter(dataSource);
    }
}
