package Socket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSocket { // 메인 서버 소켓 관리 클래스

    private java.net.ServerSocket serverSocket; // 실제 java.net ServerSocket
    private final int PORT = 1234;
    public static ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>(); // 연결된 클라이언트 리스트

    public void startServer() { // 서버 구동 시작
        try {
            serverSocket = new java.net.ServerSocket(PORT); 
            System.out.println("[SERVER] 포트 " + PORT + " 에서 서버가 시작되었습니다!");

            while (true) { // 클라이언트 접속 무한 대기 루프
                System.out.println("[SERVER] 클라이언트 접속 대기중...");
                Socket clientSock = serverSocket.accept(); // 클라이언트 연결 수락

                System.out.println("[SERVER] 클라이언트 연결됨: " + clientSock.getInetAddress());

                // 연결된 클라이언트를 처리할 전용 스레드 생성
                ConnectedClient client = new ConnectedClient(clientSock, this);
                clients.add(client); // 리스트에 추가

                client.start(); // 클라이언트 스레드 시작
            }

        } catch (IOException e) {
            System.out.println("[SERVER] 서버 오류 발생: " + e.getMessage());
        }
    }

    // 클라이언트 연결 종료 시 리스트에서 제거
    public void removeClient(ConnectedClient client) {
        clients.remove(client);
    }
}