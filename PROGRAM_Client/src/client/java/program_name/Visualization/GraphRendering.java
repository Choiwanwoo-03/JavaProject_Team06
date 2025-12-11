package Visualization;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;


/**
 * 막대 그래프 전용 렌더러
 * - Map<String, Double> 형태의 데이터 입력
 * - Key: 구간(날짜, 주차, 월)
 * - Value: CO₂e 값
 */
public class GraphRendering extends JPanel {

    private final Map<String, Double> emissionData;

    public GraphRendering(Map<String, Double> emissionData) {
        this.emissionData = (emissionData != null) ? emissionData : Collections.emptyMap();
        setPreferredSize(new Dimension(900, 350));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (emissionData.isEmpty()) {
            g.drawString("그래프 데이터가 없습니다.", 20, 20);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        // 패딩
        int padding = 50;
        int labelPadding = 40;

        // 실제 그래프 영역
        int graphWidth = width - padding * 2;
        int graphHeight = height - padding * 2 - labelPadding;

        // Key(구간) 정렬
        List<String> keys = new ArrayList<>(emissionData.keySet());
        Collections.sort(keys);

        // 최대값 찾기
        double dataMax = 0;
        for (String k : keys) {
            dataMax = Math.max(dataMax, emissionData.get(k));
        }

        // 1. 최소 눈금 설정 (데이터가 작아도 그래프가 너무 확대되지 않게 함)
        // 예: 최소 5kg까지는 그래프 축을 고정함. 데이터가 5kg를 넘으면 데이터에 맞춤.
        double displayMax = Math.max(dataMax, 5.0); 

        // 2. 상단 여유 공간 (가장 높은 막대가 천장에 닿지 않게 10% 여유)
        double maxValue = displayMax * 1.1;
        

        int barCount = keys.size();
        int barWidth = Math.max(20, graphWidth / barCount);

        // 축 그리기
        g2.setColor(Color.GRAY);
        g2.drawLine(padding, height - padding, width - padding, height - padding); // X축
        g2.drawLine(padding, padding, padding, height - padding); // Y축

        // Y축 최대값 표시
        g2.drawString(String.format("최대 %.1f kg", maxValue), padding, padding - 10);

        // 막대 그래프 그리기
        int x = padding + 10;

        for (String key : keys) {
            double value = emissionData.get(key);
            int barHeight = (int) Math.round((value / maxValue) * graphHeight);

            int y = (height - padding) - barHeight;

            // 막대 색상
            g2.setColor(new Color(120, 170, 255));
            g2.fillRect(x, y, barWidth - 20, barHeight);

            // 막대 테두리
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(x, y, barWidth - 20, barHeight);

            // 값 라벨
            g2.drawString(String.format("%.1f", value), x + 5, y - 5);

            // Key(날짜, 주, 월) 라벨
            g2.setColor(Color.BLACK);
            String shortKey = key.length() > 8 ? key.substring(key.length() - 5) : key;
            g2.drawString(shortKey, x, height - padding + 15);

            x += barWidth;
        }
    }
}
