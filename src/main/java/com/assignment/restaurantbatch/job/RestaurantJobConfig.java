package com.assignment.restaurantbatch.job;

import com.assignment.restaurantbatch.dto.RestaurantCsvDto;
import com.assignment.restaurantbatch.listener.RestaurantJobExecutionListener;
import com.assignment.restaurantbatch.listener.RestaurantSkipListener;
import com.assignment.restaurantbatch.listener.StepExecutionLogger;
import com.assignment.restaurantbatch.policy.CustomSkipPolicy;
import com.assignment.restaurantbatch.writer.MultiInsertWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * Spring Batch의 Job 및 Step 설정 클래스입니다.
 * CSV 파일을 파티셔닝하여 병렬로 처리하는 마스터-슬레이브 구조를 구성합니다.
 */
@Configuration
@RequiredArgsConstructor
public class RestaurantJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ObjectProvider<FlatFileItemReader<RestaurantCsvDto>> readerProvider;

    /**
     * 전체 Batch Job 설정.
     * @param masterStep 병렬 처리용 마스터 스텝
     * @return Job 인스턴스
     */
    @Bean
    public Job restaurantPartitionedJob(Step masterStep) {
        return new JobBuilder("restaurantPartitionedJob", jobRepository)
                .listener(new RestaurantJobExecutionListener())
                .start(masterStep)
                .build();
    }

    /**
     * 마스터 스텝 설정. Partition을 수행하며 슬레이브 스텝을 병렬 실행합니다.
     */
    @Bean
    @JobScope
    public Step masterStep(
            @Value("#{jobParameters['gridSize']}") Integer gridSize,
            @Value("#{jobParameters['chunkSize']}") Integer chunkSize,
            MultiResourcePartitioner partitioner,
            MultiInsertWriter writer,
            RestaurantSkipListener restaurantSkipListener
    ) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(new SimpleAsyncTaskExecutor("partitioner-"));
        handler.setGridSize(gridSize);
        handler.setStep(createSlaveStep(chunkSize, writer, restaurantSkipListener));

        return new StepBuilder("masterStep", jobRepository)
                .partitioner("slaveStep", partitioner)
                .partitionHandler(handler)
                .build();
    }

    /**
     * 슬레이브 스텝 설정. 각 파티션 파일을 읽어 DB에 저장합니다.
     */
    private Step createSlaveStep(
            int chunkSize,
            MultiInsertWriter writer,
            RestaurantSkipListener restaurantSkipListener
    ) {
        return new StepBuilder("slaveStep", jobRepository)
                .<RestaurantCsvDto, RestaurantCsvDto>chunk(chunkSize, transactionManager)
                .reader(readerProvider.getObject())
                .writer(writer)
                .faultTolerant()
                .retry(TransientDataAccessException.class)
                .retry(CannotGetJdbcConnectionException.class)
                .retry(SQLException.class)
                .retryLimit(3)
                .skipPolicy(new CustomSkipPolicy())
                .listener(new StepExecutionLogger())
                .listener(restaurantSkipListener)
                .build();
    }

    /**
     * 실패한 레코드를 기록할 SkipListener Bean입니다.
     */
    @Bean(name = "restaurantSkipListener")
    @StepScope
    public RestaurantSkipListener restaurantSkipListener(
            @Value("#{jobParameters['failureLog']}") String failurePath
    ) {
        return new RestaurantSkipListener(Paths.get(failurePath));
    }
}
