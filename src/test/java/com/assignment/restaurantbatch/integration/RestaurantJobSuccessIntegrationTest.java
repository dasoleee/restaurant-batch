package com.assignment.restaurantbatch.integration;

import com.assignment.restaurantbatch.RestaurantBatchApplication;
import com.assignment.restaurantbatch.util.CsvSplitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code RestaurantJobSuccessIntegrationTest}는 Spring Batch 기반 음식점 데이터 처리 Job의
 * 정상 흐름을 검증하는 통합 테스트입니다.
 *
 * <p>테스트 항목:
 * <ul>
 *   <li>정상 CSV 입력 파일이 주어졌을 때 Job이 COMPLETED 상태로 종료되는지</li>
 *   <li>실패 로그 파일이 생성되지 않거나, 비어 있는지</li>
 *   <li>(선택) DB에 데이터가 실제로 저장되었는지</li>
 * </ul>
 *
 * <p>테스트는 H2 인메모리 데이터베이스를 사용하며, {@code testSchema.sql} 스크립트를 통해
 * {@code restaurant} 테이블이 사전 생성되어야 합니다.
 */
@SpringBootTest(classes = RestaurantBatchApplication.class)
@SpringBatchTest
@ActiveProfiles("test")  // src/test/resources/application-test.yml 적용
class RestaurantJobSuccessIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job restaurantPartitionedJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 임시 디렉토리 경로. 테스트 중 생성되는 입력 CSV, 로그 파일 등이 저장됩니다.
     */
    @TempDir
    Path tempDir;

    @BeforeEach
    void truncateRestaurantTable() {
        jdbcTemplate.execute("TRUNCATE TABLE restaurant");
    }

    /**
     * 정상 CSV 파일을 입력으로 주었을 때:
     * <ul>
     *     <li>Batch Job이 COMPLETED 상태로 정상 종료</li>
     *     <li>실패 로그 파일이 없거나 header만 포함</li>
     *     <li>(선택) DB에 입력된 데이터 개수 확인</li>
     * </ul>
     *
     * @throws Exception 예외 발생 시 테스트 실패
     */
    @Test
    @DisplayName("정상 CSV 입력 시 Job 성공 및 실패 로그 없음")
    void testSuccessfulCsvProcessing() throws Exception {
        // GIVEN: 테스트용 CSV 복사
        Path inputCsv = tempDir.resolve("restaurant.csv");
        Files.copy(
                new ClassPathResource("success-test.csv").getInputStream(),
                inputCsv,
                StandardCopyOption.REPLACE_EXISTING
        );

        // 분할 디렉토리 및 실패 로그 파일 경로 지정
        String partitionDir = tempDir.resolve("partitioned").toString();
        String failureLog = tempDir.resolve("failures.csv").toString();

        // 디렉토리 생성 + CSV 분할
        Files.createDirectories(Path.of(partitionDir));
        new CsvSplitter().split(inputCsv, partitionDir, 3);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("partitionDir", partitionDir)
                .addLong("linesPerFile", 3L)
                .addLong("gridSize", 2L)
                .addLong("chunkSize", 2L)
                .addString("failureLog", failureLog)
                .toJobParameters();

        // WHEN: Job 실행
        JobExecution jobExecution = jobLauncherTestUtils.getJobLauncher()
                .run(restaurantPartitionedJob, jobParameters);

        // THEN: Job은 COMPLETED 상태여야 함
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 모든 Step 상태 확인
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        }

        // 실패 로그 파일이 없거나 header만 있어야 함
        Path failureLogPath = Path.of(failureLog);
        if (Files.exists(failureLogPath)) {
            long lineCount = Files.lines(failureLogPath).count();
            assertThat(lineCount).isLessThanOrEqualTo(1); // header 또는 없음
        }

        // DB 데이터 삽입 확인
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM restaurant", Integer.class);
        assertThat(count).isEqualTo(6);

        // 특정 레코드 필드값 확인 (정합성 테스트)
        String storeName = jdbcTemplate.queryForObject(
                "SELECT region_code FROM restaurant WHERE record_number = 1",
                String.class
        );
        assertThat(storeName).isEqualTo("3250000");
    }
}
