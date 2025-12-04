package GUI;


import Emoticon.EmoticonManager;
import Visualization.GraphRendering;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * 목표 달성 트래커 GUI (완성용)
 * - 미션 3개: (1) 고정 미션(1인 1일 권장량 미초과), (2)(3) 이모티콘 주제 1개 선택의 미션 2개
 * - 오늘 배출량은 DashBoard_Gui 테이블 합산(수동 입력 X)
 * - 모든 미션 완료 시 해당 이모티콘 해금(EmoticonManager.unlock)
 * - 일/주/월 그래프 버튼으로 배출량 집계 시각화
 *
 * 연동:
 *   GoalTracker_Gui_test gt = new GoalTracker_Gui_test(dash, emo, () -> 20.0);
 *   // dash.addLogRow(...) 로 로그가 쌓이면 gt.refreshFromDashboard() 호출해 최신 상태 반영
 */
public class AchieveGoal_Gui_test extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DashBoard_Gui dashboard;               // 오늘 배출량 및 집계 원본(테이블)
    private final EmoticonManager emoticonManager;       // 보상 해금
    private final Supplier<Double> recommendedGoal;      // 1인 1일 권장 목표(kg)

    // UI
    private final JLabel lblGoal = new JLabel();
    private final JLabel lblToday = new JLabel();
    private final JLabel lblAchv = new JLabel();
    private final JLabel lblImpact = new JLabel();

    private final JCheckBox cbFixedGoal = new JCheckBox("1인 1일 권장량 미초과");
    private final JCheckBox cbMission1 = new JCheckBox();
    private final JCheckBox cbMission2 = new JCheckBox();
    private final JLabel lblEmojiTitle = new JLabel(); // 선택된 이모티콘 주제
    private final JTextArea taTips = new JTextArea(4, 24);

    // 현재 선택된 이모티콘 주제
    private String currentEmojiName = null;

    // 이모티콘 → 미션 2개 매핑(접근성 높은 실천 과제)
    private static final Map<String, String[]> EMOJI_MISSIONS = new LinkedHashMap<>();
    static {
        EMOJI_MISSIONS.put("미니 나무", new String[]{"전자문서로 과제·메모 작성하기", "노트/종이 양면 활용하기"});
        EMOJI_MISSIONS.put("재활용 마스터", new String[]{"플라스틱 용기 세척 후 분리배출하기", "캔·유리 재질/색상별로 분리하기"});
        EMOJI_MISSIONS.put("물방울 친구", new String[]{"샤워 5분 이내로 줄이기", "세탁물 모아서 1회만 세탁하기"});
        EMOJI_MISSIONS.put("절약 전구", new String[]{"외출 전 모든 조명 끄기", "사용하지 않는 플러그 뽑기"});
        EMOJI_MISSIONS.put("친환경 라이더", new String[]{"3km 이내 도보·자전거로 이동하기", "대중교통 이용 기록 추가하기"});
        EMOJI_MISSIONS.put("장바구니 마스터", new String[]{"장보기 시 장바구니 사용 인증하기", "개인 텀블러 사용하기"});
        EMOJI_MISSIONS.put("잔반 제로", new String[]{"남김 없이 식사 완료하기", "유통기한 임박 식품 먼저 소비하기"});
    }

    public AchieveGoal_Gui_test(DashBoard_Gui dashboard,
                                EmoticonManager emoticonManager,
                                Supplier<Double> recommendedGoalSupplier) {
        super(new BorderLayout(12,12));
        this.dashboard = dashboard;
        this.emoticonManager = emoticonManager;
        this.recommendedGoal = recommendedGoalSupplier;

        setBorder(new TitledBorder("목표 달성 트래커"));

        // 좌: 목표/현황 + 미션
        JPanel left = new JPanel(new BorderLayout(10,10));
        left.add(buildGoalPanel(), BorderLayout.NORTH);
        left.add(buildMissionPanel(), BorderLayout.CENTER);

        // 우: 그래프 버튼
        JPanel right = buildGraphPanel();

        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        // 최초 롤/갱신
        rollRandomEmojiAndMissions();
        refreshFromDashboard();
    }

    /** 목표/현황 패널 */
    private JComponent buildGoalPanel() {
        JPanel p = new JPanel(new GridLayout(4,1,6,6));
        Font f = getFont().deriveFont(Font.BOLD, 14f);
        lblGoal.setFont(f);
        lblToday.setFont(f);
        lblAchv.setFont(f);
        lblImpact.setFont(f);

        p.setBorder(new TitledBorder("일일 목표/현황"));
        p.add(lblGoal);
        p.add(lblToday);
        p.add(lblAchv);
        p.add(lblImpact);
        return p;
    }

    /** 미션 패널 (고정 + 랜덤2) */
    private JComponent buildMissionPanel() {
        JPanel box = new JPanel(new BorderLayout(6,6));
        box.setBorder(new TitledBorder("오늘의 미션 (3개)"));

        // 현재 이모티콘 주제
        JPanel title = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblEmojiTitle.setText("이모티콘: -");
        title.add(lblEmojiTitle);

        // 체크박스들
        JPanel mids = new JPanel();
        mids.setLayout(new BoxLayout(mids, BoxLayout.Y_AXIS));
        cbFixedGoal.setEnabled(false); // 자동 체크(목표 충족 시)
        mids.add(cbFixedGoal);
        mids.add(cbMission1);
        mids.add(cbMission2);

        // 팁/가이드
        taTips.setEditable(false);
        taTips.setLineWrap(true);
        taTips.setWrapStyleWord(true);
        taTips.setBorder(BorderFactory.createTitledBorder("가이드/팁"));

        // 하단 버튼
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnReroll = new JButton("미션 다시 뽑기");
        btnReroll.addActionListener(e -> {
            rollRandomEmojiAndMissions();
            autoCheckFixedGoal();
        });
        JButton btnReward = new JButton("보상 수령");
        btnReward.addActionListener(e -> tryReward());

        south.add(btnReroll);
        south.add(btnReward);

        box.add(title, BorderLayout.NORTH);
        box.add(mids, BorderLayout.CENTER);
        box.add(new JScrollPane(taTips), BorderLayout.SOUTH);
        box.add(south, BorderLayout.PAGE_END);

        return box;
    }

    /** 그래프 버튼 패널 */
    private JPanel buildGraphPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new TitledBorder("그래프 보기"));

        JButton btnDaily = new JButton("일별 그래프");
        btnDaily.addActionListener(e -> showGraphDaily());
        JButton btnWeekly = new JButton("주간 그래프");
        btnWeekly.addActionListener(e -> showGraphWeekly());
        JButton btnMonthly = new JButton("월간 그래프");
        btnMonthly.addActionListener(e -> showGraphMonthly());

        p.add(btnDaily); p.add(Box.createVerticalStrut(8));
        p.add(btnWeekly); p.add(Box.createVerticalStrut(8));
        p.add(btnMonthly);
        return p;
    }

    /** 대시보드 테이블로부터 현재 상태 재계산 → 라벨/고정미션 갱신 */
    public void refreshFromDashboard() {
        double goal = safeGoal();
        double today = calcTodayEmissionFromDashboard();

        lblGoal.setText("하루 목표(kg): " + String.format("%.1f", goal));
        lblToday.setText("오늘 배출량(kg): " + String.format("%.3f", today));

        double percent = (today <= goal) ? (100.0 * (goal - today) / goal) : 0.0;
        if (percent < 0) percent = 0.0;
        lblAchv.setText("절감 달성률: " + String.format("%.1f%%", percent));

        if (today <= goal) {
            double saved = Math.max(0.0, goal - today);
            int tree = (int)(saved / 21.77); // 21.77kg CO₂ ≒ 나무 1그루
            lblImpact.setText("🌱 절감 " + String.format("%.1f", saved) + "kg → 나무 " + tree + "그루 효과");
        } else {
            lblImpact.setText("🚨 목표 초과! 내일 더 절약해봐요");
        }

        autoCheckFixedGoal();
    }

    /** 현재 랜덤 이모티콘 주제에서 두 개 미션 배치 */
    private void rollRandomEmojiAndMissions() {
        List<String> keys = new ArrayList<>(EMOJI_MISSIONS.keySet());
        Collections.shuffle(keys);
        currentEmojiName = keys.get(0);

        String[] missions = EMOJI_MISSIONS.get(currentEmojiName);
        cbMission1.setText("① " + missions[0]);
        cbMission2.setText("② " + missions[1]);
        cbMission1.setSelected(false);
        cbMission2.setSelected(false);

        lblEmojiTitle.setText("이모티콘: " + currentEmojiName);
        taTips.setText("- 위 두 가지 실천 항목 중 하나 이상 완료해 보세요.\n- 고정 미션은 오늘 배출량이 권장 목표 이하일 때 자동 달성됩니다.");
    }

    /** 고정 미션 체크(자동) */
    private void autoCheckFixedGoal() {
        double goal = safeGoal();
        double today = calcTodayEmissionFromDashboard();
        cbFixedGoal.setSelected(today <= goal);
    }

    /** 보상 수령: 미션 3개 모두 완료 시 이모티콘 해금 */
    private void tryReward() {
        if (cbFixedGoal.isSelected() && cbMission1.isSelected() && cbMission2.isSelected()) {
            emoticonManager.unlock(currentEmojiName);
            JOptionPane.showMessageDialog(this,
                    "축하합니다! '" + currentEmojiName + "' 이모티콘이 해금되었습니다.",
                    "보상 지급", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "아직 완료되지 않은 미션이 있습니다.\n(고정 미션 + 2개 실천 미션 모두 체크 필요)",
                    "보상 지급 불가", JOptionPane.WARNING_MESSAGE);
        }
    }

    /* ===================== 그래프 표시 ===================== */

    private void showGraphDaily() {
        Map<String, Double> daily = aggregateDaily();
        showGraphFrame("일별 배출량", daily);
    }

    private void showGraphWeekly() {
        Map<String, Double> weekly = aggregateWeekly();
        showGraphFrame("주간 배출량", weekly);
    }

    private void showGraphMonthly() {
        Map<String, Double> monthly = aggregateMonthly();
        showGraphFrame("월간 배출량", monthly);
    }

    private void showGraphFrame(String title, Map<String, Double> data) {
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.add(new GraphRendering(data));
        f.pack();
        f.setLocationRelativeTo(this);
        f.setVisible(true);
    }

    /* ===================== 집계 유틸 (DashBoard_Gui 테이블 → Map) ===================== */

    private double calcTodayEmissionFromDashboard() {
        DefaultTableModel model = dashboard.getTableModel();
        String today = LocalDate.now().format(DATE_FMT);
        double sum = 0.0;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object date = model.getValueAt(i, 0);
            Object val  = model.getValueAt(i, 5);
            if (date != null && today.equals(String.valueOf(date))) {
                sum += toDouble(val);
            }
        }
        return sum;
    }

    /** 최근 30일(없으면 0) 일별 합계 */
    private Map<String, Double> aggregateDaily() {
        DefaultTableModel model = dashboard.getTableModel();
        Map<String, Double> map = new TreeMap<>();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            map.put(d.format(DATE_FMT), 0.0);
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            String date = String.valueOf(model.getValueAt(i, 0));
            double v = toDouble(model.getValueAt(i, 5));
            map.put(date, map.getOrDefault(date, 0.0) + v);
        }
        return map;
    }

    /** 최근 12주 (월요일 시작키 "yyyy-MM-dd(주)") */
    private Map<String, Double> aggregateWeekly() {
        DefaultTableModel model = dashboard.getTableModel();
        Map<String, Double> map = new TreeMap<>();
        WeekFields wf = WeekFields.of(Locale.KOREA);

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusWeeks(11).with(wf.dayOfWeek(), 1); // 월요일
        LocalDate end = today;

        for (LocalDate w = start; !w.isAfter(end); w = w.plusWeeks(1)) {
            map.put(w.format(DATE_FMT) + "(주)", 0.0);
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            String dateStr = String.valueOf(model.getValueAt(i, 0));
            LocalDate d = parseDate(dateStr);
            if (d == null) continue;
            LocalDate wk = d.with(wf.dayOfWeek(), 1);
            String key = wk.format(DATE_FMT) + "(주)";
            double v = toDouble(model.getValueAt(i, 5));
            map.put(key, map.getOrDefault(key, 0.0) + v);
        }
        return map;
    }

    /** 최근 12개월 ("yyyy-MM(월)") */
    private Map<String, Double> aggregateMonthly() {
        DefaultTableModel model = dashboard.getTableModel();
        Map<String, Double> map = new TreeMap<>();

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusMonths(11).withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(1);

        DateTimeFormatter MFMT = DateTimeFormatter.ofPattern("yyyy-MM");
        for (LocalDate m = start; !m.isAfter(end); m = m.plusMonths(1)) {
            map.put(m.format(MFMT) + "(월)", 0.0);
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            String dateStr = String.valueOf(model.getValueAt(i, 0));
            LocalDate d = parseDate(dateStr);
            if (d == null) continue;
            String key = d.withDayOfMonth(1).format(MFMT) + "(월)";
            double v = toDouble(model.getValueAt(i, 5));
            map.put(key, map.getOrDefault(key, 0.0) + v);
        }
        return map;
    }

    private LocalDate parseDate(String s) {
        try { return LocalDate.parse(s, DATE_FMT); }
        catch (Exception e) { return null; }
    }

    private double toDouble(Object v) {
        try { return Double.parseDouble(String.valueOf(v)); }
        catch (Exception e) { return 0.0; }
    }

    private double safeGoal() {
        try {
            Double g = recommendedGoal.get();
            return (g != null && g > 0) ? g : 20.0;
        } catch (Exception e) {
            return 20.0;
        }
    }
}

