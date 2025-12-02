package Visualization;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 간단 막대 그래프 패널.
 * - 입력: 기간키 -> CO2e(kg)
 * - 사용: 일/주/월 집계 결과를 넘겨서 즉시 그리기
 */
public class GraphRendering extends JPanel {
    private final Map<String, Double> emissionData;

    public GraphRendering(Map<String, Double> emissionData) {
        this.emissionData = (emissionData != null) ? emissionData : Collections.<String,Double>emptyMap();
        setPreferredSize(new Dimension(900, 320));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (emissionData.isEmpty()) {
            g.drawString("그래프 데이터가 없습니다.", 20, 20);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        int pad = 40;

        // 키 정렬
        List<String> keys = new ArrayList<String>(emissionData.keySet());
        Collections.sort(keys);

        // 최대값 계산 (스트림 미사용)
        double max = 0.0;
        for (String k : keys) {
            Double v = emissionData.get(k);
            if (v != null && v > max) max = v;
        }
        if (max <= 0.0) max = 1.0;

        int n = keys.size();
        int barW = Math.max(8, (w - pad * 2) / Math.max(1, n));
        int i = 0;

        for (String k : keys) {
            double v = 0.0;
            Double vv = emissionData.get(k);
            if (vv != null) v = vv;

            int bh = (int) Math.round((h - pad * 2) * (v / max));
            int x  = pad + i * barW;
            int y  = h - pad - bh;

            // 막대
            g2.setColor(new Color(255, 120, 120));
            g2.fillRect(x, y, barW - 4, bh);

            // 값 라벨
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.format("%.1f", v), x + 4, y - 4);

            i++;
        }

        // 테두리/최댓값 표시
        g2.setColor(Color.GRAY);
        g2.drawRect(pad, pad, w - pad * 2, h - pad * 2);

        g2.setColor(Color.DARK_GRAY);
        g2.drawString(String.format("최대: %.1f kg", max), pad, pad - 10);
    }
}

