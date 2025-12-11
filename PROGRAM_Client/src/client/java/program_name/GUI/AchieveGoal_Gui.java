package GUI;

import Goal.CompareResult;
import Goal.MakeSolution;
import Mission.MissionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AchieveGoal_Gui extends JPanel {

    // 데이터 공급자
    private final Supplier<Double> todaySupplier;
    private final Supplier<Double> goalSupplier;
    private final MissionManager missionManager;
    private final Runnable onMissionUpdate; // 미션 업데이트 시 서버 전송용 콜백

    // UI 컴포넌트
    private final JLabel lblGoal = new JLabel();
    private final JLabel lblToday = new JLabel();
    private final JLabel lblAchv = new JLabel();
    private final JLabel lblImpact = new JLabel();
    private final JTextArea taAdvice = new JTextArea(3, 20);
    private final JButton btnSetGoal = new JButton("목표 수정");
    private final CircularProgressBar progressBar;

    // 미션 UI 컴포넌트들
    private final MissionItemPanel mission1Panel;
    private final MissionItemPanel mission2Panel;
    private final MissionItemPanel mission3Panel;

    // 목표 변경 콜백
    private final Consumer<Double> onGoalChange;

    public AchieveGoal_Gui(Supplier<Double> todayEmissionSupplier,
                           Supplier<Double> goalSupplier,
                           MissionManager missionManager,
                           Consumer<Double> onGoalChange,
                           Runnable onMissionUpdate) {

        super(new BorderLayout(10, 10));
        this.todaySupplier = todayEmissionSupplier;
        this.goalSupplier = goalSupplier;
        this.missionManager = missionManager;
        this.onGoalChange = onGoalChange;
        this.onMissionUpdate = onMissionUpdate;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(800, 320)); // 패널 크기 키움

        // ==========================================
        // [메인 레이아웃] 좌: 그래프 / 우: 미션 리스트
        // ==========================================
        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContainer.setOpaque(false);

        // ------------------------------------------
        // 1. 왼쪽 패널 (원형 그래프 + 수치 정보)
        // ------------------------------------------
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY), "📊 오늘의 탄소 배출 현황"));
        leftPanel.setBackground(Color.WHITE);

        // 1-1. 그래프 영역
        progressBar = new CircularProgressBar();
        progressBar.setPreferredSize(new Dimension(160, 160));
        JPanel graphWrap = new JPanel(new GridBagLayout());
        graphWrap.setBackground(Color.WHITE);
        graphWrap.add(progressBar);
        leftPanel.add(graphWrap, BorderLayout.CENTER);

        // 1-2. 텍스트 정보 & 버튼
        JPanel infoPanel = new JPanel(new GridLayout(5, 1, 2, 2));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        Font f = new Font("SansSerif", Font.BOLD, 13);
        lblGoal.setFont(f); lblToday.setFont(f); lblAchv.setFont(f); lblImpact.setFont(f);

        infoPanel.add(lblGoal);
        infoPanel.add(lblToday);
        infoPanel.add(lblAchv);
        infoPanel.add(lblImpact);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        btnPanel.setBackground(Color.WHITE);
        btnSetGoal.setBackground(new Color(240, 240, 240));
        btnPanel.add(btnSetGoal);
        infoPanel.add(btnPanel);

        leftPanel.add(infoPanel, BorderLayout.SOUTH);
        mainContainer.add(leftPanel);

        // ------------------------------------------
        // 2. 오른쪽 패널 (미션 체크리스트)
        // ------------------------------------------
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY), "✅ 오늘의 환경 미션"));
        rightPanel.setBackground(Color.WHITE);

        JPanel missionListPanel = new JPanel(new GridLayout(3, 1, 5, 10));
        missionListPanel.setBackground(Color.WHITE);
        missionListPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 미션 1 (자동)
        mission1Panel = new MissionItemPanel(true); // 자동 모드
        // 미션 2, 3 (수동)
        mission2Panel = new MissionItemPanel(false);
        mission3Panel = new MissionItemPanel(false);

        missionListPanel.add(mission1Panel);
        missionListPanel.add(mission2Panel);
        missionListPanel.add(mission3Panel);

        rightPanel.add(missionListPanel, BorderLayout.CENTER);
        mainContainer.add(rightPanel);

        add(mainContainer, BorderLayout.CENTER);

        // ------------------------------------------
        // 3. 하단 (피드백 메시지)
        // ------------------------------------------
        taAdvice.setEditable(false);
        taAdvice.setLineWrap(true);
        taAdvice.setWrapStyleWord(true);
        taAdvice.setBackground(new Color(250, 250, 250));
        taAdvice.setBorder(new EmptyBorder(10, 10, 10, 10));
        taAdvice.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(taAdvice);
        scrollPane.setBorder(new TitledBorder("💡 오늘의 피드백"));
        scrollPane.setPreferredSize(new Dimension(-1, 80));

        add(scrollPane, BorderLayout.SOUTH);

        // ==========================================
        // [이벤트 리스너]
        // ==========================================

        // 목표 수정 버튼
        btnSetGoal.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "새로운 목표(kg)를 입력하세요:", goalSupplier.get());
            if (input != null) {
                try {
                    double val = Double.parseDouble(input);
                    if (val <= 0) throw new NumberFormatException();
                    if (onGoalChange != null) onGoalChange.accept(val);
                    refreshUI(); // 즉시 갱신
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "0보다 큰 숫자를 입력해주세요.");
                }
            }
        });

        // 미션 2 완료 버튼
        mission2Panel.btnCheck.addActionListener(e -> {
            boolean current = missionManager.getMission2Success() == 1;
            int nextState = current ? 0 : 1;
            missionManager.setMission2Success(nextState);
            // DB 저장 및 서버 전송
            missionManager.saveTodayMission();
            if (onMissionUpdate != null) onMissionUpdate.run();
            refreshUI();
        });

        // 미션 3 완료 버튼
        mission3Panel.btnCheck.addActionListener(e -> {
            boolean current = missionManager.getMission3Success() == 1;
            int nextState = current ? 0 : 1;
            missionManager.setMission3Success(nextState);
            // DB 저장 및 서버 전송
            missionManager.saveTodayMission();
            if (onMissionUpdate != null) onMissionUpdate.run();
            refreshUI();
        });

        // 초기 화면 그리기
        refreshUI();
    }

    // ==========================================
    // UI 갱신 (데이터 로드 및 반영)
    // ==========================================
    public void refreshUI() {
        if (todaySupplier == null || goalSupplier == null || missionManager == null) return;

        double today = todaySupplier.get();
        double goal = goalSupplier.get();

        // 1. 그래프 및 텍스트 정보 갱신
        CompareResult res = new CompareResult(today, goal);
        lblGoal.setText(" 목표: " + String.format("%.1f", res.goalEmission) + " kg");
        lblToday.setText(" 배출: " + String.format("%.3f", res.todayEmission) + " kg");

        if (res.success) {
            lblAchv.setText(" 달성률: " + String.format("%.1f%%", res.percent) + " (성공)");
            lblAchv.setForeground(new Color(34, 139, 34));
        } else {
            lblAchv.setText(" 달성률: " + String.format("%.1f%%", res.percent) + " (초과)");
            lblAchv.setForeground(Color.RED);
        }

        double saved = Math.max(0, res.goalEmission - res.todayEmission);
        int tree = (int)(saved / 21.77);
        lblImpact.setText(" 🌱 효과: 나무 " + tree + "그루 심음 (" + String.format("%.1f", saved) + "kg 절감)");

        MakeSolution ms = new MakeSolution();
        taAdvice.setText(ms.buildMessage(res));
        progressBar.updateProgress(today, goal);

        // 2. 미션 1 (자동 체크 로직)
        // 목표 배출량 이하이면 자동 성공 처리
        boolean isMission1Complete = res.success;
        missionManager.setMission1Name("오늘의 목표 달성하기 (" + goal + "kg 이하)");
        missionManager.setMission1Success(isMission1Complete ? 1 : 0);
        // (자동 변경 사항 저장)
        missionManager.saveTodayMission();

        // 3. 미션 패널 업데이트
        mission1Panel.updateView(missionManager.getMission1Name(), missionManager.getMission1Success() == 1);
        mission2Panel.updateView(missionManager.getMission2Name(), missionManager.getMission2Success() == 1);
        mission3Panel.updateView(missionManager.getMission3Name(), missionManager.getMission3Success() == 1);
    }

    // ==========================================
    // [내부 클래스] 미션 아이템 (카드 디자인)
    // ==========================================
    private static class MissionItemPanel extends JPanel {
        private final JLabel lblName = new JLabel();
        private final JButton btnCheck = new JButton();
        private final boolean isAuto;

        public MissionItemPanel(boolean isAuto) {
            this.isAuto = isAuto;
            setLayout(new BorderLayout(10, 0));
            setBackground(new Color(245, 245, 245));
            setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
            setPreferredSize(new Dimension(0, 40));

            lblName.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblName.setBorder(new EmptyBorder(0, 10, 0, 0));
            add(lblName, BorderLayout.CENTER);

            btnCheck.setPreferredSize(new Dimension(80, 30));
            btnCheck.setFocusPainted(false);
            btnCheck.setFont(new Font("SansSerif", Font.BOLD, 11));

            // 자동 미션은 버튼 클릭 불가 (그래프 연동)
            if (isAuto) {
                btnCheck.setEnabled(false);
            }

            JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            btnWrap.setOpaque(false);
            btnWrap.add(btnCheck);
            add(btnWrap, BorderLayout.EAST);
        }

        public void updateView(String name, boolean isSuccess) {
            lblName.setText(name.isEmpty() ? "(미션 없음)" : name);
            if (isSuccess) {
                btnCheck.setText("성공!");
                btnCheck.setBackground(new Color(60, 179, 113)); // 초록색
                btnCheck.setForeground(Color.WHITE);
                setBackground(new Color(235, 255, 235)); // 연한 초록 배경
                btnCheck.setEnabled(false);
            } else {
                btnCheck.setText(isAuto ? "진행 중" : "완료 하기");
                btnCheck.setBackground(new Color(220, 220, 220));
                btnCheck.setForeground(Color.DARK_GRAY);
                setBackground(new Color(245, 245, 245)); // 회색 배경
            }
            if (!isAuto) {
                btnCheck.setEnabled(true);
            }
        }
    }

    // =========================================================
    // [내부 클래스] 원형 그래프 (이전 코드 유지)
    // =========================================================
    public static class CircularProgressBar extends JPanel {
        private double current = 0;
        private double max = 100;

        public CircularProgressBar() { setOpaque(false); }

        public void updateProgress(double current, double max) {
            this.current = current;
            this.max = max;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(); int h = getHeight();
            int size = Math.min(w, h) - 10;
            int x = (w - size) / 2; int y = (h - size) / 2;

            // 배경
            g2.setColor(new Color(230, 230, 230));
            g2.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(x, y, size, size, 0, 360);

            // 진행바
            double ratio = (max > 0) ? (current / max) : 0;
            if (ratio > 1.0) ratio = 1.0;
            int angle = (int) (ratio * 360);

            if (current > max) g2.setColor(new Color(255, 100, 100));
            else g2.setColor(new Color(100, 180, 255));

            g2.drawArc(x, y, size, size, 90, -angle);

            // 텍스트
            double percent = (max > 0 ? (current / max * 100) : 0);
            String text = String.format("%.0f%%", percent);
            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2.setColor(Color.DARK_GRAY);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, w/2 - fm.stringWidth(text)/2, h/2 + fm.getAscent()/3);
        }
    }
}