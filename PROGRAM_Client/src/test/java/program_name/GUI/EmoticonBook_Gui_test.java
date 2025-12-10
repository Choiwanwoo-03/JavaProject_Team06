package GUI;

import Emoticon.EmoticonManager;
import Emoticon.EmoticonManager.EmoticonInfo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// 이모티콘 도감 탭 GUI
public class EmoticonBook_Gui extends JPanel { // 이모티콘 도감을 표시하는 패널

    private final EmoticonManager manager;
    private final JPanel gridPanel; // 이모티콘 아이템들을 담을 그리드 레이아웃 패널

    public EmoticonBook_Gui(EmoticonManager manager) { // 생성자
        this.manager = manager;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("이모티콘 도감", SwingConstants.CENTER); // 제목 레이블
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        // 이모티콘 아이템 표시를 위한 4열 그리드 패널
        gridPanel = new JPanel(new GridLayout(0, 4, 12, 12));
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(gridPanel), BorderLayout.CENTER); // 스크롤 가능하도록 추가

        refreshGrid(); // 초기 화면 구성
    }

    // 현재 EmoticonManager 상태
    public void refreshGrid() {
        gridPanel.removeAll(); // 기존 컴포넌트 모두 제거

        for (EmoticonInfo info : manager.getAllEmoticons()) {
            boolean unlocked = manager.isUnlocked(info.name); // 해금 여부 확인

            JPanel itemPanel = new JPanel(new BorderLayout()); // 개별 이모티콘 패널 생성
            itemPanel.setBorder(new CompoundBorder(
                    new LineBorder(Color.LIGHT_GRAY, 1, true),
                    new EmptyBorder(8, 8, 8, 8)
            ));
            itemPanel.setBackground(unlocked ? Color.WHITE : Color.decode("#FAFAFA")); // 배경색 변경

            JLabel emojiLabel = new JLabel("", SwingConstants.CENTER);
            emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42)); // 이모지 폰트 설정

            JLabel nameLabel = new JLabel("", SwingConstants.CENTER);
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));

            if (unlocked) {
                // 공개된 이모티콘: 실제 이모지/이름 표시
                emojiLabel.setText(info.emoji);
                nameLabel.setText(info.name);

                itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                itemPanel.addMouseListener(new MouseAdapter() { // 마우스 클릭 리스너 등록
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // 클릭 시 상세 정보 팝업
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
                // 미공개 이모티콘: ❓와 ??? 표시
                emojiLabel.setText("❓");
                nameLabel.setText("???");
            }

            itemPanel.add(emojiLabel, BorderLayout.CENTER);
            itemPanel.add(nameLabel, BorderLayout.SOUTH);

            gridPanel.add(itemPanel); // 그리드 패널에 아이템 추가
        }

        gridPanel.revalidate(); // 레이아웃 재계산
        gridPanel.repaint(); // 화면 다시 그리기
    }
}