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

        // // 버튼을 눌렀을 때 attempLogin, attempRegister 메소드에서 이벤트 처리하도록 (서버와 연계)
        // loginButton.addActionListener(e -> attempLogin(idField.getText(), new String(pwField.getPassword()), login));
        // registerButton.addActionListener(e -> attempRegister(idField.getText(), new String(pwField.getPassword()), login));

        loginFrame.add(Mainpanel);
        loginFrame.setVisible(true);
        loginFrame.setAlwaysOnTop(true);  
    }
}
