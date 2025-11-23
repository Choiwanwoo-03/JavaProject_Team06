package Filtering;

public class Modifier {
// ============================ 수정 기능 ============================
    private void editSelectedAction(){
        // 선택한 테이블 행의 번호를 가져와서 변수 r에 저장
        int r = tbAction.getSelectedRow();

        // 행을 선택하지 않았다면
        if(r < 0){
            JOptionPane.showMessageDialog(this, "수정할 행을 선택하세요.");
            return;
        }
        // 가저온 번호를 실제 테이블 모델로 변환
        int TableRow = tbAction.convertRowIndexToModel(r);

        // 변경 전 데이터의 값을 가져옴
        String oldDate = table.getValueAt(TableRow, 0).toString();
        String oldTime = table.getValueAt(TableRow, 1).toString();
        String oldAction = table.getValueAt(TableRow, 2).toString();
        String oldqty = table.getValueAt(TableRow, 3).toString();

        // 수정 UI
        JTextField Date = new JTextField(oldDate); // 날짜 변경 텍스트 필드
        JTextField Time = new JTextField(oldTime); // 시간 변경 텍스트 필드
        JComboBox<String> Action = new JComboBox<>(actionDB.keySet().toArray(new String[0])); // 동작 변경 콤보박스
        Action.setSelectedItem(oldAction);
        JSpinner qty = new JSpinner(new SpinnerNumberModel(Double.parseDouble(oldqty), 0.1, 10000, 1)); // 갯수 변경 스피너

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("날짜 (YYYY-MM-DD):"));
        panel.add(Date);
        panel.add(new JLabel("시간 (HH:MM):"));
        panel.add(Time);
        panel.add(new JLabel("행동:"));
        panel.add(Action);
        panel.add(new JLabel("수량:"));
        panel.add(qty);
        panel.add(new JLabel("교통은 km, 에너지 소비는 시간, 쓰레기 종류는 g단위로 계산"));

        int result = JOptionPane.showConfirmDialog(this, panel, "기록 수정", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if(result == JOptionPane.OK_OPTION){
        try {
            // 입력값 파싱 과정
            String newDate = Date.getText().trim();
            String newTime = Time.getText().trim();
            String newAction = (String) Action.getSelectedItem();
            Double newQty = ((Number) qty.getValue()).doubleValue();

            // 포맷의 형식에 맞는지 검증
            LocalDate parseDate = LocalDate.parse(newDate, DATE_FMT);
            LocalTime parseTime = LocalTime.parse(newTime, TIME_FMT);

            // 현재 날짜보다 미래면 오류 발생
            if(parseDate.isAfter(today) || parseTime.isAfter(LocalTime.now())){
                throw new IllegalArgumentException();
            }
            // 유효값 검증
            if(newAction == null || newQty <= 0){
                throw new IllegalArgumentException();
            }

            // CO2e 단위 재계산
            ActionInfo ai = actionDB.get(newAction);
            double newCo2 = ai.co2eFactor * newQty * ai.unitMultiplier;
            String newUnit = ai.unit;

            // 새로운 데이터로 테이블 모델 업데이트
            table.setValueAt(newDate, TableRow, 0);
            table.setValueAt(newTime, TableRow, 1);
            table.setValueAt(newAction, TableRow, 2);
            table.setValueAt(String.format("%.2f", newQty), TableRow, 3);
            table.setValueAt(newUnit, TableRow, 4);
            table.setValueAt(String.format("%.3f", newCo2), TableRow, 5);

            // asyncSaveActions();
            refreshSummary();
            // updateGoalTrackerData();
            JOptionPane.showMessageDialog(this, "기록이 성공적으로 수정되었습니다.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "수정 실패 : 입력 형식이 올바르지 않거나 데이터 오류입니다.\n", "오류", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
}
