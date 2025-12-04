package Emoticon;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** 미션 완료 보상 팝업 (자체 카탈로그 내장) */
public class OpeningEmoticon_test extends JDialog {

    /** 이모티콘 카탈로그 (이름 → (이모지, 설명)) */
    private static final Map<String, Emoji> CATALOG = new LinkedHashMap<>();
    static {
        put("미니 나무",      "🌳", "나무 심기 운동에 기여했어요!");
        put("재활용 마스터",  "♻️", "분리수거를 완벽하게 해냈어요!");
        put("물방울 친구",    "💧", "물을 절약했어요!");
        put("절약 전구",      "💡", "전기를 아꼈어요!");
        put("친환경 라이더",  "🚲", "탄소 배출을 줄였어요!");
        put("장바구니 마스터","🧺", "일회용 봉투 없이 쇼핑을 완료했어요!");
        put("잔반 제로",      "🍲", "음식물 쓰레기 없이 식사를 마쳤어요!");
    }
    private static void put(String name, String emoji, String desc) { CATALOG.put(name, new Emoji(emoji, name, desc)); }
    private record Emoji(String emoji, String name, String desc) {}

    /** 편의 호출: 부모 컴포넌트와 이모티콘 이름만 넘기면 보상 팝업 표시 */
    public static void showReward(Component parent, String emojiName) {
        new OpeningEmoticon_test(parent, emojiName).setVisible(true);
    }

    private OpeningEmoticon_test(Component parent, String emojiName) {
        super(SwingUtilities.getWindowAncestor(parent), "보상 지급", ModalityType.APPLICATION_MODAL);
        Emoji e = CATALOG.getOrDefault(emojiName, new Emoji("🎉", emojiName, "축하합니다! 보상을 획득했어요."));
        buildUI(e);
    }

    private void buildUI(Emoji e) {
        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        JLabel lblEmoji = new JLabel(e.emoji());
        lblEmoji.setHorizontalAlignment(SwingConstants.CENTER);
        lblEmoji.setFont(lblEmoji.getFont().deriveFont(Font.PLAIN, 64f));

        JLabel lblName = new JLabel(e.name(), SwingConstants.CENTER);
        lblName.setFont(lblName.getFont().deriveFont(Font.BOLD, 20f));

        JTextArea ta = new JTextArea(e.desc());
        ta.setEditable(false);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setOpaque(false);
        ta.setFont(ta.getFont().deriveFont(14f));

        JButton ok = new JButton("확인");
        ok.addActionListener(ev -> dispose());

        JPanel center = new JPanel(new BorderLayout(8,8));
        center.add(lblName, BorderLayout.NORTH);
        center.add(ta, BorderLayout.CENTER);

        root.add(lblEmoji, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(ok, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(360, 300);
        setLocationRelativeTo(getOwner());
    }
}
