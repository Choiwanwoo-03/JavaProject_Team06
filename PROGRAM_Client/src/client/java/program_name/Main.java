import Socket.ClientSocket;
import GUI.Login_Gui;

public class Main {

    public static void main(String[] args) {

        System.out.println("[CLIENT] 클라이언트 프로그램을 시작합니다.");

        // 서버 연결
        ClientSocket client = new ClientSocket();
        boolean connected = client.connectToServer("127.0.0.1", 1234);

        if (!connected) {
            System.out.println("[CLIENT] 서버 연결 실패. 프로그램을 종료합니다.");
            return;
        }

        // 로그인 GUI 실행
        Login_Gui login = new Login_Gui(client);
        login.showLogin();  // 로그인 화면 표시

        // 프로그램 종료 시 소켓 닫기
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.close();
            System.out.println("[CLIENT] 소켓 종료 완료");
        }));
    }
}