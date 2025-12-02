package GUI;

import Goal.CompareResult;
import Goal.MakeSolution;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * 목표 달성 트래커 UI 패널.
 * - todayEmissionSupplier: 오늘 배출량(kg) 공급자(테이블 합산 등)
 * - goalSupplier: 목표값(kg) 공급자(고정/파일로드)
 * - 버튼 클릭 시 비교/피드백 갱신
 */
public class AchieveGoal_Gui extends JPanel {
    private final JLabel lblGoal = new JLabel();
    private final JLabel lblToday = new JLabel();
    private final JLabel lblAchv = new JLabel();
    private final JLabel lblImpact = new JLabel();
    private final JTextArea taAdvice = new JTextArea(6, 24);

    public AchieveGoal_Gui(Supplier<Double> todayEmissionSupplier, Supplier<Double> goalSupplier) {
        super(new BorderLayout(10,10));
        setBorder(new TitledBorder("일일 탄소 목표"));

        // 상단 정보 라벨
        JPanel north = new JPanel(new GridLayout(4,1,5,5));
        Font f = getFont().deriveFont(Font.BOLD, 14f);
        lblGoal.setFont(f); lblToday.setFont(f); lblAchv.setFont(f); lblImpact.setFont(f);
        north.add(lblGoal); north.add(lblToday); north.add(lblAchv); north.add(lblImpact);

        // 갱신 버튼: 비교 결과/피드백 업데이트
        JButton btnRefresh = new JButton("새로고침");
        btnRefresh.addActionListener(e -> {
            double today = todayEmissionSupplier.get();
            double goal  = goalSupplier.get();
            var res = new CompareResult().compare(today, goal);

            lblGoal.setText("하루 목표(kg): " + String.format("%.1f", goal));
            lblToday.setText("오늘 배출량(kg): " + String.format("%.3f", today));
            lblAchv.setText("절감 달성률: " + String.format("%.1f%%", res.achievement));

            // 임팩트 메시지(절감량 → 나무 효과 환산)
            double saved = res.saved;
            int tree = (int)(saved / 21.77);
            lblImpact.setText(res.withinGoal ? ("🌱 절감 " + String.format("%.1f", saved) + "kg → 나무 " + tree + "그루 효과")
                                             : "🚨 목표 초과! 내일 더 절약해봐요");

            // 해결책/칭찬 문구
            List<String> adv = res.withinGoal
                    ? new MakeSolution().praiseForWithinGoal(saved)
                    : new MakeSolution().suggestForOverGoal(today - goal);
            taAdvice.setText("• " + String.join("\n• ", adv));
        });

        // 피드백 영역
        taAdvice.setEditable(false);
        taAdvice.setLineWrap(true);
        taAdvice.setWrapStyleWord(true);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(taAdvice), BorderLayout.CENTER);
        add(btnRefresh, BorderLayout.SOUTH);
    }
}
