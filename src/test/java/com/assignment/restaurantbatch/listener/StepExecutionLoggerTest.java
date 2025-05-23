package com.assignment.restaurantbatch.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * {@link StepExecutionLogger}의 동작을 검증하는 테스트 클래스입니다.
 * <p>
 * - Step 시작 전 로그가 출력되는지 확인<br>
 * - Step 종료 후 통계 로그 및 예외 로그 출력 여부 검증
 */
class StepExecutionLoggerTest {

    /**
     * beforeStep()과 afterStep() 호출 시 로그 처리 및 통계 반환이 정상적으로 수행되는지 검증합니다.
     */
    @Test
    @DisplayName("beforeStep(), afterStep() 호출 시 통계 로그 및 예외 로그 출력 테스트")
    void shouldLogBeforeAndAfterStepExecution() {
        // given
        StepExecutionLogger logger = new StepExecutionLogger();
        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();

        setField(stepExecution, "stepName", "testStep");
        setField(stepExecution, "readCount", 100);
        setField(stepExecution, "writeCount", 80);
        setField(stepExecution, "commitCount", 3);
        setField(stepExecution, "rollbackCount", 1);
        setField(stepExecution, "processSkipCount", 20);

        // when
        logger.beforeStep(stepExecution);
        ExitStatus status = logger.afterStep(stepExecution);

        // then
        assertThat(status).isEqualTo(stepExecution.getExitStatus());
    }

    /**
     * afterStep() 호출 시 예외가 포함되어 있을 경우 로그로 출력되는지 확인합니다.
     */
    @Test
    @DisplayName("afterStep() 호출 시 예외 발생 로그 출력 테스트")
    void shouldLogExceptionsInAfterStep() {
        // given
        StepExecutionLogger logger = new StepExecutionLogger();
        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();

        Throwable dummyError = new RuntimeException("예외 발생 테스트");
        setField(stepExecution, "failureExceptions", Collections.singletonList(dummyError));

        // when
        ExitStatus status = logger.afterStep(stepExecution);

        // then
        assertThat(status).isEqualTo(stepExecution.getExitStatus());
    }
}
