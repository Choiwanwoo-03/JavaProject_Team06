package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class DashBoard_Gui extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

        TotalCO2 = new JLabel("오늘의 총 예상 배출량: 0.000 kg CO₂e", SwingConstants.CENTER);
        TotalCO2.setFont(TotalCO2.getFont().deriveFont(Font.BOLD, 16f));
        p.add(TotalCO2, BorderLayout.NORTH);

        table = new DefaultTableModel(new String[]{
                "날짜", "시간", "동작", "수량", "단위", "CO₂e(kg)"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

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
        updateTotalLabel();
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
        updateTotalLabel();
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
        double sum = 0.0;
        int rowCount = table.getRowCount();
        String today = LocalDate.now().format(DATE_FMT);
        for (int i = 0; i < rowCount; i++) {
            Object dateObj = table.getValueAt(i, 0);
            if (dateObj != null && today.equals(dateObj.toString())) {
                sum += toDouble(table.getValueAt(i, 5));
            }
        }
        return sum;
    }

    private void Filter() {
        String text = tfSearch.getText();
        if (text == null || text.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text)); // 대소문자 무시
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

    private void updateTotalLabel() {
        double total = 0.0;
        int rowCount = table.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            total += toDouble(table.getValueAt(i, 5));
        }
        TotalCO2.setText("오늘의 총 예상 배출량: " + String.format("%.3f", total) + " kg CO₂e");
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

}
