

# 전국일반음식점표준데이터 배치 처리 시스템

## 프로젝트 개요

공공데이터포털에서 제공하는 전국일반음식점표준데이터(CSV) 를 MySQL DB에 대량 저장하는 Spring Batch 기반 배치 애플리케이션입니다.

* 대용량 CSV → 병렬 처리 → 무결성 보장 DB 저장
* 예외 처리 및 실패 로그 기록
* 재시도 정책 포함
* 단위 & 통합 테스트 완비
* 개발 기간: 2025.05.20 \~ 2025.05.23
* 데이터 출처: [공공데이터포털 링크](https://www.data.go.kr/data/15096283/standard.do)

---

## 프로젝트 구조

```text
src
├── main
│   ├── java/com.assignment.restaurantbatch
│   │   ├── dto/           # CSV DTO
│   │   ├── job/           # Job/Step 설정
│   │   ├── listener/      # 로깅 및 스킵 리스너
│   │   ├── partition/     # MultiResourcePartitioner 설정
│   │   ├── policy/        # SkipPolicy 정의
│   │   ├── reader/        # FlatFileItemReader 설정
│   │   ├── util/          # CsvSplitter, BatchTuner 등 유틸
│   │   ├── writer/        # MultiInsertWriter 구현
│   │   └── RestaurantBatchApplication.java
│   └── resources/
│       ├── application.yml
│       └── init-db.sql    # Mysql DDL
│
├── test
│   ├── java/com.assignment.restaurantbatch
│   │   ├── dto/
│   │   ├── integration/   # 통합 테스트 (Success/Failure 시나리오)
│   │   ├── job/
│   │   ├── listener/
│   │   ├── partition/
│   │   ├── policy/
│   │   ├── reader/
│   │   ├── util/
│   │   └── writer/
│   └── resources/
│       ├── application-test.yml
│       ├── failure-test.csv
│       ├── success-test.csv
│       ├── test-schema.sql
│       └── partitioned/  
```

---

## 주요 기능

| 기능              | 설명                                                |
| --------------- | ------------------------------------------------- |
| 대용량 CSV 처리    | 220만 줄 이상 MS949 인코딩 CSV 처리                        |
| 파티셔닝 + 병렬 처리  | MultiResourcePartitioner + AsyncExecutor 병렬 처리 구성 |
| 다중 INSERT 최적화 | JDBC PreparedStatement + multi-row insert       |
| 예외 처리 및 재시도   | CustomSkipPolicy, Retry 정책 3회                 |
| 실패 로그 기록      | 실패한 레코드는 CSV로 별도 저장 (recordNumber 중복 제거)          |
| 테스트 커버리지      | 유닛 + 통합 테스트 완비 (H2 기반)                            |

---

## 실행 방법

### 1. 의존성 설치

```bash
./gradlew clean build
```

### 2. DB 초기화

```bash
mysql -u root -p < init-db.sql
```

### 3. CSV 파일 준비

* data/restaurant.csv 경로에 전국일반음식점표준데이터 CSV 파일을 위치

### 4. 애플리케이션 실행

```bash
java -jar build/libs/restaurantbatch-0.0.1-SNAPSHOT.jar
```

---

## 테스트 방법

### 1. 테스트 실행

```bash
./gradlew test
```

### 2. 검증 항목

* DTO 매핑 유효성
* Job 및 Step 생성 검증
* 파티셔닝 + 병렬 실행 검증
* 예외 발생 시 스킵 및 로그 파일 저장
* 통합 테스트에서 성공/실패 시나리오 검증

---

## 성능 최적화 전략

| 항목         | 전략                                              |
| ---------- | ----------------------------------------------- |
| 파티셔닝 수     | CPU 코어 수 기준 gridSize 자동 설정                    |
| Chunk Size | 전체 CSV 라인 수 기반 BatchTuner를 통한 동적 설정           |
| Insert 방식  | JDBC multi-row insert (한 쿼리로 다중 레코드 처리)       |
| 데이터 인코딩    | MS949 지원 및 헤더 유효성 검증                            |
| DB 커넥션     | HikariCP 튜닝 (최대 32 pool, connection timeout 설정) |

> 성능 측정 결과  
> 220만 줄 기준 평균 처리 시간:
> - 40초 (로그 미기록 시)
> - 70초 (로그 기록 포함 시)

---

## 테스트 커버리지 요약

* 단위 테스트: DTO, Reader, Writer, Policy, Listener, Partitioner
* 통합 테스트: 성공 시나리오 + 실패 레코드 스킵 검증
* 테스트 DB: H2 + test-schema.sql 자동 초기화
* 실패 케이스는 recordNumber 기준 중복 제거 후 실패 CSV에 기록됨

---

## 개발 환경

| 항목           | 버전                     |
| ------------ | ---------------------- |
| Java         | 17 (Adoptium Temurin)  |
| Spring Boot  | 3.2.5                  |
| Spring Batch | 5.1.1                  |
| MySQL Driver | 8.3.0                  |
| Gradle       | 8.10                   |
| 테스트 DB       | H2 2.2.224 (in-memory) |

---

## 기타 참고사항

* 실패 로그는 `/data/failure/` 경로에 CSV로 저장되며, 병렬 환경에서도 동기화 처리되어 중복 없이 안전하게 기록됩니다.
* 발생한 예외는 스킵 가능한 유형(`CSV 포맷`, `날짜 파싱`, `JDBC 일시적 오류` 등)과 불가한 유형으로 분리 처리되며, 스킵된 레코드는 모두 로그에 남습니다.
* 모든 로깅과 실패율은 JobExecutionListener를 통해 통계로 요약 출력됩니다.
