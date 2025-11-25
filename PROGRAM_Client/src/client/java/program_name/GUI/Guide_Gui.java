package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Guide_Gui extends JPanel {
	private JTextArea taGuide, taLifeTips; // 텍스트 출력
	private JList<String> lstRecycleGuide, lstLifeTips; // 리스트 선택
	private Map<String, String> recyclingGuideDB = new LinkedHashMap<>(); // 분리수거 가이드
	private Map<String, String> lifeTipsDB = new LinkedHashMap<>(); // 환경 팁
	
	 public Guide_Gui() {
		 loadData();
		 ChooseGuide();
	 }
	
	void ChooseGuide() {
		// 가이드 전체 화면: 좌/우 큰 패널
		this.setLayout(new GridLayout(1, 2, 15, 15));
		this.setBorder(new EmptyBorder(12, 12, 12, 12));
		
		// 왼쪽 화면: 분리수거 가이드 목록, 분리수거 가이드 내용
		JPanel leftSection = new JPanel(new BorderLayout(8, 8));
		
		DefaultListModel<String> lmRecycle = new DefaultListModel<>();
		for (String k : recyclingGuideDB.keySet()) lmRecycle.addElement(k);
		
		// 분리수거 항목 리스트
		lstRecycleGuide = new JList<>(lmRecycle);
		lstRecycleGuide.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// 분리수거 가이드 출력 패널
		JPanel guideDisplayPanel = new JPanel(new BorderLayout(6, 6));
		guideDisplayPanel.setBorder(new TitledBorder("분리수거 가이드"));
		
		// 분리수거 가이드 내용 출력 텍스트 영역
		taGuide = new JTextArea();
		taGuide.setEditable(false);
		taGuide.setLineWrap(true); // 자동 줄 넘김
		taGuide.setWrapStyleWord(true); // 단어 단위 줄바꿈
		
		guideDisplayPanel.add(new JScrollPane(taGuide), BorderLayout.CENTER);
		
		lstRecycleGuide.addListSelectionListener(ev -> {
			String key = lstRecycleGuide.getSelectedValue();
			if (key != null) taGuide.setText(recyclingGuideDB.get(key));
		});
		
		// 좌측 패널 구성: 리스트, 텍스트 패널 병합
		leftSection.add(new JScrollPane(lstRecycleGuide), BorderLayout.WEST);
		leftSection.add(guideDisplayPanel, BorderLayout.CENTER);
		
		
		// 오른쪽 화면: 환경 팁, 환경 팁 내용
		JPanel rightSection = new JPanel(new BorderLayout(8, 8));
		
		DefaultListModel<String> lmTips = new DefaultListModel<>();
		for (String k : lifeTipsDB.keySet()) lmTips.addElement(k);
		
		// 환경 팁 리스트
		lstLifeTips = new JList<>(lmTips);
		lstLifeTips.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// 환경 팁 출력 패널
		JPanel tipsDisplayPanel = new JPanel(new BorderLayout(6, 6));
		tipsDisplayPanel.setBorder(new TitledBorder("일상생활 환경 팁"));
		
		// 환경 팁 내용 출력 텍스트 영역
		taLifeTips = new JTextArea();
		taLifeTips.setEditable(false);
		taLifeTips.setLineWrap(true);
		taLifeTips.setWrapStyleWord(true);
		
		if (!lifeTipsDB.isEmpty()) {
			String firstKey = lifeTipsDB.keySet().iterator().next();
			taLifeTips.setText(lifeTipsDB.get(firstKey));
			lstLifeTips.setSelectedValue(firstKey, true);
		}
		
		tipsDisplayPanel.add(new JScrollPane(taLifeTips), BorderLayout.CENTER);
		
		lstLifeTips.addListSelectionListener(ev -> {
			String key = lstLifeTips.getSelectedValue();
			if (key != null) taLifeTips.setText(lifeTipsDB.get(key));
		});
		
		JScrollPane scrollTipsList = new JScrollPane(lstLifeTips);
		scrollTipsList.setPreferredSize(new Dimension(150, 0));
		
		// 오른쪽 패널 구성: 리스트, 텍스트 패널 병합
		rightSection.add(scrollTipsList, BorderLayout.WEST);
		rightSection.add(tipsDisplayPanel, BorderLayout.CENTER);
		
        this.add(leftSection);
        this.add(rightSection);
	}
	
	// 분리수거 가이드, 환경 팁 데이터:  키, 내용
	private void loadData() {
		// 분리수거 가이드
		recyclingGuideDB.put("플라스틱", "내용");
		recyclingGuideDB.put("캔", "내용");
		recyclingGuideDB.put("유리", "내용");
		
		// 환경 팁
		lifeTipsDB.put("대중교통 이용", "내용");
		lifeTipsDB.put("재사용 용기 사용", "내용");
		lifeTipsDB.put("대기전력 차단", "내용");
	}
}
