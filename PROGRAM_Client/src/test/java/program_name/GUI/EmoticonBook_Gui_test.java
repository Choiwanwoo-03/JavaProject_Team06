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

    private final EmoticonManager manager;
    private final JPanel gridPanel;

    public EmoticonBook_Gui(EmoticonManager manager) {
        this.manager = manager;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("이모티콘 도감", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(0, 4, 12, 12));
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        refreshGrid();
    }

    /**
     * 현재 EmoticonManager 상태(해금 여부)에 따라 도감 UI를 다시 그림
     * - TabPanel_Gui에서 SYNC_ALL 이후 또는 이모티콘 해금 후 호출
     */
    public void refreshGrid() {
        gridPanel.removeAll();

        for (EmoticonInfo info : manager.getAllEmoticons()) {
            boolean unlocked = manager.isUnlocked(info.name);

            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBorder(new CompoundBorder(
                    new LineBorder(Color.LIGHT_GRAY, 1, true),
                    new EmptyBorder(8, 8, 8, 8)
            ));
            itemPanel.setBackground(unlocked ? Color.WHITE : Color.decode("#FAFAFA"));

            JLabel emojiLabel = new JLabel("", SwingConstants.CENTER);
            emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));

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
            }

            itemPanel.add(emojiLabel, BorderLayout.CENTER);
            itemPanel.add(nameLabel, BorderLayout.SOUTH);

            gridPanel.add(itemPanel);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }
}
