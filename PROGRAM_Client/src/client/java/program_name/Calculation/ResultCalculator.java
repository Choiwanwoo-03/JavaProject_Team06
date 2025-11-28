package Calculation;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ResultCalculator {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Map<String, Double> dailyTotals(List<Object[]> rows, LocalDate start, LocalDate end) {
        Map<String, Double> out = new TreeMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            out.put(d.format(DATE_FMT), 0.0);
        }
        for (Object[] row : rows) {
            String date = String.valueOf(row[0]);
            double co2 = safeDouble(row[5]);
            out.compute(date, (k, v) -> (v == null ? 0.0 : v) + co2);
        }
        return out;
    }

    public Map<String, Double> weeklyTotals(List<Object[]> rows, LocalDate start, LocalDate end) {
        Map<String, Double> daily = dailyTotals(rows, start, end);
        Map<String, Double> out = new TreeMap<>();
        for (String key : daily.keySet()) {
            LocalDate d = LocalDate.parse(key, DATE_FMT);
            String weekKey = d.with(DayOfWeek.MONDAY).format(DATE_FMT) + " (주)";
            out.put(weekKey, out.getOrDefault(weekKey, 0.0) + daily.get(key));
        }
        return out;
    }

    public Map<String, Double> monthlyTotals(List<Object[]> rows, LocalDate start, LocalDate end) {
        Map<String, Double> daily = dailyTotals(rows, start, end);
        Map<String, Double> out = new TreeMap<>();
        for (String key : daily.keySet()) {
            LocalDate d = LocalDate.parse(key, DATE_FMT);
            String monthKey = d.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM")) + " (월)";
            out.put(monthKey, out.getOrDefault(monthKey, 0.0) + daily.get(key));
        }
        return out;
    }

    private double safeDouble(Object x) {
        try { return Double.parseDouble(String.valueOf(x)); } catch (Exception e) { return 0.0; }
    }
}
