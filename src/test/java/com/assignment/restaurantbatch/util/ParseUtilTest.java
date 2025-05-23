package com.assignment.restaurantbatch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ParseUtil} 클래스의 날짜 및 숫자 변환 기능을 검증하는 단위 테스트입니다.
 * <p>
 * • 문자열 → {@link LocalDate}, {@link LocalDateTime}, {@link Integer}, {@link Double} 변환 결과를 확인합니다.
 * • 빈 문자열 또는 null 입력 처리 및 예외 발생 조건도 함께 검증합니다.
 */
class ParseUtilTest {

    @Test
    @DisplayName("yyyy-MM-dd 문자열은 LocalDate로 정상 파싱되어야 한다")
    void testToLocalDate() {
        // given / when / then
        assertThat(ParseUtil.toLocalDate("2023-05-01")).isEqualTo(LocalDate.of(2023, 5, 1));
        assertThat(ParseUtil.toLocalDate("   ")).isNull(); // 공백 입력
        assertThat(ParseUtil.toLocalDate(null)).isNull(); // null 입력
    }

    @Test
    @DisplayName("yyyy-MM-dd HH:mm:ss 문자열은 LocalDateTime으로 정상 파싱되어야 한다")
    void testToLocalDateTime() {
        // given / when / then
        assertThat(ParseUtil.toLocalDateTime("2023-05-01 12:30:00"))
                .isEqualTo(LocalDateTime.of(2023, 5, 1, 12, 30, 0));
        assertThat(ParseUtil.toLocalDateTime("")).isNull(); // 빈 문자열
    }

    @Test
    @DisplayName("정수 문자열은 Integer로 정상 파싱되어야 하며, 공백 또는 잘못된 입력은 예외 처리해야 한다")
    void testParseInt() {
        // given / when / then
        assertThat(ParseUtil.parseInt(" 123 ")).isEqualTo(123); // 공백 포함
        assertThat(ParseUtil.parseInt("")).isNull(); // 빈 문자열
        assertThrows(NumberFormatException.class, () -> ParseUtil.parseInt("abc")); // 비정상 입력
    }

    @Test
    @DisplayName("실수 문자열은 Double로 정상 파싱되어야 하며, 공백 또는 잘못된 입력은 예외 처리해야 한다")
    void testParseDouble() {
        // given / when / then
        assertThat(ParseUtil.parseDouble(" 12.5 ")).isEqualTo(12.5); // 공백 포함
        assertThat(ParseUtil.parseDouble("")).isNull(); // 빈 문자열
        assertThrows(NumberFormatException.class, () -> ParseUtil.parseDouble("x.y")); // 비정상 입력
    }
}
