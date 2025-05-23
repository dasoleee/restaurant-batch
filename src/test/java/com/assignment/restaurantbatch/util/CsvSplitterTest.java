package com.assignment.restaurantbatch.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CsvSplitter} 클래스의 분할 기능을 검증하는 단위 테스트입니다.
 * <p>
 * • 클래스패스 리소스 파일을 지정된 라인 수마다 분할하고
 * • 생성된 모든 분할 파일에 헤더가 포함되는지 확인합니다.
 */
class CsvSplitterTest {

    private final CsvSplitter csvSplitter = new CsvSplitter();
    private final Path outputPath = Path.of("build/test-output/csv-split");

    @AfterEach
    void cleanUp() throws Exception {
        if (!Files.exists(outputPath)) return;

        try (Stream<Path> paths = Files.walk(outputPath)) {
            paths.map(Path::toFile)
                    .sorted((a, b) -> -a.compareTo(b)) // 하위 → 상위 순으로 삭제
                    .forEach(file -> {
                        if (!file.delete()) {
                            System.err.println("파일 삭제 실패: " + file.getAbsolutePath());
                        }
                    });
        }
    }

    @Test
    @DisplayName("CSV 파일을 분할하면 각 결과 파일에 헤더가 포함되어야 한다")
    void split_createsPartitionedFiles_withHeader() throws Exception {
        // given: 클래스패스에서 입력 CSV 파일 로드
        var resource = new ClassPathResource("success-test.csv");
        assertThat(resource.exists()).as("입력 리소스가 존재해야 함").isTrue();
        Path inputPath = resource.getFile().toPath();

        // when: 3줄 단위로 분할 수행
        csvSplitter.split(inputPath, outputPath.toString(), 3);

        // then: 분할 파일 존재 및 헤더 포함 여부 확인
        File[] files = outputPath.toFile().listFiles((dir, name) -> name.endsWith(".csv"));

        assertThat(files).isNotNull().isNotEmpty();

        for (File file : files) {
            List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("MS949"));
            assertThat(lines.get(0)).contains("번호"); // 첫 줄은 반드시 헤더
        }
    }
}
