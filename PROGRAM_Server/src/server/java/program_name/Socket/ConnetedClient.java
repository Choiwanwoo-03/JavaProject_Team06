package Socket;

import CommunicateClient.GetInformation;
import CommunicateClient.GiveInformation;
import CommunicateClient.LoginManager;
import database.DataWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

 // 각 클라이언트와 1:1로 통신하는 서버 측 스레드
 // - LOGIN / REGISTER
 // - SAVE_ALL / UPDATE_MISSION / UNLOCK_EMOTICON / REQUEST_SYNC 처리
public class ConnectedClient extends Thread { // 클라이언트와의 통신을 전담하는 스레드

    private final Socket socket;
    private final ServerSocket server;      // 서버 소켓 참조

    private DataInputStream in;             // 클라이언트로부터 데이터 수신 스트림
    private DataOutputStream out;           // 클라이언트로 데이터 전송 스트림

    private String nickname;                // 로그인 성공 후 사용자 닉네임

    // 외부 로직 처리 클래스 인스턴스
    private final LoginManager loginManager = new LoginManager();
    private final GetInformation getter = new GetInformation(); // 클라이언트 데이터 수신 처리 (DB 쓰기)
    private GiveInformation giver;                               // 클라이언트 데이터 전송 처리 (DB 읽기)

    public ConnectedClient(Socket socket, ServerSocket server) { // 생성자
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() { // 스레드 시작 시 실행
        try {
            in  = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            this.giver = new GiveInformation(out); // 출력 스트림이 준비된 후 GiveInformation 초기화

            System.out.println("[SERVER] 클라이언트 수신 스레드 시작");

            while (true) { // 무한 루프: 클라이언트로부터 명령(CMD)을 대기 및 수신
                String cmd = in.readUTF(); // 명령 수신
                System.out.println("[SERVER] CMD 수신: " + cmd);

                if ("LOGIN".equals(cmd)) {
                    handleLogin(); // 로그인 처리
                } else if ("REGISTER".equals(cmd)) {
                    handleRegister(); // 회원가입 처리
                } else if ("REQUEST_SYNC".equals(cmd)) {
                    handleRequestSync(); // 동기화 요청 처리
                } else {
                    // SAVE_ALL 등 데이터 저장 관련 CMD는 GetInformation에 위임
                    getter.handle(cmd, in);
                }
            }

        } catch (IOException e) {
            System.out.println("[SERVER] 클라이언트 통신 종료: " + e.getMessage());
        } finally {
            close(); // 연결 종료 시 자원 정리
        }
    }

    // LOGIN 처리: ID/PW 검증 및 SYNC_ALL 전송
    private void handleLogin() throws IOException {
        String id = in.readUTF();
        String pw = in.readUTF();

        System.out.println("[SERVER][LOGIN] 요청 수신 - ID=" + id);

        String nick = loginManager.verify(id, pw); // LoginManager를 통해 DB에서 검증

        if (nick != null) {
            this.nickname = nick; // 닉네임 저장 (로그인 성공)

            out.writeUTF("LOGIN_OK");
            out.writeUTF(nick);

            giver.sendAllUserData(nick); // 로그인 직후 전체 데이터 동기화 전송
        } else {
            System.out.println("[SERVER][LOGIN] 실패 - ID 또는 PW 불일치");
            out.writeUTF("LOGIN_FAIL");
        }
        out.flush();
    }

    // REGISTER 처리: 사용자 등록
    private void handleRegister() throws IOException {
        String id = in.readUTF();
        String pw = in.readUTF();
        String nickname = in.readUTF();

        System.out.println("[SERVER][REGISTER] 요청 수신 - ID=" + id + ", NICK=" + nickname);

        DataWriter writer = new DataWriter();
        boolean ok = writer.registerUser(id, pw, nickname); // DB에 사용자 등록

        if (ok) {
            System.out.println("[SERVER][REGISTER] 성공");
            out.writeUTF("REGISTER_OK");
        } else {
            System.out.println("[SERVER][REGISTER] 실패 - 중복 ID 또는 닉네임?");
            out.writeUTF("REGISTER_FAIL");
        }
        out.flush();
    }

    // REQUEST_SYNC 처리: 수동 데이터 동기화 요청
    private void handleRequestSync() throws IOException {
        String nick = in.readUTF();
        System.out.println("[SERVER][REQUEST_SYNC] 닉네임=" + nick);

        giver.sendAllUserData(nick); // SYNC_ALL 데이터 재전송
        out.flush();
    }

    // 소켓/리소스 정리
    private void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // 소켓 닫기
            }
        } catch (IOException ignore) {
        }

        if (server != null) {
            server.removeClient(this); // ServerSocket의 클라이언트 목록에서 자신을 제거
        }

        System.out.println("[SERVER] 클라이언트 연결 종료 및 정리 완료");
    }
}