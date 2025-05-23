package com.assignment.restaurantbatch.util;

import org.springframework.stereotype.Component;

/**
 * 전체 CSV 라인 수에 따라 배치 처리 전략(gridSize, chunkSize 등)을 자동으로 결정하는 유틸리티 클래스입니다.
 * <p>
 * 시스템 자원 및 처리량을 고려하여 적절한 병렬성/효율성 균형을 제공합니다.
 */
@Component
public class BatchTuner {

    private static final int MAX_SAFE_POOL_SIZE = 32;
    private static final int MIN_GRID_SIZE = 4;

    /**
     * 전체 데이터 라인 수에 기반하여 최적의 배치 처리 전략을 결정합니다.
     *
     * @param totalLines 전체 레코드 수
     * @return BatchConfig (linesPerFile, gridSize, chunkSize)
     */
    public BatchConfig tune(int totalLines) {
        int coreCount = Runtime.getRuntime().availableProcessors();
        int safeGridSize = Math.max(Math.min(coreCount * 2, MAX_SAFE_POOL_SIZE), MIN_GRID_SIZE);

        if (totalLines <= 100_000) {
            return new BatchConfig(20_000, Math.min(safeGridSize, 8), 500);
        } else if (totalLines <= 1_000_000) {
            return new BatchConfig(50_000, Math.min(safeGridSize, 16), 1000);
        } else {
            return new BatchConfig(100_000, Math.min(safeGridSize, 24), 2000);
        }
    }

    /**
     * 튜닝 결과 DTO. linesPerFile: 분할 파일 크기, gridSize: 병렬 작업 수, chunkSize: Chunk 단위 처리 크기
     */
    public record BatchConfig(int linesPerFile, int gridSize, int chunkSize) {}
}
