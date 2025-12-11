package GUI;

import Socket.ClientSocket;
import CommunicateServer.GetInformation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Login_Gui {

    private final ClientSocket client;
    private final GetInformation sender;

    public Login_Gui(ClientSocket client) {
        this.client = client;
        this.sender = client.getGetInformation();
    }

    // ---------------------------
    // 로그인 UI 표시
    // ---------------------------
    public void showLogin() {

        JFrame loginFrame = new JFrame("로그인 / 회원가입");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(350, 220);
        loginFrame.setLocationRelativeTo(null);

        JTextField idField = new JTextField(15);
        JPasswordField pwField = new JPasswordField(15);

        JButton loginButton = new JButton("로그인");
        JButton registerButton = new JButton("회원가입");

        JPanel panel = new JPanel(new GridLayout(3, 2, 10,10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("ID: "));
        panel.add(idField);
        panel.add(new JLabel("Password: "));
        panel.add(pwField);
        panel.add(loginButton);
        panel.add(registerButton);

        loginFrame.add(panel);
        loginFrame.setVisible(true);

        // ---------------------------
        // 서버의 LOGIN_OK / LOGIN_FAIL 콜백 처리
        // ---------------------------
        client.setLoginListener(new ClientSocket.LoginListener() {

            @Override
            public void onLoginSuccess(String nickname) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(loginFrame,
                            nickname + "님 환영합니다!", "로그인 성공",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    loginFrame.dispose();  // 로그인 창 닫기

                    // 메인 탭 화면으로 이동
                    TabPanel_Gui mainWindow = new TabPanel_Gui(nickname, client);
                    mainWindow.setVisible(true);
                });
            }

            @Override
            public void onLoginFail() {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        loginFrame,
                        "로그인 실패: ID 또는 비밀번호가 틀립니다.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE
                ));
            }
        });

        // ---------------------------
        // 로그인 버튼 이벤트
        // ---------------------------
        loginButton.addActionListener(e -> {
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword()).trim();

            if (id.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame,
                        "ID와 비밀번호를 입력하세요.",
                        "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            sender.sendLogin(id, pw); // 서버로 로그인 요청 전송
        });

        // ---------------------------
        // 회원가입 버튼 이벤트
        // ---------------------------
        registerButton.addActionListener(e -> {
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword()).trim();

            if (id.length() < 3 || pw.length() < 6) {
                JOptionPane.showMessageDialog(loginFrame,
                        "ID는 3글자 이상, PW는 6글자 이상이어야 합니다.",
                        "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 닉네임은 우선 ID와 동일하게 사용 (원하면 나중에 별도 입력 창 추가)
            sender.sendRegister(id, pw, id);
            JOptionPane.showMessageDialog(loginFrame,
                    "회원가입 요청을 전송했습니다.\n서버에서 처리 후 다시 로그인해주세요.");
        });
    }
}
