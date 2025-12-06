import Socket.ClientSocket;
import GUI.Login_Gui;

/**
 * 클라이언트 프로그램의 시작 지점 (main 클래스)
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("[CLIENT] 클라이언트 프로그램을 시작합니다.");

        // 서버와 통신할 소켓 객체 생성
        ClientSocket client = new ClientSocket();

        // 서버에 연결 시도
        boolean connected = client.connectToServer("127.0.0.1", 5000);

        // 서버 연결 실패 시 프로그램 종료
        if (!connected) {
            System.out.println("[CLIENT] 서버 연결 실패. 프로그램을 종료합니다.");
            return;
        }

        // 로그인 화면(GUI) 실행
        Login_Gui login = new Login_Gui(client);
        login.showLogin();   // 로그인 창 표시

        // 프로그램 종료 시 자동으로 소켓을 닫도록 설정
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.close();
            System.out.println("[CLIENT] 소켓 종료 완료");
        }));
    }
}
