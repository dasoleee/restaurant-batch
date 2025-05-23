package com.assignment.restaurantbatch.job;

import com.assignment.restaurantbatch.util.BatchTuner;
import com.assignment.restaurantbatch.util.CsvSplitter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Spring Boot 시작 시 자동 실행되는 배치 작업 런처 설정입니다.
 * <p>
 * - 입력 CSV의 전체 라인 수를 계산하여 적절한 배치 설정을 튜닝하고, <br>
 * - CSV를 여러 파일로 분할한 후, <br>
 * - 파라미터를 구성하여 Spring Batch Job을 실행합니다.
 */
@Configuration
@RequiredArgsConstructor
public class RestaurantJobLauncher {

    private final JobLauncher jobLauncher;
    private final Job restaurantPartitionedJob;
    private final BatchTuner batchTuner;
    private final CsvSplitter csvSplitter;

    /**
     * Spring Boot 실행 시 자동으로 실행되는 배치 Job Runner입니다.
     * @return ApplicationRunner 인스턴스
     */
    @Bean
    @Profile("!test")
    public ApplicationRunner jobRunner() {
        return args -> {
            Path inputPath = getInputCsvPath();
            Path partitionPath = getPartitionDirPath();

            // 전체 라인 수 계산
            long totalLines;
            try (BufferedReader reader = Files.newBufferedReader(inputPath, Charset.forName("MS949"))) {
                totalLines = reader.lines().count();
            }

            var config = batchTuner.tune((int) totalLines);

            // CSV 분할
            csvSplitter.split(inputPath, partitionPath.toString(), config.linesPerFile());

            // 실패 로그 파일 경로
            String formattedTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addLong("linesPerFile", (long) config.linesPerFile())
                    .addLong("gridSize", (long) config.gridSize())
                    .addLong("chunkSize", (long) config.chunkSize())
                    .addString("partitionDir", partitionPath.toString())
                    .addString("failureLog", "data/failure/failed-" + formattedTime + ".csv")
                    .toJobParameters();

            jobLauncher.run(restaurantPartitionedJob, jobParameters);
        };
    }

    /**
     * 입력 CSV 파일 경로 반환 (테스트 오버라이드 가능)
     */
    protected Path getInputCsvPath() {
        return Paths.get("data/restaurant.csv");
    }

    /**
     * 파티션 디렉토리 경로 반환 (테스트 오버라이드 가능)
     */
    protected Path getPartitionDirPath() {
        return Paths.get("data/partitioned");
    }
}
