package com.assignment.restaurantbatch.writer;

import com.assignment.restaurantbatch.dto.RestaurantCsvDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.item.Chunk;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * {@link MultiInsertWriter} 클래스의 단위 테스트입니다.
 * <p>
 * • Chunk에 담긴 DTO들을 다중 INSERT 쿼리로 DB에 저장하는지 확인합니다.
 */
class MultiInsertWriterTest {

    @Test
    @DisplayName("한 Chunk의 아이템을 DB에 다중 INSERT 쿼리로 처리해야 한다")
    void shouldWriteChunkToDatabaseWithMultiInsert() throws Exception {
        // given: DTO 2개에 일부 필드를 채워 작성
        RestaurantCsvDto dto1 = new RestaurantCsvDto();
        dto1.setRecordNumber(1);
        dto1.setLicenseDate("2024-01-01");

        RestaurantCsvDto dto2 = new RestaurantCsvDto();
        dto2.setRecordNumber(2);
        dto2.setLicenseDate("2025-01-01");

        Chunk<RestaurantCsvDto> chunk = new Chunk<>(List.of(dto1, dto2));

        // mock 구성
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        MultiInsertWriter writer = new MultiInsertWriter(dataSource);

        // when
        writer.write(chunk);

        // then: executeUpdate 호출 확인
        verify(ps, times(1)).executeUpdate();

        // 바인딩된 모든 값 확인
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(ps, atLeast(1)).setObject(anyInt(), captor.capture());

        List<Object> allValues = captor.getAllValues();
        assertThat(allValues).contains(1, 2);
        assertThat(allValues.stream().map(String::valueOf))
                .anyMatch(s -> s.contains("2024-01-01"));
        assertThat(allValues.stream().map(String::valueOf))
                .anyMatch(s -> s.contains("2025-01-01"));
    }
}
