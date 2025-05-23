package com.assignment.restaurantbatch.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 문자열을 안전하게 날짜, 숫자 타입으로 변환하는 유틸리티 클래스입니다.
 * <p>
 * - 공백/null 입력은 null 반환<br>
 * - 포맷 오류 시 예외를 발생시켜 상위에서 처리 가능
 */
public class ParseUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 공백 또는 null인지 판단합니다.
     */
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * yyyy-MM-dd 형식 문자열을 LocalDate로 변환합니다.
     *
     * @param value 날짜 문자열
     * @return LocalDate 객체 또는 null
     */
    public static LocalDate toLocalDate(String value) {
        if (isBlank(value)) return null;
        return LocalDate.parse(value.trim(), DATE_FORMAT);
    }

    /**
     * yyyy-MM-dd HH:mm:ss 형식 문자열을 LocalDateTime으로 변환합니다.
     *
     * @param value 날짜시간 문자열
     * @return LocalDateTime 객체 또는 null
     */
    public static LocalDateTime toLocalDateTime(String value) {
        if (isBlank(value)) return null;
        return LocalDateTime.parse(value.trim(), DATETIME_FORMAT);
    }

    /**
     * 숫자 문자열을 Integer로 변환합니다.
     *
     * @param value 숫자 문자열
     * @return Integer 값 또는 null
     */
    public static Integer parseInt(String value) {
        if (isBlank(value)) return null;
        return Integer.parseInt(value.trim());
    }

    /**
     * 숫자 문자열을 Double로 변환합니다.
     *
     * @param value 숫자 문자열
     * @return Double 값 또는 null
     */
    public static Double parseDouble(String value) {
        if (isBlank(value)) return null;
        return Double.parseDouble(value.trim());
    }
}
