package com.assignment.restaurantbatch.reader;

import com.assignment.restaurantbatch.dto.RestaurantCsvDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 파티션된 CSV 파일을 읽기 위한 Spring Batch ItemReader 설정 클래스입니다.
 * <p>
 * 각 슬레이브 스텝에서 주어진 파일 리소스를 기반으로 FlatFileItemReader를 생성합니다.
 */
@Slf4j
@Configuration
public class RestaurantItemReaderConfig {

    /** DTO 필드 수 = 예상 CSV 컬럼 수 */
    private static final int RECORD_COLUMN_COUNT = RestaurantCsvDto.class.getDeclaredFields().length;

    /**
     * FlatFileItemReader 설정. 한 파티션 파일에 대해 한 슬레이브 스텝이 실행됩니다.
     * @param resource 파티션 파일 리소스 (stepExecutionContext['file']로 전달됨)
     * @return FlatFileItemReader 인스턴스
     */
    @Bean(name = "restaurantItemReader")
    @StepScope
    public FlatFileItemReader<RestaurantCsvDto> restaurantItemReader(
            @Value("#{stepExecutionContext['file']}") Resource resource
    ) throws Exception {

        // CSV 헤더 유효성 검사
        validateHeader(resource);

        return new FlatFileItemReaderBuilder<RestaurantCsvDto>()
                .name("restaurantItemReader")
                .resource(resource)
                .encoding("MS949")
                .linesToSkip(1)
                .strict(true)

                // 빈 줄 무시
                .recordSeparatorPolicy(new DefaultRecordSeparatorPolicy() {
                    @Override
                    public boolean isEndOfRecord(String line) {
                        return line != null && !line.trim().isEmpty();
                    }
                })

                .lineMapper(lineMapper())
                .build();
    }

    /**
     * CSV 헤더의 필드 수가 DTO와 일치하는지 검증합니다.
     */
    private void validateHeader(Resource resource) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), Charset.forName("MS949")))) {
            String header = reader.readLine();
            if (header == null) {
                throw new IllegalArgumentException("CSV 파일에 헤더가 존재하지 않습니다.");
            }
            int columnCount = header.split(",").length;
            if (columnCount != RECORD_COLUMN_COUNT) {
                throw new IllegalArgumentException("CSV 헤더 필드 수가 예상과 다릅니다. 실제: " + columnCount);
            }
        }
    }

    /**
     * CSV 라인을 DTO로 매핑하기 위한 LineMapper 구성.
     */
    private DefaultLineMapper<RestaurantCsvDto> lineMapper() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setQuoteCharacter('"');
        tokenizer.setStrict(false);
        tokenizer.setNames(
                "recordNumber", "serviceName", "serviceId", "regionCode", "managementNumber",
                "licenseDate", "cancelDate", "businessStatusCode", "businessStatusName",
                "detailStatusCode", "detailStatusName", "closeDate", "suspendStartDate", "suspendEndDate",
                "reopenDate", "phone", "areaSize", "postalCode", "fullAddress", "roadAddress",
                "roadPostalCode", "storeName", "lastModified", "dataUpdateType", "dataUpdateDate",
                "businessType", "coordX", "coordY", "sanitationType", "maleEmployee", "femaleEmployee",
                "aroundInfo", "grade", "waterType", "totalEmployees", "hqEmployees", "officeEmployees",
                "salesEmployees", "productionEmployees", "buildingOwnership", "guaranteeAmount", "monthlyRent",
                "multiUseYn", "totalScale", "traditionalId", "mainMenu", "homepage"
        );

        BeanWrapperFieldSetMapper<RestaurantCsvDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(RestaurantCsvDto.class);

        DefaultLineMapper<RestaurantCsvDto> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }
}
