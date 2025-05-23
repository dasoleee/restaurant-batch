package com.assignment.restaurantbatch.policy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CustomSkipPolicy} 클래스의 {@code shouldSkip()} 메서드에 대한 단위 테스트입니다.
 * <p>
 * • 다양한 예외 유형에 대해 스킵 여부가 올바르게 판단되는지 검증합니다.
 * • 허용된 예외는 true, 그렇지 않은 예외는 false를 반환해야 합니다.
 */
class CustomSkipPolicyTest {

    private CustomSkipPolicy skipPolicy;

    @BeforeEach
    void setUp() {
        skipPolicy = new CustomSkipPolicy();
    }

    @Test
    @DisplayName("FlatFileParseException은 skip 처리해야 한다")
    void shouldSkipForFlatFileParseException() throws Exception {
        // given
        FlatFileParseException exception = new FlatFileParseException("Invalid line", "bad,data,line", 3);

        // when
        boolean result = skipPolicy.shouldSkip(exception, 0);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("NumberFormatException은 skip 처리해야 한다")
    void shouldSkipForNumberFormatException() throws Exception {
        // when
        boolean result = skipPolicy.shouldSkip(new NumberFormatException("NaN"), 0);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("DateTimeParseException은 skip 처리해야 한다")
    void shouldSkipForDateTimeParseException() throws Exception {
        // when
        boolean result = skipPolicy.shouldSkip(new DateTimeParseException("Invalid date", "2023-13-40", 0), 0);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("TransientDataAccessException은 skip 처리해야 한다")
    void shouldSkipForTransientDataAccessException() throws Exception {
        // when
        boolean result = skipPolicy.shouldSkip(new TransientDataAccessException("DB issue") {}, 0);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("CannotGetJdbcConnectionException은 skip 처리해야 한다")
    void shouldSkipForCannotGetJdbcConnectionException() throws Exception {
        // when
        boolean result = skipPolicy.shouldSkip(new CannotGetJdbcConnectionException("Connection failed"), 0);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SQLException은 skip 처리해야 한다")
    void shouldSkipForSqlException() throws Exception {
        // when
        boolean result = skipPolicy.shouldSkip(new SQLException("SQL error"), 0);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("FileNotFoundException은 skip 처리하지 않아야 한다")
    void shouldNotSkipForFileNotFoundException() throws Exception {
        // when
        boolean result = skipPolicy.shouldSkip(new FileNotFoundException("File not found"), 0);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("UncheckedIOException은 skip 처리하지 않아야 한다")
    void shouldNotSkipForUncheckedIOException() throws Exception {
        // when
        boolean result = skipPolicy.shouldSkip(new UncheckedIOException("IO error", new IOException()), 0);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("VirtualMachineError는 skip 처리하지 않아야 한다")
    void shouldNotSkipForVirtualMachineError() throws Exception {
        // when
        boolean result = skipPolicy.shouldSkip(new OutOfMemoryError("Out of memory"), 0);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("알 수 없는 예외는 skip 처리해야 한다")
    void shouldSkipForUnknownException() throws Exception {
        // when
        boolean result = skipPolicy.shouldSkip(new IllegalStateException("Something went wrong"), 0);

        // then
        assertThat(result).isTrue();
    }
}
