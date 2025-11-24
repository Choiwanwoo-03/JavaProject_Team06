package Socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSocket {

    private ServerSocket server;  // 포트 바인딩용
    private final int PORT = 5000;  // 원하는 포트 번호
    public static ArrayList<ConnectedClient> clients = new ArrayList<>();

    // 서버 시작 메소드
    public void startServer() {
        try {
            server = new ServerSocket(PORT);
            System.out.println("[SERVER] 포트 " + PORT + " 에서 서버가 시작되었습니다!");

            while (true) {
                System.out.println("[SERVER] 클라이언트 접속 대기중...");
                Socket clientSock = server.accept();  

                System.out.println("[SERVER] 클라이언트 연결됨: " + clientSock.getInetAddress());

                // 클라이언트 객체 생성
                ConnectedClient client = new ConnectedClient(clientSock, this);
                clients.add(client);

                // 스레드로 실행
                client.start();
            }

        } catch (IOException e) {
            System.out.println("[SERVER] 서버 오류 발생: " + e.getMessage());
        }
    }

    // 클라이언트 리스트에서 제거 (종료 시 호출)
    public void removeClient(ConnectedClient client) {
        clients.remove(client);
    }
}
