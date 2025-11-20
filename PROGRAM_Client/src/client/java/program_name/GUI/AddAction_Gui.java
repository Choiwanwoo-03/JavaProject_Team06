package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddAction_Gui extends JPanel {
	
	// 로그 데이터(날짜, 시간, 동작, 수량, 단위, CO2e) 전송용 내부 인터페이스
	public interface LogActionListener {
        void onActionLogged(Map<String, Object> actionLog);
    }
	
	private LogActionListener logListener; // 리스너 객체 저장 필드
	private static final Map<String, List<String>> actionData = new HashMap<>(); // 행동 데이터
	private static final Map<String, Double> co2eFactors = new HashMap<>(); // CO2e 계수 데이터
	private static final Map<String, String> tabUnits = new HashMap<>(); // 서브 탭 고정 단위
	
	static {
		// 행동 데이터
		actionData.put("교통", List.of("비행기", "중형 승용차", "기차", "KTX", "버스", "지하철"));
		actionData.put("에너지", List.of("노트북(문서 작업)", "노트북(활성 및 영상 작업)", "전기밥솥(취사)", "전기밥솥(보온)", "LED 전등", "백열등"));
		actionData.put("쓰레기", List.of("음식물 쓰레기", "플라스틱류", "종이류", "의류(섬유류)", "캔\금속류", "유리병", "일반 혼합폐기물(생활폐기물)"));
		
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
		co2eFactors.put("전기밥솥(취사)", 0.353);
		co2eFactors.put("전기밥솥(보온)", 0.017);
		co2eFactors.put("LED 전등", 0.004);
		co2eFactors.put("백열등", 0.025);
		
		// 쓰레기 분야 CO2e 계수 데이터 (g)
		co2eFactors.put("음식물 쓰레기", 0.006); // 100g 당 CO2e (kg)
		co2eFactors.put("플라스틱류", 0.25);
		co2eFactors.put("종이류", 0.04);
		co2eFactors.put("의류(섬유류)", 0.15);
		co2eFactors.put("캔/금속류", 0.02);
		co2eFactors.put("유리병", 0.01);
		co2eFactors.put("일반 혼합폐기물(생활폐기물)", 0.1);
		
		// 서브 탭 고정 단위
		tabUnits.put("교통", "km");
	    tabUnits.put("에너지", "시간");
	    tabUnits.put("쓰레기", "g");
	}
	
	private JTabbedPane tabbedPane;
	
	// 탭 1 (교통) 내부 컴포넌트
	private JComboBox<String> comboBox1; // 탭 1 (교통): 행동 유형 선택
	private JSpinner spinner1; // 탭 1 (교통): 수량 입력
	private JButton button1; // 탭 1 (교통): 기록 추가 버튼
	
	// 탭 2 (에너지) 내부 컴포넌트
	private JComboBox<String> comboBox2; // 탭 2 (에너지): 행동 유형 선택
	private JSpinner spinner2; // 탭 2 (에너지): 수량 입력
	private JButton button2; // 탭 2 (에너지): 기록 추가 버튼
	
	// 탭 3 (쓰레기) 내부 컴포넌트
	private JComboBox<String> comboBox3; // 탭 3 (쓰레기): 행동 유형 선택
	private JSpinner spinner3; // 탭 3 (쓰레기): 수량 입력
	private JButton button3; // 탭 3 (쓰레기): 기록 추가 버튼
	
	public AddAction_Gui() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(12, 12, 12, 12));
		
		tabbedPane = new JTabbedPane();
		
		tabbedPane.addTab("교통", createSubPanel("교통", tabUnits.get("교통"), 1.0, 10000.0, 1.0)); // 유형, 단위, 초깃값, 최댓값, 버튼 증감 단위
		tabbedPane.addTab("에너지", createSubPanel("에너지", tabUnits.get("에너지"), 1.0, 10000.0, 0.5));
		tabbedPane.addTab("쓰레기", createSubPanel("쓰레기", tabUnits.get("쓰레기"), 100.0, 10000.0, 50.0));
		
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	// 외부에서 리스너를 등록할 공개 메서드
	public void setLogActionListener(LogActionListener listener) {
        this.logListener = listener;
    }
	
	// 서브 탭 내부 GUI 구성 메서드
	private JPanel createSubPanel(String actionType, String unitLabel, double initialValue, double maxValue, double stepSize) {
		JPanel form = new JPanel(new GridBagLayout());
		form.setBorder(new TitleBorder());
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(8, 8, 8, 8);
		gc.fill = GridBagConstraints.HORIZONTAL;
		
		// 콤보박스 초기화
		String[] actions = ChooseAction(actionType);
		JComboBox<String> cb = new JComboBox<>(actions);
		
		// 스피너 초기화
		SpinnerNumberModel model = new SpinnerNumberModel(initialValue, 0.1, maxValue, stepSize); // 초깃값, 최솟값(0.1), 최댓값, 버튼 증감 단위
		JSpinner spn = new JSpinner(model);
		
		// 버튼 초기화
		JButton btn = new JButton(actionType + " 기록 추가");
		
		if (actionType.equals("교통")) {
	        this.comboBox1 = cb;
	        this.spinner1 = spn;
	        this.button1 = btn;
	        this.button1.addActionListener(e -> AddAction());
	    } else if (actionType.equals("에너지")) {
	        this.comboBox2 = cb;
	        this.spinner2 = spn;
	        this.button2 = btn;
	        this.button2.addActionListener(e -> AddAction());
	    } else { // 쓰레기
	        this.comboBox3 = cb;
	        this.spinner3 = spn;
	        this.button3 = btn;
	        this.button3.addActionListener(e -> AddAction());
	    }
		
		gc.gridx = 0; gc.gridy = 0; form.add(new JLabel("행동 유형"), gc); 
		gc.gridx = 1; form.add(cb, gc);
		
		gc.gridx = 0; gc.gridy = 1; form.add(spn, gc);
		gc.gridx = 1; form.add(new JLabel(unitLabel), gc);
		
//		gc.gridx = 0; gc.gridy = 1; form.add(new JLabel("수량 (" + unitLabel + ")"), gc); // ⬅️ 먼저 배치 (왼쪽)
//		gc.gridx = 1; form.add(spn, gc);
		
		gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
		form.add(btn, gc);
		
		return form;
	}
	
	// 서브 탭 선택 시, 해당하는 행동 데이터 로드
	public String[] ChooseAction(String actionType) {
		List<String> actions = actionData.get(actionType);
		
		if (actions == null) {
			return new String[]{"데이터 없음"};
		}
		
		return actions.toArray(new String[0]);
	}
	
	// 버튼 클릭 시 DB, 다른 클래스에서 사용할 수 있도록 데이터 추출
	public void AddAction() {
		int selectedTabIndex = tabbedPane.getSelectedIndex();
		
		LocalDateTime now = LocalDateTime.now();
		String currentDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
		String tabTitle = tabbedPane.getTitleAt(selectedTabIndex); // 대분류 (교통, 에너지, 쓰레기)
		
		String actionName = null; // 동작 (행동 유형)
		double count = 0.0; // 수량
		
		// 현재 선택된 탭의 콤보박스와 스피너에서 데이터 추출
		if (selectedTabIndex == 0 && comboBox1 != null && spinner1 != null) { // 교통
			actionName = (String) comboBox1.getSelectedItem();
			count = ((Number) spinner1.getValue()).doubleValue();
		} else if (selectedTabIndex == 1 && comboBox2 != null && spinner2 != null) { // 에너지
			actionName = (String) comboBox2.getSelectedItem();
			count = ((Number) spinner2.getValue()).doubleValue();
		} else if (selectedTabIndex == 2 && comboBox3 != null && spinner3 != null) { // 쓰레기
			actionName = (String) comboBox3.getSelectedItem();
			count = ((Number) spinner3.getValue()).doubleValue();
		}
		
		// 유효성 검사
		if (actionName == null || count <= 0.0) {
			JOptionPane.showMessageDialog(this, "유효한 행동 유형과 수량을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// 단위 및 CO2e 배출 계수 로드
		String unit = tabUnits.getOrDefault(tabTitle, "N/A");
		Double co2eFactor = co2eFactors.get(actionName);
		
		if (co2eFactor == null) {
	        JOptionPane.showMessageDialog(this, 
	            "'" + actionName + "'에 대한 CO₂e 배출 계수 정보가 없습니다. (배출량 계산 불가)", 
	            "데이터 오류", JOptionPane.ERROR_MESSAGE);
	        return;
	    }
		
		// 배출한 CO2e 양 계산
		double adCount = count
		
		if (tabTitle.equals("쓰레기")) {
			adCount = count
		    adCount = count / 100.0; 
		}
		
		double calculatedCo2e = adCount * co2eFactor;
		
		// 데이터 추출
		Map<String, Object> logData = new HashMap<>();
		logData.put("date", currentDate); // 현재 날짜 (YYYY-MM-DD)
		logData.put("time", currentTime); // 현재 시간 (HH:MM)
		logData.put("type", actionName); // 동작
		logData.put("count", count); // 수량
		logData.put("unit", unit); // 단위 (탭 고정 단위 사용)
		logData.put("result", calculatedCo2e); // 계산된 CO2e (kg)
		
		// 알림창 표시
		JOptionPane.showMessageDialog(this, 
		        tabTitle + "의 '" + actionName + "' 기록이 추가되었습니다.\n" + 
		        "예상 CO₂e: " + String.format("%.3f", calculatedCo2e) + " kg",
		        JOptionPane.INFORMATION_MESSAGE);
		
		// 등록된 리스너에게 로그 데이터(날짜, 시간, 동작, 수량, 단위, CO2e) 전달
		if (logListener != null) {
            logListener.onActionLogged(logData);
        }
	}
}