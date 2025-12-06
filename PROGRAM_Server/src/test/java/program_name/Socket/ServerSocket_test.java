package Socket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 서버 메인 소켓 클래스
 * - 클라이언트 접속을 계속 대기
 * - 접속한 클라이언트마다 ConnectedClient 스레드 생성
 */
public class ServerSocket {

    // 실제 서버 소켓 객체 (클래스명 충돌 방지)
    private java.net.ServerSocket serverSocket;

    // 서버 포트 번호
    private final int PORT = 5000;

    // 현재 접속 중인 모든 클라이언트 목록
    public static ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>();

    /**
     * 서버 시작 메소드
     */
    public void startServer() {
        try {
            // 서버 소켓 생성
            serverSocket = new java.net.ServerSocket(PORT);
            System.out.println("[SERVER] 포트 " + PORT + " 에서 서버가 시작되었습니다!");

            while (true) {
                // 클라이언트 접속 대기
                System.out.println("[SERVER] 클라이언트 접속 대기중...");
                Socket clientSock = serverSocket.accept();

                System.out.println("[SERVER] 클라이언트 연결됨: " + clientSock.getInetAddress());

                // 클라이언트 전용 통신 스레드 생성
                ConnectedClient client = new ConnectedClient(clientSock, this);
                clients.add(client);

                // 통신 스레드 실행
                client.start();
            }

        } catch (IOException e) {
            System.out.println("[SERVER] 서버 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 연결 종료된 클라이언트를 목록에서 제거
     */
    public void removeClient(ConnectedClient client) {
        clients.remove(client);
    }
}
