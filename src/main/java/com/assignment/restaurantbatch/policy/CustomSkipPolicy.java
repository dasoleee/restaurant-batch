package com.assignment.restaurantbatch.policy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;

/**
 * Spring Batch의 SkipPolicy 구현체입니다.
 * <p>
 * 예외 발생 시, 그 예외가 스킵 가능한 유형인지 판단하여 true/false를 반환합니다.<br>
 * - CSV 포맷 오류, 날짜/숫자 파싱 오류, 일시적인 DB 오류는 스킵 허용<br>
 * - 치명적인 시스템 오류나 파일 시스템 오류는 스킵 불가<br>
 */
@Slf4j
public class CustomSkipPolicy implements SkipPolicy {

    /**
     * 발생한 예외가 스킵 가능한 예외인지 여부를 반환합니다.
     *
     * @param t 발생한 예외
     * @param skipCount 현재까지 스킵된 항목 수
     * @return 스킵 가능하면 true, 중단해야 하면 false
     */
    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {

        if (t instanceof VirtualMachineError) {
            log.error("치명적인 시스템 오류 발생 - 즉시 중단: {}", t.getClass().getSimpleName());
            return false;
        }

        if (t instanceof FileNotFoundException || t instanceof UncheckedIOException) {
            log.error("파일 시스템 오류 발생 - 스킵 불가: {}", t.getMessage());
            return false;
        }

        if (t instanceof FlatFileParseException e) {
            log.warn("잘못된 CSV 포맷 - 스킵 처리 (line={}): {}", e.getLineNumber(), e.getInput());
            return true;
        }

        if (t instanceof NumberFormatException) {
            log.warn("숫자 파싱 실패 - 스킵 처리: {}", t.getMessage());
            return true;
        }

        if (t instanceof DateTimeParseException) {
            log.warn("날짜 파싱 실패 - 스킵 처리: {}", t.getMessage());
            return true;
        }

        if (t instanceof TransientDataAccessException
                || t instanceof CannotGetJdbcConnectionException
                || t instanceof SQLException) {
            log.warn("일시적 SQL 오류 또는 JDBC 오류 - 스킵 처리: {}", t.getMessage());
            return true;
        }

        log.error("정의되지 않은 예외 - 스킵 처리 ({}): {}", t.getClass().getSimpleName(), t.getMessage());
        return true;
    }
}
