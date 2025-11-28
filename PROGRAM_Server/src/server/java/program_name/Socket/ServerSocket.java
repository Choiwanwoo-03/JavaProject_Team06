package Socket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSocket {

    private java.net.ServerSocket serverSocket;  // ← 이름 충돌 해결
    private final int PORT = 5000;
    public static ArrayList<ConnectedClient> clients = new ArrayList<>();

    public void startServer() {
        try {
            serverSocket = new java.net.ServerSocket(PORT); // ← 충돌 해결
            System.out.println("[SERVER] 포트 " + PORT + " 에서 서버가 시작되었습니다!");

            while (true) {
                System.out.println("[SERVER] 클라이언트 접속 대기중...");
                Socket clientSock = serverSocket.accept();

                System.out.println("[SERVER] 클라이언트 연결됨: " + clientSock.getInetAddress());

                ConnectedClient client = new ConnectedClient(clientSock, this);
                clients.add(client);

                client.start();
            }

        } catch (IOException e) {
            System.out.println("[SERVER] 서버 오류 발생: " + e.getMessage());
        }
    }

    public void removeClient(ConnectedClient client) {
        clients.remove(client);
    }
}
