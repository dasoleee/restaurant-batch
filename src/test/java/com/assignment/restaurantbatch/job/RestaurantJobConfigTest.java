package com.assignment.restaurantbatch.job;

import com.assignment.restaurantbatch.dto.RestaurantCsvDto;
import com.assignment.restaurantbatch.listener.RestaurantSkipListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * {@link RestaurantJobConfig} 설정 클래스에 대한 단위 테스트입니다.
 * Job, Step, Listener 빈이 정상적으로 생성되는지 확인합니다.
 */
class RestaurantJobConfigTest {

    @Test
    @DisplayName("restaurantPartitionedJob은 Job 인스턴스를 반환해야 한다")
    void testRestaurantPartitionedJobCreation() {
        // given
        JobRepository jobRepository = mock(JobRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        ObjectProvider<FlatFileItemReader<RestaurantCsvDto>> readerProvider = mock(ObjectProvider.class);

        Step mockMasterStep = mock(Step.class);
        RestaurantJobConfig config = new RestaurantJobConfig(jobRepository, transactionManager, readerProvider);

        // when
        Job job = config.restaurantPartitionedJob(mockMasterStep);

        // then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("restaurantPartitionedJob");
    }

    @DisplayName("RestaurantSkipListener는 지정된 Path로 생성되어야 한다")
    @Test
    void testRestaurantSkipListenerFieldViaReflection() throws Exception {
        // given
        String path = "/tmp/failure-log.csv";
        RestaurantSkipListener listener = new RestaurantSkipListener(Path.of(path));

        // when
        Field failureFileField = RestaurantSkipListener.class.getDeclaredField("failureFile");
        failureFileField.setAccessible(true);
        Object fieldValue = failureFileField.get(listener);

        // then
        assertThat(fieldValue).isNotNull();
        assertThat(fieldValue.toString()).isEqualTo(path);
    }
}
