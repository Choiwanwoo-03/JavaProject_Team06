package Visualization;



import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/** 일/주/월 그래프 출력(심플 막대 그래프 + 개선된 시각 요소) */
public class GraphRendering extends JPanel {
    private final Map<String, Double> emissionData; // X축: 기간 문자열 / Y축: CO2 배출량 (kg CO2e)

    public GraphRendering(Map<String, Double> emissionData) {
        this.emissionData = emissionData;
        setPreferredSize(new Dimension(900, 350));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (emissionData == null || emissionData.isEmpty()) {
            g.drawString("그래프 데이터가 없습니다.", 20, 20);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), pad = 50;
        List<String> keys = new ArrayList<>(emissionData.keySet());
        double max = emissionData.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);

        // 막대 폭 계산
        int n = keys.size();
        int barSpacing = 10;
        int barW = Math.max(10, (w - pad * 2 - n * barSpacing) / n);

        // 축 스타일
        g2.setFont(new Font("Dialog", Font.PLAIN, 12));
        g2.setColor(Color.GRAY);
        g2.drawLine(pad, h - pad, w - pad, h - pad); // X축
        g2.drawLine(pad, pad, pad, h - pad); // Y축

        g2.drawString(String.format("최대: %.1f kg", max), pad, pad - 10);

        // 값 표시
        int i = 0;
        for (String k : keys) {
            double v = emissionData.get(k);
            int bh = (int) ((h - pad * 2) * (v / max));
            int x = pad + i * (barW + barSpacing);
            int y = h - pad - bh;

            // 그래디언트 느낌 색상
            g2.setColor(new Color(100, 180, 255));
            g2.fillRoundRect(x, y, barW, bh, 6, 6);

            // 테두리
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(x, y, barW, bh, 6, 6);

            // 값 라벨
            g2.drawString(String.format("%.1f", v), x + 2, y - 6);

            // X축 라벨
            g2.setColor(new Color(60, 60, 60));
            drawRotatedText(g2, k, x + barW / 2, h - pad + 15);

            i++;
        }
    }

    /** X축 라벨이 길 경우 기울여서 표시 */
    private void drawRotatedText(Graphics2D g2, String text, int x, int y) {
        g2.rotate(Math.toRadians(-25), x, y);
        g2.drawString(text, x, y);
        g2.rotate(Math.toRadians(25), x, y);
    }
}
