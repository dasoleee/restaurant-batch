package com.assignment.restaurantbatch.writer;

import com.assignment.restaurantbatch.dto.RestaurantCsvDto;
import com.assignment.restaurantbatch.util.ParseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Chunk 단위로 CSV 데이터를 DB에 다중 INSERT하는 Writer 클래스입니다.
 * <p>
 * PreparedStatement를 사용하여 데이터 타입에 맞게 바인딩하며,
 * 성능 향상을 위해 다중 VALUES SQL 구문을 동적으로 생성합니다.
 */
@RequiredArgsConstructor
public class MultiInsertWriter implements ItemWriter<RestaurantCsvDto> {

    private final DataSource dataSource;

    /** INSERT 구문 prefix (VALUES 제외) */
    private static final String INSERT_SQL_PREFIX = """
        INSERT INTO restaurant (
            record_number, service_name, service_id, region_code, management_number,
            license_date, cancel_date, business_status_code, business_status_name,
            detail_status_code, detail_status_name, close_date, suspend_start_date,
            suspend_end_date, reopen_date, phone, area_size, postal_code, full_address,
            road_address, road_postal_code, store_name, last_modified, data_update_type,
            data_update_date, business_type, coord_x, coord_y, sanitation_type,
            male_employee, female_employee, around_info, grade, water_type,
            total_employees, hq_employees, office_employees, sales_employees,
            production_employees, building_ownership, guarantee_amount, monthly_rent,
            multi_use_yn, total_scale, traditional_id, main_menu, homepage
        ) VALUES 
    """;

    private static final int RECORD_COLUMN_COUNT = RestaurantCsvDto.class.getDeclaredFields().length;

    /**
     * 한 Chunk의 아이템들을 다중 INSERT SQL로 DB에 저장합니다.
     */
    @Override
    public void write(Chunk<? extends RestaurantCsvDto> chunk) throws Exception {
        List<? extends RestaurantCsvDto> items = chunk.getItems();
        if (items.isEmpty()) return;

        String sql = INSERT_SQL_PREFIX + generatePlaceholders(items.size(), RECORD_COLUMN_COUNT);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            for (RestaurantCsvDto item : items) {
                bindValues(ps, item, paramIndex);
                paramIndex += RECORD_COLUMN_COUNT;
            }

            ps.executeUpdate();
        }
    }

    /**
     * (?,?,?,...) 형태의 VALUES 구문을 레코드 수만큼 생성합니다.
     */
    private String generatePlaceholders(int rows, int cols) {
        String singleRow = "(" + "?,".repeat(cols - 1) + "?" + ")";
        return String.join(",", java.util.Collections.nCopies(rows, singleRow));
    }

    /**
     * 한 레코드의 각 필드를 PreparedStatement에 순서대로 바인딩합니다.
     */
    private void bindValues(PreparedStatement ps, RestaurantCsvDto item, int index) throws Exception {
        ps.setObject(index++, item.getRecordNumber());
        ps.setString(index++, item.getServiceName());
        ps.setString(index++, item.getServiceId());
        ps.setString(index++, item.getRegionCode());
        ps.setString(index++, item.getManagementNumber());
        ps.setObject(index++, ParseUtil.toLocalDate(item.getLicenseDate()));
        ps.setObject(index++, ParseUtil.toLocalDate(item.getCancelDate()));
        ps.setString(index++, item.getBusinessStatusCode());
        ps.setString(index++, item.getBusinessStatusName());
        ps.setString(index++, item.getDetailStatusCode());
        ps.setString(index++, item.getDetailStatusName());
        ps.setObject(index++, ParseUtil.toLocalDate(item.getCloseDate()));
        ps.setObject(index++, ParseUtil.toLocalDate(item.getSuspendStartDate()));
        ps.setObject(index++, ParseUtil.toLocalDate(item.getSuspendEndDate()));
        ps.setObject(index++, ParseUtil.toLocalDate(item.getReopenDate()));
        ps.setString(index++, item.getPhone());
        ps.setString(index++, item.getAreaSize());
        ps.setString(index++, item.getPostalCode());
        ps.setString(index++, item.getFullAddress());
        ps.setString(index++, item.getRoadAddress());
        ps.setString(index++, item.getRoadPostalCode());
        ps.setString(index++, item.getStoreName());
        ps.setObject(index++, ParseUtil.toLocalDateTime(item.getLastModified()));
        ps.setString(index++, item.getDataUpdateType());
        ps.setObject(index++, ParseUtil.toLocalDateTime(item.getDataUpdateDate()));
        ps.setString(index++, item.getBusinessType());
        ps.setObject(index++, ParseUtil.parseDouble(item.getCoordX()));
        ps.setObject(index++, ParseUtil.parseDouble(item.getCoordY()));
        ps.setString(index++, item.getSanitationType());
        ps.setObject(index++, ParseUtil.parseInt(item.getMaleEmployee()));
        ps.setObject(index++, ParseUtil.parseInt(item.getFemaleEmployee()));
        ps.setString(index++, item.getAroundInfo());
        ps.setString(index++, item.getGrade());
        ps.setString(index++, item.getWaterType());
        ps.setObject(index++, ParseUtil.parseInt(item.getTotalEmployees()));
        ps.setObject(index++, ParseUtil.parseInt(item.getHqEmployees()));
        ps.setObject(index++, ParseUtil.parseInt(item.getOfficeEmployees()));
        ps.setObject(index++, ParseUtil.parseInt(item.getSalesEmployees()));
        ps.setObject(index++, ParseUtil.parseInt(item.getProductionEmployees()));
        ps.setString(index++, item.getBuildingOwnership());
        ps.setObject(index++, ParseUtil.parseInt(item.getGuaranteeAmount()));
        ps.setObject(index++, ParseUtil.parseInt(item.getMonthlyRent()));
        ps.setString(index++, item.getMultiUseYn());
        ps.setString(index++, item.getTotalScale());
        ps.setString(index++, item.getTraditionalId());
        ps.setString(index++, item.getMainMenu());
        ps.setString(index++, item.getHomepage());
    }
}
