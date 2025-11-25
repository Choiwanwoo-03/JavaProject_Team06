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
		recyclingGuideDB.put("골판지류&종이류", 
				"종이의 재질, 택배 상자마다 다른 분리배출 방법을 확인하세요.\n"
				+ "\n"
				+ "일반 골판지 상자 : 테이프 제거 후 종이류로 분리배출\n"
				+ "보냉용 택배 상자(비닐·알루미늄 안감) : 종량제봉투로 배출\n"
				+ "양면 코팅 종이컵, 택배 전표, 영수증 : 종량제봉투로 배출");
		recyclingGuideDB.put("유리류", 
				"깨진 유리는 분리배출이 불가능해요.\n"
				+ "\n"
				+ "깨진 유리 : 신문지로 싸서 소량은 종량제봉투, 다량은 특수규격마대로 배출\n"
				+ "깨지지 않은 음료·소주병 등의 유리병: 행군 후 분리배출\n" 
				+ "내열·크리스탈 유리, 도자기, 판 유리: 특수규격마대 또는 대형폐기물로 배출");
		recyclingGuideDB.put("금속캔", 
				"내용물이 남은 락카나 부탄가스는 폭발 위험이 있어요.\n"
				+ "바람이 잘 통하는 곳에서 노즐을 눌러 가스 제거 후 배출하세요.\n"
				+ "\n"
				+ "일반 캔류 : 내용물 제거 후 분리배출\n"
				+ "내용물 남은 락카, 부탄가스 : 특수규격마대로 배출\n"
				+ "알루미늄 호일 : 종량제봉투로 배출\n"
				+ "건전지 : '전지류'로 따로 분리배출");
		recyclingGuideDB.put("플라스틱&장난감", 
				"플라스틱 같아도 재활용이 되지 않는 품목들을 확인하세요.\n"
				+ "\n"
				+ "플라스틱 완구·문구류(혼합재질) : 종량제봉투로 배출\n"
				+ "옷걸이, 칫솔, CD·DVD : 종량제봉투로 배출\n"
				+ "배터리 포함된 완구 : 초소형 전자제품 배출함\n"
				+ "대형 플라스틱 제품(낚싯대, 유모차, 여행용 캐리어 등) : 대형폐기물 신고 후 배출");
		recyclingGuideDB.put("비닐류", 
				"작아도, 이물질(물, 기름)이 묻어도 폐비닐 분리 배출이 가능해요.\n"
				+ "분리배출이 불가능한 비닐류를 확인하세요.\n"
				+ "\n"
				+ "비닐 : 투명 봉투에 모아 분리배출 (플라스틱류와 혼합배출 불가능)\n"
				+ "과자·커피 비닐, 양파망, 뽁뽁이, (송장을 안 뗀)택배봉투 : 투명 봉투에 모아 분리배출\n"
				+ "랩, 식탁보, 고무장갑, 고무호스, 현수막 : 종량제봉투로 배출\n"
				+ "장판, 돗자리, 천막 : 대형폐기물 신고 후 배출");
		recyclingGuideDB.put("스티로폼", 
				"전자제품 구매 시, 스티로폼은 가급적 구입처로 반납하는 걸 권장해요.\n"
				+ "\n"
				+ "깨끗한 스티로폼 상자 : 라벨 제거 후 분리배출\n"
				+ "음식물 묻은 스티로폼, 과일 포장용 망 : 종량제봉투로 베출\n"
				+ "건축용 내·외장재 스티로폼 : 대형폐기물 신고 후 배출");
		recyclingGuideDB.put("헌옷·신발", 
				"깨끗한 의류는 전용 수거함에 배출이 가능해요.\n"
				+ "\n"
				+ "깨끗한 헌옷/신발 : 폐의류 전용 수거함에 젖지 않도록 배출\n"
				+ "재활용 불가한 헌옷·헌신발·헌가방, 베개 : 종량제봉투로 베출\n"
				+ "솜이불, 쿠션 : 대형폐기물 신고 후 배출");
		recyclingGuideDB.put("형광등·조명", 
				"깨진 형광등·조명은 분리배출이 불가능해요.\n"
				+ "\n"
				+ "형광등 : 형광등 전용 수거함에 분리배출\n"
				+ "깨진 형광등/LED 조명 : 소량은 종량제봉투, 다량은 특수규격마대로 배출");
		recyclingGuideDB.put("헷갈리는 음식물 쓰레기", 
				"음식물이지만 일반쓰레기인 경우를 확인하세요.\n"
				+ "\n"
				+ "어패류 껍데기\n"
				+ "딱딱한 과일 껍데기와 씨앗\n"
				+ "육류와 생선의 뼈\n"
				+ "양파 껍질, 채소 뿌리\n"
				+ "달걀, 메추리 등 알 껍질");
		
		// 환경 팁
		lifeTipsDB.put("냉난방 온도 조절", 
				"여름엔 26℃ 이상, 겨울엔 20℃ 이하로, 계절에 맞는 적정 실내 온도를 유지합니다.\n"
				+ "냉난방 온도를 1℃ 조정할 경우 연간 110kg CO₂를 줄일 수 있으며, 냉난방 비용을 34,000원 줄일 수 있습니다.");
		lifeTipsDB.put("절전형 전등으로 교체",
				"절전형 형광등은 백열등과 비교해 수명이 약 8배 길며, 전력소비가 적습니다.\n"
				+ "백열등(60W)을 형광등(24W)으로 교체 시 연간 17kg의 CO₂를 줄일 수 있습니다.");
		lifeTipsDB.put("대기전력 차단",
				"대기전력은 에너지 사용 기기 전체 이용 전력의 약 10%를 차지합니다.\n"
				+ "멀티탭은 잘 보이는 곳에 두어 손쉽게 대기전력을 차단할 수 있도록 합니다.");
		lifeTipsDB.put("걷기 > 자전거 타기 > 대중교통 이용 생활화",
				"승용차 이용을 일주일에 하루만 줄여도 연간 445kg의 CO₂를 줄일 수 있습니다.");
		lifeTipsDB.put("장바구니 사용",
				"1회용 비닐봉지(연간 160억개)가 분해되는 데100년 이상 걸립니다.\n"
				+ "가정 쓰레기를 철저히 분리만 하여도 연간 188kg의 CO₂를 줄일 수 있습니다.");
		lifeTipsDB.put("친환경 상품 구매",
				"환경마크 제품, 에너지 효율이 높은 제품을 구매합니다.\n"
				+ "친환경 상품 사용으로 가구당 연간 350kg의 CO₂를 줄일 수 있습니다.");
		lifeTipsDB.put("물 사용 시간 줄이기",
				"샤워 시간을 1분 줄이면 가구당 연간 4.3kg의 CO₂를 줄일 수 있습니다.\n"
				+ "빨래를 모아서 하면 가구당 연간 14kg의 CO₂를 줄일 수 있습니다.\n"
				+ "설거지통을 이용(10분)하면 약 80리터의 물을 절약할 수 있습니다.");
		lifeTipsDB.put("음식물 쓰레기 줄이기",
				"연간 버려지는 음식물 쓰레기를 돈으로 환산하면 15조원이 넘습니다.\n"
				+ "몸에도 좋고 온실가스도 덜 발생시키는 제철 식료품, 지역 농산물을 먹습니다.");
		lifeTipsDB.put("컴퓨터 전원 끄기",
				"컴퓨터를 한 시간 켜 놓을 경우 100Wh의 전기가 낭비됩니다.\n"
				+ "컴퓨터 모니터와 하드디스크에 절전모드를 설정합니다.");
		lifeTipsDB.put("계단 이용하기",
				"엘리베이터 1회 이용 시 약 30Wh의 에너지가 소모되며 12.7g의 CO₂가 발생합니다.");
		lifeTipsDB.put("개인 컵 사용하기",
				"하루에 종이컵을 5개 사용하면 연간 20kg의 CO₂가 배출됩니다.");
	}
}
