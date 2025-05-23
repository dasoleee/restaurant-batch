package com.assignment.restaurantbatch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * 각 Step의 실행 전후 로그를 출력하는 리스너 클래스입니다.
 * <p>
 * - 슬레이브 스텝의 시작/종료 로그 기록<br>
 * - 처리 건수, 커밋/롤백 수, 예외 로그 출력
 */
@Slf4j
public class StepExecutionLogger implements StepExecutionListener {

    /**
     * Step 실행 시작 시 호출됩니다.
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step 시작: {}", stepExecution.getStepName());
    }

    /**
     * Step 실행 종료 후 호출되며 통계 및 예외를 로그로 출력합니다.
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step 완료: {}, read={}, write={}, skip={}, commit={}, rollback={}",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount(),
                stepExecution.getCommitCount(),
                stepExecution.getRollbackCount());

        if (!stepExecution.getFailureExceptions().isEmpty()) {
            for (Throwable e : stepExecution.getFailureExceptions()) {
                log.error("예외 발생: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        return stepExecution.getExitStatus();
    }
}
