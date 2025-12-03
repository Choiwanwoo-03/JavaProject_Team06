package GUI;

public class Login_Gui {
  
private void showLogin() {
        JFrame loginFrame = new JFrame("로그인 / 회원가입");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(350,220);
        loginFrame.setLocationRelativeTo(null);

        // 아이디와 비밀번호를 입력하는 필드 생성
        JTextField idField = new JTextField(15);
        JPasswordField pwField = new JPasswordField(15);
        
        // 로그인과 회원가입 버튼 생성
        JButton loginButton = new JButton("로그인");
        JButton registerButton = new JButton("회원가입");

        // 메인 로그인 화면 패널
        JPanel Mainpanel = new JPanel(new GridLayout(3, 2, 10, 10));
        Mainpanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        Mainpanel.add(new JLabel("id (사용자 이름): "));
        Mainpanel.add(idField);
        Mainpanel.add(new JLabel("password (비밀번호): "));
        Mainpanel.add(pwField);
        Mainpanel.add(loginButton);
        Mainpanel.add(registerButton);

        // 버튼을 눌렀을 때 attempLogin, attempRegister 메소드에서 이벤트 처리하도록 (서버와 연계)
        loginButton.addActionListener(e -> attempLogin(idField.getText(), new String(pwField.getPassword()), login));
        registerButton.addActionListener(e -> attempRegister(idField.getText(), new String(pwField.getPassword()), login));

        loginFrame.add(Mainpanel);
        loginFrame.setVisible(true);
        loginFrame.setAlwaysOnTop(true); 
    }
  
   // 로그인 버튼을 눌렀을 때 이벤트 처리를 하는 메소드
private void attemptLogin(String username, String password, JFrame loginFrame) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(loginFrame, "ID와 비밀번호를 입력하세요.");
            return;
        }
        
        AuthData auth = new AuthData(username, password);
        // 서버에 로그인 요청 (userId를 Integer로 반환 받음)
        Integer userId = communicate(RequestType.LOGIN, auth); 

        if (userId != null && userId > 0) {
            currentUserId = userId; // 사용자 ID 설정
            loginFrame.dispose(); // 로그인 창 닫기
            showMainApp(); // 메인 앱 실행
        } else {
            JOptionPane.showMessageDialog(loginFrame, "로그인 실패: ID 또는 비밀번호를 확인하세요.", "로그인 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
  
  // 회원가입 버튼을 눌렀을 때 이벤트 처리를 하는 메소드
private void attemptRegister(String username, String password) {
        if (username.length() < 3 || password.length() < 6) {
            JOptionPane.showMessageDialog(null, "ID는 3자 이상, 비밀번호는 6자 이상이어야 합니다.");
            return;
        }
        
        AuthData auth = new AuthData(username, password);
        // 서버에 회원가입 요청
        Boolean success = communicate(RequestType.REGISTER, auth); 

        if (success != null && success) {
            JOptionPane.showMessageDialog(null, "회원가입 성공! 이제 로그인해주세요.");
        } else {
            JOptionPane.showMessageDialog(null, "회원가입 실패: 이미 존재하는 ID이거나 서버 오류입니다.", "회원가입 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}