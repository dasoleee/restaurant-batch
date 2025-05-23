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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code RestaurantJobFailureIntegrationTest}는 잘못된 CSV 데이터가 포함된 경우
 * Spring Batch Job이 실패한 레코드를 건너뛰고 Job 전체는 성공적으로 완료되는지 검증합니다.
 *
 * 테스트 항목:
 * - 잘못된 CSV 입력이 일부 포함되어도 Job이 COMPLETED 상태로 종료되는지
 * - 실패한 행이 실패 로그 파일에 기록되는지
 * - 성공한 행만 DB에 저장되는지
 */
@SpringBootTest(classes = RestaurantBatchApplication.class)
@SpringBatchTest
@ActiveProfiles("test")
class RestaurantJobFailureIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job restaurantPartitionedJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @TempDir
    Path tempDir;

    @BeforeEach
    void truncateRestaurantTable() {
        jdbcTemplate.execute("TRUNCATE TABLE restaurant");
    }

    @Test
    @DisplayName("실패 데이터 포함 시 Job은 성공하고 일부 레코드는 스킵됨")
    void testCsvWithInvalidRows() throws Exception {
        // GIVEN: 실패 케이스 CSV 복사
        Path inputCsv = tempDir.resolve("restaurant_failure.csv");
        Files.copy(
                new ClassPathResource("failure-test.csv").getInputStream(),
                inputCsv,
                StandardCopyOption.REPLACE_EXISTING
        );

        String partitionDir = tempDir.resolve("partitioned").toString();
        String failureLog = tempDir.resolve("failures.csv").toString();

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

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM restaurant");
        rows.forEach(System.out::println);


        // THEN: Job은 COMPLETED 상태여야 함
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 실패 로그 파일 존재 여부 및 실패 행 수 확인 (2건)
        Path failureLogPath = Path.of(failureLog);
        assertThat(Files.exists(failureLogPath)).isTrue();
        long lineCount = Files.lines(failureLogPath).count();
        assertThat(lineCount).isEqualTo(3); // header + 2 rows

        // DB에 저장된 성공 행 수 확인
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM restaurant", Integer.class);
        assertThat(count).isEqualTo(4); // 유효한 1, 2, 3, 4번 행만 저장
    }
}
