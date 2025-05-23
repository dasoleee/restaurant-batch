package com.assignment.restaurantbatch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BatchTuner} 클래스의 단위 테스트입니다.
 * <p>
 * • 총 CSV 라인 수에 따라 적절한 파티셔닝 설정이 계산되는지 검증합니다.
 * • linesPerFile, chunkSize, gridSize 등의 튜닝 로직이 정상 동작하는지 확인합니다.
 */
class BatchTunerTest {

    @Test
    @DisplayName("10만 줄 미만의 데이터에 대해 적절한 설정이 반환되어야 한다")
    void shouldReturnConfigForLessThan100KLines() {
        // given
        int totalLines = 80_000;

        // when
        BatchTuner.BatchConfig config = new BatchTuner().tune(totalLines);

        // then
        assertThat(config.linesPerFile()).isEqualTo(20_000);
        assertThat(config.chunkSize()).isEqualTo(500);
        assertThat(config.gridSize()).isBetween(4, 32); // gridSize는 CPU 수를 기반으로 제한됨
    }

    @Test
    @DisplayName("10만 이상 100만 미만의 데이터에 대해 적절한 설정이 반환되어야 한다")
    void shouldReturnConfigForBetween100KAnd1MillionLines() {
        // given
        int totalLines = 500_000;

        // when
        BatchTuner.BatchConfig config = new BatchTuner().tune(totalLines);

        // then
        assertThat(config.linesPerFile()).isEqualTo(50_000);
        assertThat(config.chunkSize()).isEqualTo(1000);
        assertThat(config.gridSize()).isBetween(4, 32);
    }

    @Test
    @DisplayName("100만 줄 이상의 데이터에 대해 적절한 설정이 반환되어야 한다")
    void shouldReturnConfigForMoreThan1MillionLines() {
        // given
        int totalLines = 2_000_000;

        // when
        BatchTuner.BatchConfig config = new BatchTuner().tune(totalLines);

        // then
        assertThat(config.linesPerFile()).isEqualTo(100_000);
        assertThat(config.chunkSize()).isEqualTo(2000);
        assertThat(config.gridSize()).isBetween(4, 32);
    }
}
