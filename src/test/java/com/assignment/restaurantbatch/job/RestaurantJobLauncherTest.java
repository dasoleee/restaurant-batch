package com.assignment.restaurantbatch.job;

import com.assignment.restaurantbatch.util.BatchTuner;
import com.assignment.restaurantbatch.util.CsvSplitter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * {@link RestaurantJobLauncher} 클래스의 단위 테스트입니다.
 * <p>
 * - 클래스패스 리소스 기반 입력 CSV 사용<br>
 * - 분할기 및 파라미터 처리 검증
 */
class RestaurantJobLauncherTest {

    @Test
    @DisplayName("jobRunner는 클래스패스 CSV를 분할하고 Job을 실행해야 한다")
    void testJobRunnerLaunchesJobWithClasspathCsv(@TempDir Path tempDir) throws Exception {
        // given: 목 객체 생성
        JobLauncher jobLauncher = mock(JobLauncher.class);
        Job job = mock(Job.class);
        BatchTuner batchTuner = mock(BatchTuner.class);
        CsvSplitter csvSplitter = mock(CsvSplitter.class);

        // 리소스 복사
        Path inputCsv = tempDir.resolve("restaurant.csv");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("success-test.csv")) {
            Files.copy(Objects.requireNonNull(in), inputCsv, StandardCopyOption.REPLACE_EXISTING);
        }

        Path partitionDir = tempDir.resolve("partitioned");
        Files.createDirectories(partitionDir);

        // RestaurantJobLauncher를 익명 클래스 형태로 오버라이드
        RestaurantJobLauncher launcher = new RestaurantJobLauncher(jobLauncher, job, batchTuner, csvSplitter) {
            @Override
            protected Path getInputCsvPath() {
                return inputCsv;
            }

            @Override
            protected Path getPartitionDirPath() {
                return partitionDir;
            }
        };

        // BatchTuner가 반환할 설정
        when(batchTuner.tune(anyInt())).thenReturn(new BatchTuner.BatchConfig(2, 2, 2));

        // when: jobRunner 실행
        launcher.jobRunner().run(null);

        // then: 호출 검증
        verify(csvSplitter).split(eq(inputCsv), eq(partitionDir.toString()), eq(2));

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(job), captor.capture());

        JobParameters params = captor.getValue();
        assertThat(params.getString("partitionDir")).isEqualTo(partitionDir.toString());
        assertThat(params.getLong("chunkSize")).isEqualTo(2L);
    }
}
