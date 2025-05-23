package com.assignment.restaurantbatch.util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * 입력 CSV 파일을 지정된 라인 수 단위로 분할하는 유틸리티 클래스입니다.
 * <p>
 * - 헤더는 모든 분할 파일에 포함됩니다.<br>
 * - 기존 파일이 존재할 경우 삭제 후 새로 생성합니다.
 */
@Component
public class CsvSplitter {

    /**
     * 주어진 파일 경로의 CSV 파일을 지정된 라인 수 단위로 분할하여 저장합니다.
     *
     * @param inputPath     원본 CSV 파일 경로
     * @param outputDir     분할 파일 저장 디렉토리
     * @param linesPerFile  파일당 라인 수
     */
    public void split(Path inputPath, String outputDir, int linesPerFile) {
        try (BufferedReader reader = Files.newBufferedReader(inputPath, Charset.forName("MS949"))) {

            Path outDir = Paths.get(outputDir);
            Files.createDirectories(outDir);

            // 기존 파일 삭제
            try (Stream<Path> paths = Files.walk(outDir)) {
                paths.filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new UncheckedIOException("파티션 파일 삭제 실패: " + path, e);
                            }
                        });
            }

            // 헤더 + 내용 읽기
            String header = reader.readLine();
            List<String> buffer = new ArrayList<>();
            String line;
            int fileIndex = 0;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                buffer.add(line);
                count++;
                if (count == linesPerFile) {
                    writeFile(buffer, header, outDir, fileIndex++);
                    buffer.clear();
                    count = 0;
                }
            }

            if (!buffer.isEmpty()) {
                writeFile(buffer, header, outDir, fileIndex);
            }

        } catch (IOException e) {
            throw new RuntimeException("CSV 분할 실패: " + inputPath, e);
        }
    }

    private void writeFile(List<String> lines, String header, Path dir, int index) throws IOException {
        Path file = dir.resolve(String.format("restaurant-part-%03d.csv", index));
        try (BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("MS949"))) {
            writer.write(header);
            writer.newLine();
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}

