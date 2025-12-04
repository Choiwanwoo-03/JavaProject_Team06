package GUI;

import Emoticon.EmoticonManager;
import Emoticon.OpeningEmoticon_test;
import Visualization.GraphRendering_test;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class AchieveGoal_Gui_test extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DashBoard_Gui dashboard;         // 오늘 배출량 원본
    private final EmoticonManager emoticonManager; // 도감/해금 상태
    private final Supplier<Double> recommendedGoal; // 권장 목표(kg) 공급자
    private final OpeningEmoticon_test rewardHandler; // 보상 이벤트 처리자

    // 상태 라벨
    private final JLabel lblGoal = new JLabel();
    private final JLabel lblToday = new JLabel();
    private final JLabel lblAchv = new JLabel();
    private final JLabel lblImpact = new JLabel();

    // 미션 UI
    private final JCheckBox cbFixedGoal = new JCheckBox("1인 1일 권장량 미초과");
    private final JCheckBox cbMission1 = new JCheckBox();
    private final JCheckBox cbMission2 = new JCheckBox();
    private final JLabel lblEmojiTitle = new JLabel("이모티콘: -");
    private final JTextArea taTips = new JTextArea(4, 24);

    // 현재 선택된 이모티콘 주제명
    private String currentEmojiName = null;

    // 접근성 좋은 2미션 매핑
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
        this.rewardHandler = new OpeningEmoticon_test(emoticonManager);

        setBorder(new TitledBorder("목표 달성 트래커"));

        // 좌측(목표/현황 + 미션)
        JPanel left = new JPanel(new BorderLayout(10,10));
        left.add(buildGoalPanel(), BorderLayout.NORTH);
        left.add(buildMissionPanel(), BorderLayout.CENTER);

        // 우측(그래프 버튼)
        JPanel right = buildGraphButtons();

        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        // 초기 미션 뽑기 & 라벨 갱신
        rollRandomEmojiAndMissions();
        refreshFromDashboard();
    }

    private JComponent buildGoalPanel() {
        JPanel p = new JPanel(new GridLayout(4,1,6,6));
        Font f = getFont().deriveFont(Font.BOLD, 14f);
        for (JLabel l : new JLabel[]{lblGoal,lblToday,lblAchv,lblImpact}) l.setFont(f);
        p.setBorder(new TitledBorder("일일 목표/현황"));
        p.add(lblGoal); p.add(lblToday); p.add(lblAchv); p.add(lblImpact);
        return p;
    }

    private JComponent buildMissionPanel() {
        JPanel box = new JPanel(new BorderLayout(6,6));
        box.setBorder(new TitledBorder("오늘의 미션 (3개)"));

        JPanel title = new JPanel(new FlowLayout(FlowLayout.LEFT));
        title.add(lblEmojiTitle);

        JPanel mids = new JPanel();
        mids.setLayout(new BoxLayout(mids, BoxLayout.Y_AXIS));
        cbFixedGoal.setEnabled(false); // 자동 체크
        mids.add(cbFixedGoal);
        mids.add(cbMission1);
        mids.add(cbMission2);

        taTips.setEditable(false);
        taTips.setLineWrap(true);
        taTips.setWrapStyleWord(true);
        taTips.setBorder(BorderFactory.createTitledBorder("가이드/팁"));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnReroll = new JButton("미션 다시 뽑기");
        btnReroll.addActionListener(e -> { rollRandomEmojiAndMissions(); autoCheckFixedGoal(); });

        JButton btnReward = new JButton("보상 수령");
        btnReward.addActionListener(e -> {
            // 이벤트 분리: 보상은 OpeningEmoticon_test가 담당
            boolean ok = rewardHandler.tryReward(currentEmojiName,
                    cbFixedGoal.isSelected(), cbMission1.isSelected(), cbMission2.isSelected());
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "아직 완료되지 않은 미션이 있습니다.\n(고정 미션 + 2개 실천 미션 체크 필요)",
                        "보상 지급 불가", JOptionPane.WARNING_MESSAGE);
            }
        });

        south.add(btnReroll);
        south.add(btnReward);

        box.add(title, BorderLayout.NORTH);
        box.add(mids, BorderLayout.CENTER);
        box.add(new JScrollPane(taTips), BorderLayout.SOUTH);
        box.add(south, BorderLayout.PAGE_END);
        return box;
    }

    private JPanel buildGraphButtons() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new TitledBorder("그래프 보기"));

        JButton btnDaily = new JButton("일별 그래프");
        btnDaily.addActionListener(e -> showGraph("일별 배출량", aggregateDaily()));

        JButton btnWeekly = new JButton("주간 그래프");
        btnWeekly.addActionListener(e -> showGraph("주간 배출량", aggregateWeekly()));

        JButton btnMonthly = new JButton("월간 그래프");
        btnMonthly.addActionListener(e -> showGraph("월간 배출량", aggregateMonthly()));

        p.add(btnDaily); p.add(Box.createVerticalStrut(8));
        p.add(btnWeekly); p.add(Box.createVerticalStrut(8));
        p.add(btnMonthly);
        return p;
    }

    /** 대시보드 테이블을 읽어 오늘 상태 반영 */
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
            int tree = (int)(saved / 21.77); // 21.77kg ≒ 나무 1그루
            lblImpact.setText("🌱 절감 " + String.format("%.1f", saved) + "kg → 나무 " + tree + "그루 효과");
        } else {
            lblImpact.setText("🚨 목표 초과! 내일 더 절약해봐요");
        }
        autoCheckFixedGoal();
    }

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
        taTips.setText("- 위 두 가지 실천 항목 중 하나 이상 완료해 보세요.\n- 고정 미션은 오늘의 배출량이 권장 목표 이하일 때 자동 달성됩니다.");
    }

    private void autoCheckFixedGoal() {
        cbFixedGoal.setSelected(calcTodayEmissionFromDashboard() <= safeGoal());
    }

    private void showGraph(String title, Map<String, Double> data) {
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.add(new GraphRendering_test(data));
        f.pack();
        f.setLocationRelativeTo(this);
        f.setVisible(true);
    }

    // ===== 집계 유틸 =====
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

    /** 최근 30일 */
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

    /** 최근 12주 (월요일 시작) */
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
            LocalDate d = parseDate(String.valueOf(model.getValueAt(i, 0)));
            if (d == null) continue;
            LocalDate wk = d.with(wf.dayOfWeek(), 1);
            String key = wk.format(DATE_FMT) + "(주)";
            double v = toDouble(model.getValueAt(i, 5));
            map.put(key, map.getOrDefault(key, 0.0) + v);
        }
        return map;
    }

    /** 최근 12개월 */
    private Map<String, Double> aggregateMonthly() {
        DefaultTableModel model = dashboard.getTableModel();
        Map<String, Double> map = new TreeMap<>();

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusMonths(11).withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(1);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (LocalDate m = start; !m.isAfter(end); m = m.plusMonths(1)) {
            map.put(m.format(fmt) + "(월)", 0.0);
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            LocalDate d = parseDate(String.valueOf(model.getValueAt(i, 0)));
            if (d == null) continue;
            String key = d.withDayOfMonth(1).format(fmt) + "(월)";
            double v = toDouble(model.getValueAt(i, 5));
            map.put(key, map.getOrDefault(key, 0.0) + v);
        }
        return map;
    }

    private LocalDate parseDate(String s) {
        try { return LocalDate.parse(s, DATE_FMT); } catch (Exception e) { return null; }
    }
    private double toDouble(Object v) {
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0.0; }
    }
    private double safeGoal() {
        try {
            Double g = recommendedGoal.get();
            return (g != null && g > 0) ? g : 20.0;
        } catch (Exception e) { return 20.0; }
    }
}
