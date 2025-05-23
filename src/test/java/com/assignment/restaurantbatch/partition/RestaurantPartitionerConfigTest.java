package com.assignment.restaurantbatch.partition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RestaurantPartitionerConfig} 클래스의 단위 테스트입니다.
 * <p>
 * • 지정된 디렉토리 내에 존재하는 `restaurant-part-*.csv` 파일들을
 * • 리소스로 인식하고 `MultiResourcePartitioner`에 올바르게 등록하는지 검증합니다.
 */
class RestaurantPartitionerConfigTest {

    private final RestaurantPartitionerConfig config = new RestaurantPartitionerConfig();

    @Test
    @DisplayName("리소스 디렉토리에서 restaurant-part-*.csv 파일들을 파티셔너에 등록하는지 테스트")
    void shouldConfigurePartitionResourcesFromClasspath() throws Exception {
        // given: 클래스패스 기준 리소스 디렉토리 경로 획득
        File folder = new File(
                getClass().getClassLoader().getResource("partitioned").toURI()
        );

        // restaurant-part-*.csv 파일 필터링
        File[] expectedFiles = folder.listFiles((dir, name) ->
                name.startsWith("restaurant-part") && name.endsWith(".csv"));

        assertThat(expectedFiles)
                .as("테스트 리소스 디렉토리에 적어도 하나의 파티션 파일이 있어야 합니다.")
                .isNotNull()
                .isNotEmpty();

        Resource[] expectedResources = new Resource[expectedFiles.length];
        for (int i = 0; i < expectedFiles.length; i++) {
            expectedResources[i] = new FileSystemResource(expectedFiles[i]);
        }

        // when: 절대경로로 config 호출
        String partitionDir = folder.getAbsolutePath();
        MultiResourcePartitioner partitioner = config.multiResourcePartitioner(partitionDir);

        // then: partitioner 내부 설정 리소스 확인
        Field resourcesField = MultiResourcePartitioner.class.getDeclaredField("resources");
        resourcesField.setAccessible(true);
        Resource[] actualResources = (Resource[]) resourcesField.get(partitioner);

        assertThat(partitioner).isNotNull();
        assertThat(actualResources).hasSameSizeAs(expectedResources);
    }
}
