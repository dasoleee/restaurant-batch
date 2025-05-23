package com.assignment.restaurantbatch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Spring Batch Job의 실행 전후 동작을 정의하는 리스너입니다.
 * <p>
 * - Job 통계 로그 출력<br>
 * - 실패율 분석 및 경고<br>
 * - 스텝별 처리 결과 요약<br>
 * - 파티션 파일 정리 수행
 */
@Slf4j
public class RestaurantJobExecutionListener implements JobExecutionListener {

    /**
     * Job 실행 전 로그를 출력합니다.
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("배치 작업 시작: {}", jobExecution.getJobInstance().getJobName());
    }

    /**
     * Job 실행 후 통계 로그 및 파티션 파일 정리를 수행합니다.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        List<StepExecution> steps = jobExecution.getStepExecutions().stream()
                .filter(step -> !step.getStepName().equals("masterStep"))
                .toList();

        long totalRead = steps.stream().mapToLong(StepExecution::getReadCount).sum();
        long totalWrite = steps.stream().mapToLong(StepExecution::getWriteCount).sum();
        long totalSkip = steps.stream().mapToLong(StepExecution::getSkipCount).sum();
        long totalCommit = steps.stream().mapToLong(StepExecution::getCommitCount).sum();
        long totalRollback = steps.stream().mapToLong(StepExecution::getRollbackCount).sum();

        log.info("[잡 통계 요약]");
        log.info("총 처리 건수: {}", totalRead);
        log.info("성공 건수 (write): {}", totalWrite);
        log.info("실패 건수 (스킵): {}", totalSkip);
        log.info("커밋 횟수: {}, 롤백 횟수: {}", totalCommit, totalRollback);

        Instant startInstant = Objects.requireNonNull(jobExecution.getStartTime()).atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = Objects.requireNonNull(jobExecution.getEndTime()).atZone(ZoneId.systemDefault()).toInstant();

        long durationMillis = Duration.between(startInstant, endInstant).toMillis();
        log.info("실행 시간: {} ms", durationMillis);

        String failureFile = jobExecution.getJobParameters().getString("failureLog");
        if (failureFile != null) {
            log.info("실패 라인 기록 파일: {}", failureFile);
        }

        double failRate = (totalRead == 0) ? 0.0 : (totalSkip * 100.0 / totalRead);
        if (failRate > 5.0) {
            log.warn("실패율 {}% - 5% 초과", String.format("%.2f", failRate));
        } else {
            log.warn("실패율 {}%", String.format("%.2f", failRate));
        }

        log.info("[스텝별 통계]");
        for (StepExecution step : steps) {
            log.info(" - {} | read={}, write={}, skip={}, commit={}, rollback={}",
                    step.getStepName(),
                    step.getReadCount(),
                    step.getWriteCount(),
                    step.getSkipCount(),
                    step.getCommitCount(),
                    step.getRollbackCount()
            );
        }

        cleanUpPartitionFiles(jobExecution);

        log.info("배치 작업 종료: {}", jobExecution.getJobInstance().getJobName());
    }

    /**
     * 파티션 작업 후 생성된 임시 CSV 파일 및 디렉토리를 안전하게 삭제합니다.
     * - restaurant-part- 로 시작하는 파일만 삭제
     * - 디렉토리가 비었을 경우에만 삭제
     */
    private void cleanUpPartitionFiles(JobExecution jobExecution) {
        String partitionDir = jobExecution.getJobParameters().getString("partitionDir");
        if (partitionDir == null) return;

        Path dirPath = Paths.get(partitionDir);
        if (!Files.exists(dirPath)) return;

        // 1. restaurant-part-* 파일 삭제
        try (Stream<Path> files = Files.list(dirPath)) {
            files.filter(path -> path.getFileName().toString().startsWith("restaurant-part-"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                            log.debug("삭제된 임시 파일: {}", path);
                        } catch (IOException e) {
                            log.warn("임시 파일 삭제 실패: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("파일 리스트 중 오류", e);
        }

        // 2. 디렉토리가 비었을 경우에만 삭제
        try (Stream<Path> remaining = Files.list(dirPath)) {
            if (remaining.findAny().isEmpty()) {
                Files.deleteIfExists(dirPath);
                log.info("임시 디렉토리 삭제 완료: {}", dirPath);
            } else {
                log.warn("디렉토리에 삭제되지 않은 파일이 남아 있어 디렉토리 삭제 생략: {}", dirPath);
            }
        } catch (IOException e) {
            log.error("디렉토리 삭제 중 오류", e);
        }
    }
}
