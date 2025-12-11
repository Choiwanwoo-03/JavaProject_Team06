package Emoticon;

import javax.swing.*;
import java.awt.*;

public class OpeningEmoticon {

    public static class EmoticonInfo {
        public final String name;
        public final String emoji;
        public final String description;

        public EmoticonInfo(String name, String emoji, String description) {
            this.name = name;
            this.emoji = emoji;
            this.description = description;
        }
    }

    // ============================================
    // ★★★ AchieveGoal_Gui에서 호출하는 showReward 추가
    // ============================================
    public static void showReward(Component parent, String emoticonName) {
        JOptionPane.showMessageDialog(
                parent,
                "새로운 이모티콘이 해금되었습니다!\n\n" + emoticonName,
                "🎉 이모티콘 보상",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
