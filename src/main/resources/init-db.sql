-- ========================================================
-- restaurant_db 초기화 스크립트
-- DB 생성 + 사용자 생성 + 권한 부여 + 테이블 생성까지 포함
-- ========================================================

-- 1. 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS restaurant_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

-- 2. 사용자 생성 (모든 호스트에서 접속 가능하도록 % 사용)
CREATE USER IF NOT EXISTS 'batchuser'@'%' IDENTIFIED BY 'batchpass123!';

-- 3. 권한 부여
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'batchuser'@'%';
FLUSH PRIVILEGES;

-- 4. 사용할 DB 선택
USE restaurant_db;

-- 5. 테이블 생성
CREATE TABLE IF NOT EXISTS restaurant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    record_number INT COMMENT '원본 CSV 번호',
    service_name VARCHAR(100) COMMENT '개방서비스명',
    service_id VARCHAR(50) COMMENT '개방서비스아이디',
    region_code VARCHAR(10) COMMENT '개방자치단체코드',
    management_number VARCHAR(50) COMMENT '관리번호',
    license_date DATE COMMENT '인허가일자',
    cancel_date DATE COMMENT '인허가취소일자',
    business_status_code VARCHAR(10) COMMENT '영업상태구분코드',
    business_status_name VARCHAR(20) COMMENT '영업상태명',
    detail_status_code VARCHAR(10) COMMENT '상세영업상태코드',
    detail_status_name VARCHAR(20) COMMENT '상세영업상태명',
    close_date DATE COMMENT '폐업일자',
    suspend_start_date DATE COMMENT '휴업시작일자',
    suspend_end_date DATE COMMENT '휴업종료일자',
    reopen_date DATE COMMENT '재개업일자',
    phone VARCHAR(50) COMMENT '소재지전화',
    area_size VARCHAR(20) COMMENT '소재지면적',
    postal_code VARCHAR(10) COMMENT '소재지우편번호',
    full_address VARCHAR(255) COMMENT '소재지전체주소',
    road_address VARCHAR(255) COMMENT '도로명전체주소',
    road_postal_code VARCHAR(10) COMMENT '도로명우편번호',
    store_name VARCHAR(255) COMMENT '사업장명',
    last_modified DATETIME COMMENT '최종수정시점',
    data_update_type VARCHAR(5) COMMENT '데이터갱신구분',
    data_update_date DATETIME COMMENT '데이터갱신일자',
    business_type VARCHAR(50) COMMENT '업태구분명',
    coord_x DOUBLE COMMENT '좌표정보X(EPSG:5174)',
    coord_y DOUBLE COMMENT '좌표정보Y(EPSG:5174)',
    sanitation_type VARCHAR(50) COMMENT '위생업태명',
    male_employee INT COMMENT '남성종사자수',
    female_employee INT COMMENT '여성종사자수',
    around_info VARCHAR(50) COMMENT '영업장주변구분명',
    grade VARCHAR(20) COMMENT '등급구분명',
    water_type VARCHAR(50) COMMENT '급수시설구분명',
    total_employees INT COMMENT '총직원수',
    hq_employees INT COMMENT '본사직원수',
    office_employees INT COMMENT '공장사무직직원수',
    sales_employees INT COMMENT '공장판매직직원수',
    production_employees INT COMMENT '공장생산직직원수',
    building_ownership VARCHAR(50) COMMENT '건물소유구분명',
    guarantee_amount BIGINT COMMENT '보증액(원)',
    monthly_rent BIGINT COMMENT '월세액(원)',
    multi_use_yn CHAR(1) COMMENT '다중이용업소여부',
    total_scale VARCHAR(50) COMMENT '시설총규모',
    traditional_id VARCHAR(100) COMMENT '전통업소지정번호',
    main_menu VARCHAR(255) COMMENT '전통업소주된음식',
    homepage VARCHAR(255) COMMENT '홈페이지'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
