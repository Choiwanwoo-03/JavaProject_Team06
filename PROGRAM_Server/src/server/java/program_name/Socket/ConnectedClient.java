package Socket;

import CommunicateClient.GetInformation;
import CommunicateClient.GiveInformation;
import CommunicateClient.LoginManager_test;

import java.io.*;
import java.net.Socket;

public class ConnectedClient extends Thread {

    private Socket socket;
    private ServerSocket server; // ← 패키지 경로 명시

    private DataInputStream in;
    private DataOutputStream out;

    private String nickname = null;

    private final GetInformation getter = new GetInformation();
    private final GiveInformation giver = new GiveInformation();

    public ConnectedClient(Socket socket, ServerSocket server) {
        this.socket = socket;
        this.server = server;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("[SERVER][ConnectedClient] 스트림 생성 오류");
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String cmd = in.readUTF();

                if (cmd.equals("LOGIN")) {
                    handleLogin();
                    continue;
                }

                getter.handle(in);
            }
        } catch (Exception e) {
            System.out.println("[SERVER][ConnectedClient] 클라이언트 종료");
        } finally {
            server.removeClient(this);
            try { socket.close(); } catch (Exception ignore) {}
        }
    }

    private void handleLogin() throws IOException {
        String userId = in.readUTF();
        String userPwd = in.readUTF();

        LoginManager_test loginManager = new LoginManager_test();
        this.nickname = loginManager.verify(userId, userPwd);

        if (nickname == null) {
            out.writeUTF("LOGIN_FAIL");
            out.flush();
            return;
        }

        out.writeUTF("LOGIN_OK");
        out.writeUTF(nickname);
        out.flush();

        giver.sendAllUserData(nickname, out);
    }
}
