package com.assignment.restaurantbatch.listener;

import com.assignment.restaurantbatch.dto.RestaurantCsvDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.ExitStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code RestaurantSkipListener}는 Spring Batch의 병렬 처리 환경에서도
 * 실패 항목을 하나의 로그 파일로 안전하게 기록할 수 있도록 설계된 리스너입니다.
 *
 * <p>다음 기능을 제공합니다:
 * <ul>
 *   <li>병렬 Step 간 충돌 없이 하나의 파일에 append</li>
 *   <li>헤더는 최초 1회만 기록 (beforeStep에서 처리)</li>
 *   <li>recordNumber 기준 중복 항목은 기록하지 않음</li>
 * </ul>
 */
@Slf4j
public class RestaurantSkipListener implements SkipListener<RestaurantCsvDto, RestaurantCsvDto>, StepExecutionListener {

    private final Path failureFile;

    // recordNumber 중복 제거용 (스레드 안전)
    private static final Set<String> seenRecordNumbers = ConcurrentHashMap.newKeySet();

    public RestaurantSkipListener(Path failureFile) {
        this.failureFile = failureFile;
        try {
            Files.createDirectories(failureFile.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException("디렉토리 생성 실패", e);
        }
    }

    /**
     * Step 시작 전 헤더를 기록합니다. 모든 스레드에 대해 최초 한 번만 실행됩니다.
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        try (BufferedWriter writer = Files.newBufferedWriter(failureFile,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(buildHeaderLine());
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException("헤더 기록 실패", e);
        }
    }

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("읽기 단계 Skip 발생: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(RestaurantCsvDto item, Throwable t) {
        writeFailureLine(item);
    }

    @Override
    public void onSkipInWrite(RestaurantCsvDto item, Throwable t) {
        writeFailureLine(item);
    }

    /**
     * 실패 항목을 파일에 기록합니다. 중복된 recordNumber는 한 번만 기록됩니다.
     */
    private void writeFailureLine(RestaurantCsvDto item) {
        if (item == null) return;

        String key = String.valueOf(item.getRecordNumber());
        if (!seenRecordNumbers.add(key)) return; // 중복이면 기록하지 않음

        try (BufferedWriter writer = Files.newBufferedWriter(failureFile,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(buildCsvLine(item));
            writer.newLine();
        } catch (IOException e) {
            log.error("실패 라인 기록 중 오류", e);
        }
    }

    private String buildHeaderLine() {
        return "\"번호\",\"개방서비스명\",\"개방서비스아이디\",\"개방자치단체코드\",\"관리번호\",\"인허가일자\",\"인허가취소일자\"," +
                "\"영업상태구분코드\",\"영업상태명\",\"상세영업상태코드\",\"상세영업상태명\",\"폐업일자\",\"휴업시작일자\",\"휴업종료일자\"," +
                "\"재개업일자\",\"소재지전화\",\"소재지면적\",\"소재지우편번호\",\"소재지전체주소\",\"도로명전체주소\",\"도로명우편번호\"," +
                "\"사업장명\",\"최종수정시점\",\"데이터갱신구분\",\"데이터갱신일자\",\"업태구분명\",\"좌표정보x(epsg5174)\"," +
                "\"좌표정보y(epsg5174)\",\"위생업태명\",\"남성종사자수\",\"여성종사자수\",\"영업장주변구분명\",\"등급구분명\"," +
                "\"급수시설구분명\",\"총직원수\",\"본사직원수\",\"공장사무직직원수\",\"공장판매직직원수\",\"공장생산직직원수\"," +
                "\"건물소유구분명\",\"보증액\",\"월세액\",\"다중이용업소여부\",\"시설총규모\",\"전통업소지정번호\",\"전통업소주된음식\"," +
                "\"홈페이지\"";
    }

    private String buildCsvLine(RestaurantCsvDto item) {
        return String.join(",", quote(item.getRecordNumber()),
                quote(item.getServiceName()), quote(item.getServiceId()), quote(item.getRegionCode()),
                quote(item.getManagementNumber()), quote(item.getLicenseDate()), quote(item.getCancelDate()),
                quote(item.getBusinessStatusCode()), quote(item.getBusinessStatusName()), quote(item.getDetailStatusCode()),
                quote(item.getDetailStatusName()), quote(item.getCloseDate()), quote(item.getSuspendStartDate()),
                quote(item.getSuspendEndDate()), quote(item.getReopenDate()), quote(item.getPhone()),
                quote(item.getAreaSize()), quote(item.getPostalCode()), quote(item.getFullAddress()),
                quote(item.getRoadAddress()), quote(item.getRoadPostalCode()), quote(item.getStoreName()),
                quote(item.getLastModified()), quote(item.getDataUpdateType()), quote(item.getDataUpdateDate()),
                quote(item.getBusinessType()), quote(item.getCoordX()), quote(item.getCoordY()),
                quote(item.getSanitationType()), quote(item.getMaleEmployee()), quote(item.getFemaleEmployee()),
                quote(item.getAroundInfo()), quote(item.getGrade()), quote(item.getWaterType()),
                quote(item.getTotalEmployees()), quote(item.getHqEmployees()), quote(item.getOfficeEmployees()),
                quote(item.getSalesEmployees()), quote(item.getProductionEmployees()), quote(item.getBuildingOwnership()),
                quote(item.getGuaranteeAmount()), quote(item.getMonthlyRent()), quote(item.getMultiUseYn()),
                quote(item.getTotalScale()), quote(item.getTraditionalId()), quote(item.getMainMenu()),
                quote(item.getHomepage()));
    }

    private String quote(Object value) {
        return "\"" + (value == null ? "" : value.toString().replace("\"", "\"\"")) + "\"";
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
