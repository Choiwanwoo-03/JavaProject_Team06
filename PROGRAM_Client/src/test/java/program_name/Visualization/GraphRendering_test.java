package Visualization;


import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GraphRendering_test extends JPanel {
    private final Map<String, Double> emissionData;

    public GraphRendering_test(Map<String, Double> emissionData) {
        this.emissionData = (emissionData != null) ? emissionData : Collections.emptyMap();
        setPreferredSize(new Dimension(900, 320));
        setBackground(Color.WHITE);
        setOpaque(true);
    }
//
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (emissionData.isEmpty()) {
            g.drawString("그래프 데이터가 없습니다.", 20, 20);
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth(), h = getHeight(), pad = 40;

        java.util.List<String> keys = new ArrayList<>(emissionData.keySet());
        double max = emissionData.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);

        int n = keys.size();
        int barW = Math.max(8, (w - pad*2) / Math.max(1, n));
        int i = 0;
        for (String k : keys) {
            double v = emissionData.getOrDefault(k, 0.0);
            int bh = (int) ((h - pad*2) * (v / max));
            int x = pad + i * barW;
            int y = h - pad - bh;

            g2.setColor(new Color(120, 170, 255));
            g2.fillRect(x, y, barW - 4, bh);

            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.format("%.1f", v), x + 4, y - 4);

            i++;
        }

        g2.setColor(Color.GRAY);
        g2.drawRect(pad, pad, w - pad*2, h - pad*2);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(String.format("최대: %.1f kg", max), pad, pad - 10);
    }
}
