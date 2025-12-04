package Filtering;

import GUI.DashBoard_Gui;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Modifier {

    private final DashBoard_Gui dashboard;

    public Modifier(DashBoard_Gui dashboard) {
        this.dashboard = dashboard;
    }

    public void Modify() {

        JTable tbAction = dashboard.getTbAction();
        if (tbAction == null) return;

        int viewRow = tbAction.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(null, "수정할 행을 선택하세요.");
            return;
        }

        int row = tbAction.convertRowIndexToModel(viewRow);
        DefaultTableModel table = dashboard.getTableModel();

        // 수정 전 기존 값 가져오기
        String date = String.valueOf(table.getValueAt(row, 0));
        String time = String.valueOf(table.getValueAt(row, 1));
        String type = String.valueOf(table.getValueAt(row, 2));
        String count = String.valueOf(table.getValueAt(row, 3));
        String unit = String.valueOf(table.getValueAt(row, 4));
        String result = String.valueOf(table.getValueAt(row, 5));

        // 수정 입력 폼 구성
        JTextField tfDate = new JTextField(date);
        JTextField tfTime = new JTextField(time);
        JTextField tfType = new JTextField(type);
        JTextField tfCount = new JTextField(count);
        JTextField tfUnit = new JTextField(unit);
        JTextField tfResult = new JTextField(result);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("날짜:")); panel.add(tfDate);
        panel.add(new JLabel("시간:")); panel.add(tfTime);
        panel.add(new JLabel("동작:")); panel.add(tfType);
        panel.add(new JLabel("수량:")); panel.add(tfCount);
        panel.add(new JLabel("단위:")); panel.add(tfUnit);
        panel.add(new JLabel("CO₂e(kg):")); panel.add(tfResult);

        int ok = JOptionPane.showConfirmDialog(
                null, panel, "행 수정",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (ok == JOptionPane.OK_OPTION) {
            table.setValueAt(tfDate.getText(), row, 0);
            table.setValueAt(tfTime.getText(), row, 1);
            table.setValueAt(tfType.getText(), row, 2);
            table.setValueAt(tfCount.getText(), row, 3);
            table.setValueAt(tfUnit.getText(), row, 4);
            table.setValueAt(tfResult.getText(), row, 5);

            dashboard.asyncSaveActions();
            dashboard.refreshSummary();
        }
    }
}
