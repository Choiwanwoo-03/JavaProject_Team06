package Socket;

import CommunicateClient.GetInformation;
import CommunicateClient.GiveInformation;
import CommunicateClient.LoginManager;
import database.DataWriter;
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
                String cmd = in.readUTF();   // ★ 명령어를 여기서만 읽음

                if (cmd.equals("LOGIN")) {
                    handleLogin();
                    continue;
                }

                if (cmd.equals("REGISTER")) {
                    handleRegister();
                    continue;
                }

                // ★ cmd를 그대로 넘김
                getter.handle(cmd, in);
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

        LoginManager loginManager = new LoginManager();
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
    
 // ================== 회원가입 처리 ==================
    private void handleRegister() {
        try {
            // 1) 클라이언트가 보낸 회원가입 정보 읽기
            String userId   = in.readUTF();
            String userPwd  = in.readUTF();
            String nickname = in.readUTF();

            System.out.println("[SERVER][REGISTER] 요청 수신 - ID=" + userId + ", nickname=" + nickname);

            // 2) DB에 사용자 등록
            DataWriter writer = new DataWriter();
            boolean success   = writer.registerUser(userId, userPwd, nickname);

            // 3) 결과 전송
            if (success) {
                out.writeUTF("REGISTER_OK");
                System.out.println("[SERVER][REGISTER] 성공 - ID=" + userId);
            } else {
                // 중복 ID 또는 기타 오류
                out.writeUTF("REGISTER_FAIL");
                System.out.println("[SERVER][REGISTER] 실패(중복 또는 오류) - ID=" + userId);
            }
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                // 예외 발생 시에도 클라이언트 측에서 처리할 수 있도록 FAIL 전송
                out.writeUTF("REGISTER_FAIL");
                out.flush();
            } catch (Exception ignore) {}
        }
    }

}
