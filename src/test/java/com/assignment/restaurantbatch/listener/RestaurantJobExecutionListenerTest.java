package com.assignment.restaurantbatch.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * {@link RestaurantJobExecutionListener}의 기능 테스트 클래스입니다.
 * <p>
 * - 잡 실행 후 통계 로그 출력 및 실패율 경고 검증<br>
 * - 임시 파티션 파일 정리 기능 검증
 */
class RestaurantJobExecutionListenerTest {

    /**
     * afterJob 메서드가 임시 디렉토리를 정상적으로 삭제하는지 테스트합니다.
     */
    @Test
    @DisplayName("afterJob() 호출 시 파티션 디렉토리 내 restaurant-part-*.csv 파일만 삭제되는지 테스트")
    void shouldCleanUpPartitionFilesAfterJob() throws IOException {
        // 임시 디렉토리 생성
        Path tempDir = Files.createTempDirectory("partition-test");
        Path tempFile1 = Files.createFile(tempDir.resolve("restaurant-part-001.csv"));
        Path tempFile2 = Files.createFile(tempDir.resolve("restaurant-part-002.csv"));
        Path otherFile = Files.createFile(tempDir.resolve("unrelated-file.csv"));

        // JobExecution 생성 및 파라미터 주입
        JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
        setField(jobExecution, "jobParameters", new JobParametersBuilder()
                .addString("partitionDir", tempDir.toString())
                .toJobParameters());

        // 시작 및 종료 시간 설정
        setField(jobExecution, "startTime", LocalDateTime.now());
        setField(jobExecution, "endTime", LocalDateTime.now());

        // StepExecution 더미 추가 (통계 출력 확인용)
        StepExecution step = new StepExecution("step1", jobExecution);
        setField(step, "readCount", 100);
        setField(step, "writeCount", 90);
        setField(step, "commitCount", 5);
        setField(step, "rollbackCount", 1);
        setField(step, "processSkipCount", 10);
        jobExecution.addStepExecutions(List.of(step));

        // 실행
        RestaurantJobExecutionListener listener = new RestaurantJobExecutionListener();
        listener.afterJob(jobExecution);

        // 검증: restaurant-part-*.csv는 삭제되고, unrelated-file.csv는 남아 있어야 함
        assertThat(Files.exists(tempFile1)).isFalse();
        assertThat(Files.exists(tempFile2)).isFalse();
        assertThat(Files.exists(otherFile)).isTrue();

        // 디렉토리는 남아 있어야 함 (삭제 대상 아님)
        assertThat(Files.exists(tempDir)).isTrue();

        // 정리
        Files.deleteIfExists(otherFile);
        Files.deleteIfExists(tempDir);
    }
}
