package Filtering;

public class Deleter {
// ============================ 삭제 기능(DashBoard_GUI) ============================
    private void Delete(){
        int r = tbAction.getSelectedRow();
        if(r < 0) { JOptionPane.showMessageDialog(this, "삭제할 행을 선택하세요."); return; }
        int TableRow = tbAction.convertRowIndexToModel(r);
        ((DefaultTableModel)tbAction.getModel()).removeRow(TableRow);
        table = (DefaultTableModel) tbAction.getModel();
        asyncSaveActions(); // 저장한 동작 등록 데이터들을 불러오는 메소드
        refreshSummary();
        // updateGoalTrackerData(); // 목표 탭에서 현재 탄소 배출량을 업데이트 하는 메소드 / 목표 탭 구현 완료시 해당 코드에 맞게 수정 예정
    }
}