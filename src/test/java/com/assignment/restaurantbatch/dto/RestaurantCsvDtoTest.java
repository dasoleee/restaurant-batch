package com.assignment.restaurantbatch.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RestaurantCsvDto} 클래스의 단위 테스트입니다.
 * 각 필드의 Getter/Setter 메서드 동작을 검증합니다.
 */
class RestaurantCsvDtoTest {

    @Test
    @DisplayName("RestaurantCsvDto의 Getter 및 Setter가 정상 동작해야 한다")
    void testGettersAndSetters() {
        // given
        RestaurantCsvDto dto = new RestaurantCsvDto();

        // when
        dto.setRecordNumber(1);
        dto.setServiceName("음식점서비스");
        dto.setServiceId("SVC123");
        dto.setRegionCode("11000");
        dto.setManagementNumber("MGMT-001");
        dto.setLicenseDate("20210101");
        dto.setCancelDate("20211231");

        // then
        assertThat(dto.getRecordNumber()).isEqualTo(1);
        assertThat(dto.getServiceName()).isEqualTo("음식점서비스");
        assertThat(dto.getServiceId()).isEqualTo("SVC123");
        assertThat(dto.getRegionCode()).isEqualTo("11000");
        assertThat(dto.getManagementNumber()).isEqualTo("MGMT-001");
        assertThat(dto.getLicenseDate()).isEqualTo("20210101");
        assertThat(dto.getCancelDate()).isEqualTo("20211231");
    }
}
