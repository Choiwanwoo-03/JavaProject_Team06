package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;

import Visualization.GraphRendering;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.List;

public class DashBoard_Gui extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    
    JTable tbAction;
    JLabel TotalCO2;
    JTextField tfSearch;
    DefaultTableModel table;
    private TableRowSorter<TableModel> sorter;

    public DashBoard_Gui() {
        setLayout(new BorderLayout());
        add(buildDashboardPanel(), BorderLayout.CENTER);
    }

    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 상단: 총 배출량
        TotalCO2 = new JLabel("오늘의 총 예상 배출량: 0.000 kg CO₂e", SwingConstants.CENTER);
        TotalCO2.setFont(TotalCO2.getFont().deriveFont(Font.BOLD, 16f));
        p.add(TotalCO2, BorderLayout.NORTH);

        // 테이블 모델
        table = new DefaultTableModel(new String[]{
                "날짜", "시간", "동작", "수량", "단위", "CO₂e(kg)"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // 테이블
        tbAction = new JTable(table);
        tbAction.setFillsViewportHeight(true);
        sorter = new TableRowSorter<>(table);
        tbAction.setRowSorter(sorter);

        p.add(new JScrollPane(tbAction), BorderLayout.CENTER);

        JPanel South = new JPanel(new BorderLayout(6, 6));

        JPanel Left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Left.add(new JLabel("검색:"));
        tfSearch = new JTextField(20);
        Left.add(tfSearch);
        JButton FilterButton = new JButton("필터");
        FilterButton.addActionListener(e -> Filter());
        Left.add(FilterButton);
        South.add(Left, BorderLayout.WEST);

        JPanel graphPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnDaily = new JButton("일일 그래프");
        JButton btnWeekly = new JButton("주간 그래프");
        JButton btnMonthly = new JButton("월간 그래프");

        btnDaily.addActionListener(e -> showGraphDialog("DAILY"));
        btnWeekly.addActionListener(e -> showGraphDialog("WEEKLY"));
        btnMonthly.addActionListener(e -> showGraphDialog("MONTHLY"));

        graphPanel.add(btnDaily);
        graphPanel.add(btnWeekly);
        graphPanel.add(btnMonthly);

        South.add(graphPanel, BorderLayout.CENTER);
        
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton EditButton = new JButton("수정");
        EditButton.addActionListener(e -> Modify());
        right.add(EditButton);
        JButton DeleteButton = new JButton("삭제");
        DeleteButton.addActionListener(e -> Delete());
        right.add(DeleteButton);

        South.add(right, BorderLayout.EAST);

        p.add(South, BorderLayout.SOUTH);

        return p;
    }
    
    public void addLog(String date, String type, double result, double count, String unit) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        addLogFull(date, time, type, result, count, unit);
    }
    
    private void addLogFull(String date, String time, String type, double result, double count, String unit) {
        Object[] row = {
            date,
            time,
            type,
            String.format("%.1f", count),
            unit,
            String.format("%.3f", result)
        };
        table.addRow(row);
        
        SwingUtilities.invokeLater(() -> {
            updateTotalLabel();
        });
    }

    public void loadFromServer(List<Map<String, Object>> rows) {
        table.setRowCount(0);
        if (rows == null) return;

        for (Map<String, Object> r : rows) {
            table.addRow(new Object[]{
                    r.get("DATE"),
                    r.get("TIME"),
                    r.get("TYPE"),
                    r.get("COUNT"),
                    r.get("UNIT"),
                    r.get("RESULT")
            });
        }
        SwingUtilities.invokeLater(this::updateTotalLabel);
    }

    public void addLogRow(Map<String, Object> actionLog) {
        if (actionLog == null) return;
        table.addRow(new Object[]{
                actionLog.get("date"),
                actionLog.get("time"),
                actionLog.get("type"),
                actionLog.get("count"),
                actionLog.get("unit"),
                actionLog.get("result")
        });
        SwingUtilities.invokeLater(this::updateTotalLabel);
    }

    public List<Map<String, Object>> exportForSave() {
        List<Map<String, Object>> list = new ArrayList<>();
        int rowCount = table.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("DATE", table.getValueAt(i, 0));
            row.put("TIME", table.getValueAt(i, 1));
            row.put("TYPE", table.getValueAt(i, 2));
            row.put("COUNT", toDouble(table.getValueAt(i, 3)));
            row.put("UNIT", table.getValueAt(i, 4));
            row.put("RESULT", toDouble(table.getValueAt(i, 5)));
            list.add(row);
        }
        return list;
    }

    public double calculateTotalEmission() {
    	double total = 0.0;
        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        for (int i = 0; i < table.getRowCount(); i++) {
            try {
                String rowDate = String.valueOf(table.getValueAt(i, 0));
                
                if (todayStr.equals(rowDate)) {
                    double val = parseDoubleSafe(table.getValueAt(i, 5));
                    total += val;
                }
            } catch (Exception ignore) {}
        }
        return total;
    }
    
    private double parseDoubleSafe(Object value) {
        if (value == null) return 0.0;
        try {
            String s = String.valueOf(value).replace(",", "").trim();
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void Filter() {
        String text = tfSearch.getText();
        if (text == null || text.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text)); 
    }

    private void Modify() {
        int viewRow = tbAction.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "수정할 행을 선택하세요.");
            return;
        }
        int row = tbAction.convertRowIndexToModel(viewRow);

        String date = table.getValueAt(row, 0).toString();
        String time = table.getValueAt(row, 1).toString();
        String type = table.getValueAt(row, 2).toString();
        String count = table.getValueAt(row, 3).toString();
        String unit = table.getValueAt(row, 4).toString();
        String result = table.getValueAt(row, 5).toString();

        JTextField tfDate = new JTextField(date);
        JTextField tfTime = new JTextField(time);
        JTextField tfType = new JTextField(type);
        JTextField tfCount = new JTextField(count);
        JTextField tfUnit = new JTextField(unit);
        JTextField tfResult = new JTextField(result);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("날짜")); panel.add(tfDate);
        panel.add(new JLabel("시간")); panel.add(tfTime);
        panel.add(new JLabel("동작")); panel.add(tfType);
        panel.add(new JLabel("수량")); panel.add(tfCount);
        panel.add(new JLabel("단위")); panel.add(tfUnit);
        panel.add(new JLabel("CO₂e(kg)")); panel.add(tfResult);

        int ok = JOptionPane.showConfirmDialog(this, panel, "행 수정",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok == JOptionPane.OK_OPTION) {
            table.setValueAt(tfDate.getText(), row, 0);
            table.setValueAt(tfTime.getText(), row, 1);
            table.setValueAt(tfType.getText(), row, 2);
            table.setValueAt(tfCount.getText(), row, 3);
            table.setValueAt(tfUnit.getText(), row, 4);
            table.setValueAt(tfResult.getText(), row, 5);
            updateTotalLabel();
        }
    }

    private void Delete() {
        int viewRow = tbAction.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "삭제할 행을 선택하세요.");
            return;
        }
        int row = tbAction.convertRowIndexToModel(viewRow);
        table.removeRow(row);
        updateTotalLabel();
    }

    public void updateTotalLabel() {
    	double total = calculateTotalEmission();
        TotalCO2.setText("오늘의 총 예상 배출량: " + String.format("%.3f", total) + " kg CO₂e");
        TotalCO2.repaint();
    }

    private double toDouble(Object v) {
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    public JTable getTbAction() {
        return tbAction;
    }

    public DefaultTableModel getTableModel() {
        return table;
    }

    public JTextField getTfSearch() {
        return tfSearch;
    }

    public TableRowSorter<TableModel> getSorter() {
        return sorter;
    }

    public void asyncSaveActions() {
    }

    public void refreshSummary() {
        updateTotalLabel();
    }
    
    private void showGraphDialog(String mode) {
        Map<String, Double> data = aggregateData(mode);

        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "표시할 데이터가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String title; 

        switch (mode) {
            case "DAILY":
                title = "일일 탄소 배출량";
                break; 
            case "WEEKLY":
                title = "주간 탄소 배출량";
                break;
            case "MONTHLY":
                title = "월간 탄소 배출량";
                break;
            default:
                title = "탄소 배출량 그래프";
                break;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), mode + " 탄소 배출량 그래프", true);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        GraphRendering graphPanel = new GraphRendering(data);
        dialog.add(graphPanel, BorderLayout.CENTER);

        dialog.setVisible(true);
    }

    private Map<String, Double> aggregateData(String mode) {
        Map<String, Double> data = new TreeMap<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < table.getRowCount(); i++) {
            try {
                String dateStr = String.valueOf(table.getValueAt(i, 0));
                double val = parseDoubleSafe(table.getValueAt(i, 5)); 
                
                LocalDate date = LocalDate.parse(dateStr, dateFmt);
                String key = "";
                
                if ("DAILY".equals(mode)) {
                    key = dateStr; 
                } else if ("WEEKLY".equals(mode)) {
                    WeekFields wf = WeekFields.ISO;
                    int week = date.get(wf.weekOfWeekBasedYear());
                    int year = date.get(wf.weekBasedYear());
                    key = String.format("%d-W%02d", year, week);
                } else if ("MONTHLY".equals(mode)) {
                    key = String.format("%d-%02d", date.getYear(), date.getMonthValue());
                }

                data.put(key, data.getOrDefault(key, 0.0) + val);

            } catch (Exception ignored) {}
        }
        return data;
    }

}
