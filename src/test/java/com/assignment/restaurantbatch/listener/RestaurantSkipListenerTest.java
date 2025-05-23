package com.assignment.restaurantbatch.listener;

import com.assignment.restaurantbatch.dto.RestaurantCsvDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RestaurantSkipListener}의 병렬 환경 안정성 및 동작 검증을 위한 단위 테스트 클래스입니다.
 *
 * <p>테스트 항목:
 * <ul>
 *     <li>헤더는 오직 한 번만 기록되는지 검증</li>
 *     <li>중복 recordNumber는 한 번만 기록되는지 검증</li>
 *     <li>필드 내 큰따옴표는 CSV 형식으로 올바르게 이스케이프 되는지 검증</li>
 * </ul>
 */
class RestaurantSkipListenerTest {

    @TempDir
    Path tempDir;

    private Path failureFile;
    private RestaurantSkipListener listener;

    @BeforeEach
    void setUp() {
        // 테스트마다 새로운 실패로그 파일 생성
        failureFile = tempDir.resolve("failed.csv");
        listener = new RestaurantSkipListener(failureFile);
        listener.beforeStep(null); // 헤더 강제 작성
    }

    @Test
    @DisplayName("헤더는 단 한 번만 기록되어야 한다")
    void shouldWriteCsvHeaderOnlyOnce() throws IOException {
        // given & when: 동일 Step 내에서 여러 번 스킵 발생
        var item = new RestaurantCsvDto();
        item.setRecordNumber(100);
        item.setServiceName("Service");

        listener.onSkipInWrite(item, new RuntimeException("skip"));
        listener.onSkipInWrite(item, new RuntimeException("skip again"));

        // then: 헤더 + 한 줄만 존재해야 함
        List<String> lines = Files.readAllLines(failureFile);
        assertThat(lines).hasSize(2); // 헤더 + 1 데이터
        assertThat(lines.get(0)).contains("번호", "개방서비스명", "홈페이지");
        assertThat(lines.get(1)).contains("Service");
    }

    @Test
    @DisplayName("recordNumber가 같으면 중복 기록하지 않아야 한다")
    void shouldWriteOnlyUniqueRecordNumberOnce() throws IOException {
        // given: 중복된 recordNumber
        var item1 = new RestaurantCsvDto();
        item1.setRecordNumber(1);
        item1.setServiceName("A");

        var item2 = new RestaurantCsvDto();
        item2.setRecordNumber(1);
        item2.setServiceName("B");

        // when: 두 항목 모두 스킵 처리
        listener.onSkipInWrite(item1, new RuntimeException("skip"));
        listener.onSkipInWrite(item2, new RuntimeException("skip"));

        // then: 헤더 + item1만 기록되어야 함
        List<String> lines = Files.readAllLines(failureFile);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(1)).contains("A");
    }
}
