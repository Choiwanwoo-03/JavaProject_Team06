package GUI;

import Emoticon.EmoticonManager;
import Emoticon.EmoticonManager.EmoticonInfo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 이모티콘 도감 탭 GUI
 * - 공개된 이모티콘: 실제 이모지 + 이름 표시
 * - 미공개 이모티콘: ❓ + "???" 표시
 * - 공개된 이모티콘 클릭 시 이름 + 설명 팝업
 */
public class EmoticonBook_Gui extends JPanel {

    private final EmoticonManager emoticonManager;
    private final JPanel gridPanel;

    public EmoticonBook_Gui(EmoticonManager emoticonManager) {
        this.emoticonManager = emoticonManager;

        setLayout(new BorderLayout());
        setBorder(new TitledBorder("친환경 이모티콘 도감"));

        gridPanel = new JPanel(new GridLayout(0, 5, 10, 10)); // 5열 그리드
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        refreshGrid();

        JScrollPane scroll = new JScrollPane(gridPanel);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * 도감 그리드 전체 갱신
     * EmoticonManager의 상태(해금/미해금)에 따라 UI를 다시 그림
     */
    public void refreshGrid() {
        gridPanel.removeAll();

        for (EmoticonInfo info : emoticonManager.getAllEmoticons()) {
            boolean unlocked = emoticonManager.isUnlocked(info.name);

            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBorder(BorderFactory.createLineBorder(
                    unlocked ? Color.GREEN.darker() : Color.LIGHT_GRAY,
                    2
            ));

            JLabel emojiLabel = new JLabel("", SwingConstants.CENTER);
            emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

            JLabel nameLabel = new JLabel("", SwingConstants.CENTER);
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));

            if (unlocked) {
                // 공개된 이모티콘
                emojiLabel.setText(info.emoji);
                nameLabel.setText(info.name);

                itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JOptionPane.showMessageDialog(
                            EmoticonBook_Gui.this,
                            "이모티콘: " + info.emoji + " (" + info.name + ")\n" +
                            "설명: " + info.description,
                            "도감 상세 정보",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                });

            } else {
                // 아직 공개되지 않은 이모티콘
                emojiLabel.setText("❓");
                nameLabel.setText("???");
                itemPanel.setBackground(Color.decode("#FAFAFA"));
            }

            itemPanel.add(emojiLabel, BorderLayout.CENTER);
            itemPanel.add(nameLabel, BorderLayout.SOUTH);

            gridPanel.add(itemPanel);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }
}
