package Filtering;

public class Filter {
// ============================ 필터 기능 (DashBoard_GUI) ============================
    private void Filter(){
        String q = tfSearch.getText().trim().toLowerCase();
        // if(q.isEmpty()) {loadAllDataFromServer(); return;}

        DefaultTableModel currentModel = (DefaultTableModel) tbAction.getModel();
        DefaultTableModel filtered = new DefaultTableModel(new String[]{
                    "날짜", "시간", "동작", "수량", "단위", "CO₂e(kg)"
        }, 0){ 
            public boolean isCellEditable(int r, int c){ return false; }
        };

        for (int r = 0; r < currentModel.getRowCount(); r++){
            String action = currentModel.getValueAt(r, 2).toString().toLowerCase();
            if (action.contains(q)){
                filtered.addRow(new Object[] {
                    currentModel.getValueAt(r, 0),
                    currentModel.getValueAt(r, 1),
                    currentModel.getValueAt(r, 2),
                    currentModel.getValueAt(r, 3),
                    currentModel.getValueAt(r, 4),
                    currentModel.getValueAt(r, 5),
                });
            }
        }
        tbAction.setModel(filtered);
        table = filtered;
    }
}