package com.assignment.restaurantbatch.partition;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Partition된 CSV 파일들을 리소스로 설정하는 구성 클래스입니다.
 * <p>
 * 주어진 디렉토리 내에서 특정 패턴의 CSV 파일들을 찾아 파티션 단위로 설정합니다.
 * 설정된 각 파일은 슬레이브 스텝에서 독립적으로 처리됩니다.
 */
@Configuration
public class RestaurantPartitionerConfig {

    /**
     * 파티션 리소스를 생성하는 Partitioner 빈입니다.
     *
     * @param partitionDir 파티션 CSV 파일이 위치한 디렉토리 (JobParameter)
     * @return MultiResourcePartitioner 인스턴스
     */
    @Bean
    @JobScope
    public MultiResourcePartitioner multiResourcePartitioner(
            @Value("#{jobParameters['partitionDir']}") String partitionDir
    ) {
        File folder = new File(partitionDir);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalStateException("Partition 디렉토리가 존재하지 않거나 디렉토리가 아님: " + partitionDir);
        }

        // restaurant-part-xxx.csv 패턴 파일 필터링
        File[] files = folder.listFiles((dir, name) ->
                name.startsWith("restaurant-part") && name.endsWith(".csv"));

        if (files == null || files.length == 0) {
            throw new IllegalStateException("분할된 CSV 파일이 없음: " + partitionDir);
        }

        // 정렬된 순서로 리소스 생성
        Arrays.sort(files);

        Resource[] resources = Stream.of(files)
                .map(FileSystemResource::new)
                .toArray(Resource[]::new);

        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        partitioner.setKeyName("file");       // stepExecutionContext['file']로 접근 가능
        partitioner.setResources(resources);  // 각 파일이 하나의 파티션으로 설정됨

        return partitioner;
    }
}
