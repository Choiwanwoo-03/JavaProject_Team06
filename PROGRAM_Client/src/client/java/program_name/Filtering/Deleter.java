package Filtering;

import GUI.DashBoard_Gui;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Deleter {

    private final DashBoard_Gui dashboard;

    public Deleter(DashBoard_Gui dashboard) {
        this.dashboard = dashboard;
    }

    public void Delete() {

        JTable tbAction = dashboard.getTbAction(); // getter 통해 접근
        int r = tbAction.getSelectedRow();

        if (r < 0) {
            JOptionPane.showMessageDialog(null, "삭제할 행을 선택하세요.");
            return;
        }

        int modelRow = tbAction.convertRowIndexToModel(r);
        DefaultTableModel table = (DefaultTableModel) tbAction.getModel();

        table.removeRow(modelRow);

        dashboard.asyncSaveActions();
        dashboard.refreshSummary();
    }
}
