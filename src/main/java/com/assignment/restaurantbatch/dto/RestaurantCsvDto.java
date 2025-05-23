package com.assignment.restaurantbatch.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * CSV 파일로부터 읽어들인 음식점 정보를 담는 DTO 클래스입니다.
 * 각 필드는 전국일반음식점표준데이터의 컬럼과 1:1로 매핑됩니다.
 */
@Getter
@Setter
public class RestaurantCsvDto {

    /** 레코드 번호 */
    private Integer recordNumber;

    /** 개방 서비스명 */
    private String serviceName;

    /** 개방 서비스 ID */
    private String serviceId;

    /** 자치단체 코드 */
    private String regionCode;

    /** 관리 번호 */
    private String managementNumber;

    /** 인허가 일자 */
    private String licenseDate;

    /** 인허가 취소 일자 */
    private String cancelDate;

    /** 영업 상태 코드 */
    private String businessStatusCode;

    /** 영업 상태 명 */
    private String businessStatusName;

    /** 상세 영업 상태 코드 */
    private String detailStatusCode;

    /** 상세 영업 상태 명 */
    private String detailStatusName;

    /** 폐업 일자 */
    private String closeDate;

    /** 휴업 시작일 */
    private String suspendStartDate;

    /** 휴업 종료일 */
    private String suspendEndDate;

    /** 재개업 일자 */
    private String reopenDate;

    /** 전화번호 */
    private String phone;

    /** 소재지 면적 */
    private String areaSize;

    /** 우편번호 */
    private String postalCode;

    /** 전체 주소 */
    private String fullAddress;

    /** 도로명 주소 */
    private String roadAddress;

    /** 도로명 우편번호 */
    private String roadPostalCode;

    /** 사업장명 */
    private String storeName;

    /** 최종 수정 시점 */
    private String lastModified;

    /** 데이터 갱신 구분 */
    private String dataUpdateType;

    /** 데이터 갱신 일자 */
    private String dataUpdateDate;

    /** 업태 구분명 */
    private String businessType;

    /** X 좌표 (EPSG:5174) */
    private String coordX;

    /** Y 좌표 (EPSG:5174) */
    private String coordY;

    /** 위생업태명 */
    private String sanitationType;

    /** 남성 종사자 수 */
    private String maleEmployee;

    /** 여성 종사자 수 */
    private String femaleEmployee;

    /** 주변 환경 정보 */
    private String aroundInfo;

    /** 등급 구분명 */
    private String grade;

    /** 급수 시설 구분명 */
    private String waterType;

    /** 총 직원 수 */
    private String totalEmployees;

    /** 본사 직원 수 */
    private String hqEmployees;

    /** 사무직 직원 수 */
    private String officeEmployees;

    /** 판매직 직원 수 */
    private String salesEmployees;

    /** 생산직 직원 수 */
    private String productionEmployees;

    /** 건물 소유 구분명 */
    private String buildingOwnership;

    /** 보증액 */
    private String guaranteeAmount;

    /** 월세액 */
    private String monthlyRent;

    /** 다중이용업소 여부 */
    private String multiUseYn;

    /** 시설 총규모 */
    private String totalScale;

    /** 전통업소 지정번호 */
    private String traditionalId;

    /** 주된 메뉴 */
    private String mainMenu;

    /** 홈페이지 URL */
    private String homepage;
}
