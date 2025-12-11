package Calculation;

import javax.swing.table.TableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * ResultCalculator
 * - DashBoard_Gui의 테이블 데이터를 기반으로
 *   날짜별/주별/월별 배출량 합계를 계산하는 클래스
 * - GraphRendering 또는 통계 UI에서 사용가능
 */
public class ResultCalculator {

    // DashBoard_Gui 테이블 기준:
    // 0열: DATE (yyyy-MM-dd 문자열)
    // 5열: RESULT (double, CO2e kg)

    /**
     * 전체 합계 계산
     */
    public double calculateTotalEmission(TableModel model) {
        double total = 0.0;
        if (model == null) return 0.0;

        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                total += Double.parseDouble(model.getValueAt(i, 5).toString());
            } catch (Exception ignore) {
            }
        }
        return round3(total);
    }

    /**
     * 특정 날짜(yyyy-MM-dd)의 합계
     */
    public double dailyTotal(TableModel model, String date) {
        if (model == null || date == null) return 0.0;

        double sum = 0.0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String d = String.valueOf(model.getValueAt(i, 0));
            if (date.equals(d)) {
                try {
                    sum += Double.parseDouble(model.getValueAt(i, 5).toString());
                } catch (Exception ignore) {
                }
            }
        }
        return round3(sum);
    }

    /**
     * 주별 합계 계산 (단일 주)
     * datePrefix 형식: "yyyy-ww" (예: 2025-03)
     */
    public double weeklyTotal(TableModel model, String datePrefix) {
        if (model == null || datePrefix == null) return 0.0;

        double sum = 0.0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String weekKey = toWeekKey(String.valueOf(model.getValueAt(i, 0)));
            if (datePrefix.equals(weekKey)) {
                try {
                    sum += Double.parseDouble(model.getValueAt(i, 5).toString());
                } catch (Exception ignore) {
                }
            }
        }
        return round3(sum);
    }

    /**
     * 월별 합계 계산 (단일 월)
     * monthPrefix 형식: "yyyy-MM" (예: 2025-03)
     */
    public double monthlyTotal(TableModel model, String monthPrefix) {
        if (model == null || monthPrefix == null) return 0.0;

        double sum = 0.0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String monthKey = toMonthKey(String.valueOf(model.getValueAt(i, 0)));
            if (monthPrefix.equals(monthKey)) {
                try {
                    sum += Double.parseDouble(model.getValueAt(i, 5).toString());
                } catch (Exception ignore) {
                }
            }
        }
        return round3(sum);
    }

    /**
     * 날짜별 합계 Map
     * key: yyyy-MM-dd
     */
    public Map<String, Double> dailyTotals(TableModel model) {
        Map<String, Double> map = new LinkedHashMap<>();
        if (model == null) return map;

        for (int i = 0; i < model.getRowCount(); i++) {
            String date = String.valueOf(model.getValueAt(i, 0));
            double val;
            try {
                val = Double.parseDouble(model.getValueAt(i, 5).toString());
            } catch (Exception ignore) {
                continue;
            }
            Double old = map.get(date);
            map.put(date, (old == null ? 0.0 : old) + val);
        }

        roundMap(map);
        return map;
    }

    /**
     * 주별 합계 Map
     * key: yyyy-ww (ISO 주차 기준)
     */
    public Map<String, Double> weeklyTotals(TableModel model) {
        Map<String, Double> map = new LinkedHashMap<>();
        if (model == null) return map;

        for (int i = 0; i < model.getRowCount(); i++) {
            String dateStr = String.valueOf(model.getValueAt(i, 0));
            String weekKey = toWeekKey(dateStr);
            if (weekKey == null) continue;

            double val;
            try {
                val = Double.parseDouble(model.getValueAt(i, 5).toString());
            } catch (Exception ignore) {
                continue;
            }
            Double old = map.get(weekKey);
            map.put(weekKey, (old == null ? 0.0 : old) + val);
        }

        roundMap(map);
        return map;
    }

    /**
     * 월별 합계 Map
     * key: yyyy-MM
     */
    public Map<String, Double> monthlyTotals(TableModel model) {
        Map<String, Double> map = new LinkedHashMap<>();
        if (model == null) return map;

        for (int i = 0; i < model.getRowCount(); i++) {
            String dateStr = String.valueOf(model.getValueAt(i, 0));
            String monthKey = toMonthKey(dateStr);
            if (monthKey == null) continue;

            double val;
            try {
                val = Double.parseDouble(model.getValueAt(i, 5).toString());
            } catch (Exception ignore) {
                continue;
            }
            Double old = map.get(monthKey);
            map.put(monthKey, (old == null ? 0.0 : old) + val);
        }

        roundMap(map);
        return map;
    }

    // =======================
    // 내부 유틸 메서드
    // =======================

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** yyyy-MM-dd 문자열 -> yyyy-ww 주차 키 */
    private String toWeekKey(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FMT);
            WeekFields wf = WeekFields.ISO;
            int week = date.get(wf.weekOfWeekBasedYear());
            int year = date.get(wf.weekBasedYear());
            return String.format("%d-%02d", year, week);
        } catch (Exception e) {
            return null;
        }
    }

    /** yyyy-MM-dd 문자열 -> yyyy-MM 월 키 */
    private String toMonthKey(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FMT);
            int year = date.getYear();
            int month = date.getMonthValue();
            return String.format("%d-%02d", year, month);
        } catch (Exception e) {
            return null;
        }
    }

    private double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private void roundMap(Map<String, Double> map) {
        for (Map.Entry<String, Double> e : map.entrySet()) {
            e.setValue(round3(e.getValue()));
        }
    }
}
