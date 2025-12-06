package Socket;

import CommunicateClient.GetInformation;
import CommunicateClient.GiveInformation;
import CommunicateClient.LoginManager;
import database.DataWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 각 클라이언트와 1:1로 통신하는 서버 측 스레드
 * - 회원가입 / 로그인 처리
 * - 클라이언트 데이터 저장 및 동기화 요청 처리
 */
public class ConnectedClient extends Thread {

    // 클라이언트와 연결된 소켓
    private final Socket socket;

    // 서버 메인 소켓 (접속 리스트 관리용)
    private final ServerSocket server;

    // 클라이언트와의 입출력 스트림
    private DataInputStream in;
    private DataOutputStream out;

    // 로그인 성공 시 저장되는 사용자 닉네임
    private String nickname;

    // 로그인 인증용
    private final LoginManager loginManager = new LoginManager();

    // 클라이언트 → 서버 데이터 수신 처리 담당
    private final GetInformation getter = new GetInformation();

    // 서버 → 클라이언트 데이터 송신 담당
    private GiveInformation giver;

    public ConnectedClient(Socket socket, ServerSocket server) {
        this.socket = socket;
        this.server = server;
    }

    /**
     * 클라이언트와의 통신을 계속 대기하는 메인 스레드 루프
     */
    @Override
    public void run() {
        try {
            // 스트림 초기화
            in  = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // 출력 스트림이 준비된 후 송신 객체 생성
            this.giver = new GiveInformation(out);

            System.out.println("[SERVER] 클라이언트 수신 스레드 시작");

            while (true) {
                // 클라이언트 명령 수신
                String cmd = in.readUTF();
                System.out.println("[SERVER] CMD 수신: " + cmd);

                if ("LOGIN".equals(cmd)) {
                    handleLogin();
                } else if ("REGISTER".equals(cmd)) {
                    handleRegister();
                } else if ("REQUEST_SYNC".equals(cmd)) {
                    handleRequestSync();
                } else {
                    // SAVE_ALL, UPDATE_MISSION, UNLOCK_EMOTICON 등 처리
                    getter.handle(cmd, in);
                }
            }

        } catch (IOException e) {
            System.out.println("[SERVER] 클라이언트 통신 종료: " + e.getMessage());
        } finally {
            close();
        }
    }

    /**
     * 로그인 처리
     */
    private void handleLogin() throws IOException {
        String id = in.readUTF();
        String pw = in.readUTF();

        System.out.println("[SERVER][LOGIN] 요청 수신 - ID=" + id);

        // DB에서 ID/PW 검증
        String nick = loginManager.verify(id, pw);

        if (nick != null) {
            this.nickname = nick;

            out.writeUTF("LOGIN_OK");
            out.writeUTF(nick);

            // 로그인 성공 시 즉시 전체 데이터 동기화 전송
            giver.sendAllUserData(nick);

        } else {
            out.writeUTF("LOGIN_FAIL");
        }
        out.flush();
    }

    /**
     * 회원가입 처리
     */
    private void handleRegister() throws IOException {
        String id = in.readUTF();
        String pw = in.readUTF();
        String nickname = in.readUTF();

        DataWriter writer = new DataWriter();
        boolean ok = writer.registerUser(id, pw, nickname);

        if (ok) {
            out.writeUTF("REGISTER_OK");
        } else {
            out.writeUTF("REGISTER_FAIL");
        }
        out.flush();
    }

    /**
     * 클라이언트가 수동으로 전체 데이터 동기화를 요청할 때 처리
     */
    private void handleRequestSync() throws IOException {
        String nick = in.readUTF();
        giver.sendAllUserData(nick);
        out.flush();
    }

    /**
     * 소켓 종료 및 서버 클라이언트 목록에서 제거
     */
    private void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignore) {
        }

        if (server != null) {
            server.removeClient(this);
        }

        System.out.println("[SERVER] 클라이언트 연결 종료 및 정리 완료");
    }
}
