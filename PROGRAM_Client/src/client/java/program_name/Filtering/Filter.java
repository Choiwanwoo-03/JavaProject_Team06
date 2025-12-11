package Filtering;

import GUI.DashBoard_Gui;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class Filter {

    private final DashBoard_Gui dashboard;

    public Filter(DashBoard_Gui dashboard) {
        this.dashboard = dashboard;
    }

    public void applyFilter() {

        JTextField tfSearch = dashboard.getTfSearch();   // getter 사용
        TableRowSorter<TableModel> sorter = dashboard.getSorter();
        JTable tbAction = dashboard.getTbAction();

        if (tfSearch == null || sorter == null || tbAction == null) {
            return;
        }

        String text = tfSearch.getText();
        if (text == null || text.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
    }
}
