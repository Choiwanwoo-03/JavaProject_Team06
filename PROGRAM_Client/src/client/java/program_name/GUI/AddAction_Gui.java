package GUI;

import Calculation.CarbonCalculator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class AddAction_Gui extends JPanel {

    // 로그 데이터(날짜, 시간, 동작, 수량, 단위, CO2e) 전송용 인터페이스
    public interface LogActionListener {
        void onActionLogged(Map<String, Object> logData);
    }

    private LogActionListener logListener; // 리스너 객체 저장 필드
    
    // 외부에서 리스너를 등록할 공개 메서드
    public void setLogActionListener(LogActionListener listener) {
        this.logListener = listener;
    }

    // 행동 목록 및 CO2e 계수
    static final Map<String, List<String>> actionData = new HashMap<String, List<String>>();
    static final Map<String, Double> co2eFactors = new HashMap<String, Double>();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    static {
        // 행동 데이터
        actionData.put("교통", Arrays.asList("비행기", "중형 승용차", "기차", "KTX", "버스", "지하철"));
        actionData.put("에너지", Arrays.asList("노트북(문서 작업)", "노트북(활성 및 영상 작업)", "전기밥솥(취사)", "전기밥솥(보온)", "LED 전등", "백열등"));
        actionData.put("쓰레기", Arrays.asList("음식물 쓰레기", "플라스틱류", "종이류", "의류(섬유류)", "캔/금속류", "유리병", "일반 혼합폐기물(생활폐기물)"));

        // 교통 분야 CO2e 계수 데이터 (km)
        co2eFactors.put("비행기", 0.152); // 1km 당 CO2e (kg)
        co2eFactors.put("중형 승용차", 0.21);
        co2eFactors.put("기차", 0.023);
        co2eFactors.put("KTX", 0.026);
        co2eFactors.put("버스", 0.027);
        co2eFactors.put("지하철", 0.001);

        // 에너지 분야 CO2e 계수 데이터 (시간)
        co2eFactors.put("노트북(문서 작업)", 0.012); // 1시간 당 CO2e (kg)
        co2eFactors.put("노트북(활성 및 영상 작업)", 0.031);
        co2eFactors.put("전기밥솥(취사)", 0.254);
        co2eFactors.put("전기밥솥(보온)", 0.052);
        co2eFactors.put("LED 전등", 0.004);
        co2eFactors.put("백열등", 0.06);

        // 쓰레기 분야 CO2e 계수 데이터 (g → kg 변환은 CarbonCalculator에서 처리)
        co2eFactors.put("음식물 쓰레기", 0.45 / 1000.0); // 1g 당 CO2e
        co2eFactors.put("플라스틱류", 2.5 / 1000.0);
        co2eFactors.put("종이류", 0.8 / 1000.0);
        co2eFactors.put("의류(섬유류)", 3.0 / 1000.0);
        co2eFactors.put("캔/금속류", 1.6 / 1000.0);
        co2eFactors.put("유리병", 0.9 / 1000.0);
        co2eFactors.put("일반 혼합폐기물(생활폐기물)", 1.2 / 1000.0);
    }

    private final CarbonCalculator calculator = new CarbonCalculator(co2eFactors);
    
    public AddAction_Gui() {
    	setLayout(new BorderLayout());
		setBorder(new EmptyBorder(12, 12, 12, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("교통", createActionPanel("교통"));
        tabs.addTab("에너지", createActionPanel("에너지"));
        tabs.addTab("쓰레기", createActionPanel("쓰레기"));

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createActionPanel(String tabTitle) {
    	 JPanel form = new JPanel(new GridBagLayout());
		 form.setBorder(new TitledBorder(tabTitle + " 행동 등록"));
    	 
		 GridBagConstraints gc = new GridBagConstraints();
		 gc.insets = new Insets(8, 8, 8, 8);
		 gc.fill = GridBagConstraints.HORIZONTAL;
		 
		 List<String> actions = actionData.get(tabTitle);
		 String[] actionArray = (actions != null) ? actions.toArray(new String[0]) : new String[0];
		 final JComboBox<String> cbAction = new JComboBox<String>(actionArray);
		 
		 final JSpinner spCount = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 100000.0, 1.0));
		 
		 String unit;
		 if ("교통".equals(tabTitle)) {
			 unit = "km";
		 } else if ("에너지".equals(tabTitle)) {
			 unit = "시간";
		 } else if ("쓰레기".equals(tabTitle)) {
			 unit = "g";
		 } else {
			 unit = "";
		 }
		 
		 JButton btnAdd = new JButton("추가");
		 btnAdd.addActionListener(e -> {
			 String actionName = (String) cbAction.getSelectedItem();
			 double count = ((Number) spCount.getValue()).doubleValue();
			 registerAction(tabTitle, actionName, count, unit);
		 });
		 
		 gc.gridx = 0; gc.gridy = 0; form.add(new JLabel("행동 유형"), gc); 
		 gc.gridx = 1; form.add(cbAction, gc);
			
		 gc.gridx = 0; gc.gridy = 1; form.add(new JLabel("수량"), gc);
		 gc.gridx = 1; gc.gridy = 1; form.add(spCount, gc);
		 gc.gridx = 2; gc.gridy = 1; form.add(new JLabel(unit), gc);
		 
		 gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
		 form.add(btnAdd, gc);
			
		 return form;
    }

    // 행동 등록, CO2e 계산, 리스너에게 알림
    private void registerAction(String tabTitle, String actionName, double count, String unit) {
        if (actionName == null || actionName.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "행동을 선택하세요.");
            return;
        }
        if (count <= 0) {
            JOptionPane.showMessageDialog(this, "수량은 0보다 커야 합니다.");
            return;
        }

        // 현재 시간 기준 날짜/시간
        LocalDateTime now = LocalDateTime.now();
        String date = now.toLocalDate().format(DATE_FMT);
        String time = now.toLocalTime().format(TIME_FMT);

        // CO₂e 계산
        double calculatedCo2e = calculator.CalculateActionCarbon(actionName, count, tabTitle);

        // 로그 데이터 구성
        Map<String, Object> logData = new HashMap<String, Object>();
        logData.put("date", date); // 현재 날짜 (YYYY-MM-DD)
        logData.put("time", time); // 현재 시간 (HH:MM)			
        logData.put("type", actionName); // 동작 이름
        logData.put("count", count); // 수량
        logData.put("unit", unit); // 단위 (탭 고정 단위 사용)
        logData.put("result", calculatedCo2e); // 계산된 CO₂e (kg)
        
        // 알림창 표시
        JOptionPane.showMessageDialog(this,
                tabTitle + "의 '" + actionName + "' 기록이 추가되었습니다.\n" +
                        "예상 CO₂e: " + String.format("%.3f", calculatedCo2e) + " kg",
                "동작 등록 완료",
                JOptionPane.INFORMATION_MESSAGE);
        
        // 등록된 리스너에게 로그 데이터(날짜, 시간, 동작, 수량, 단위, CO2e) 전달
        if (logListener != null) {
            logListener.onActionLogged(logData);
        }
    }
}
