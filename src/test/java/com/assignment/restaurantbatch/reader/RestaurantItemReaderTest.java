package com.assignment.restaurantbatch.reader;

import com.assignment.restaurantbatch.dto.RestaurantCsvDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FlatFileItemReader}를 통해 CSV 파일이 {@link RestaurantCsvDto}로
 * 정상적으로 매핑되는지 검증하는 단위 테스트입니다.
 * <p>
 * • 특정 필드가 CSV의 각 라인에서 올바르게 파싱되어 DTO에 설정되는지 확인합니다.
 * • 클래스패스 리소스를 기준으로 CSV를 읽어들입니다.
 */
class RestaurantItemReaderTest {

    @Test
    @DisplayName("CSV 라인을 RestaurantCsvDto로 정상 매핑하는지 테스트")
    void shouldMapCsvLineToDtoCorrectly() throws Exception {
        // given: 클래스패스 리소스를 사용하는 CSV 리더 설정
        FlatFileItemReader<RestaurantCsvDto> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("success-test.csv"));
        reader.setLinesToSkip(1); // 헤더 건너뛰기
        reader.setLineMapper((line, lineNumber) -> {
            String[] fields = line.split(",", -1);
            RestaurantCsvDto dto = new RestaurantCsvDto();
            dto.setRegionCode(fields[3]);
            dto.setLicenseDate(fields[5]);
            return dto;
        });

        // when: 리더 실행
        reader.open(new ExecutionContext());
        RestaurantCsvDto result = reader.read();
        reader.close();

        // then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getRegionCode()).isEqualTo("\"3250000\"");
        assertThat(result.getLicenseDate()).isEqualTo("\"2024-12-02\"");
    }
}
