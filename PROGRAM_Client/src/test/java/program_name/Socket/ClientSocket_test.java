package Socket;

import CommunicateServer.GetInformation; // 서버로 데이터 전송 담당 클래스
import CommunicateServer.GiveInformation; // 서버로부터 데이터 수신 담당 클래스

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientSocket { // 클라이언트 측 소켓 연결 및 통신 관리

    private Socket socket;
    private DataInputStream in; // 서버 수신 스트림
    private DataOutputStream out; // 서버 전송 스트림

    // --- 통신 클래스 ---
    private GetInformation getInformation; // 서버로 요청
    private GiveInformation giveInformation; // 서버 응답 처리

    // --- 로그인 콜백 ---
    public interface LoginListener { // 로그인 결과 처리를 위한 인터페이스
        void onLoginSuccess(String nickname);
        void onLoginFail();
    }
    private LoginListener loginListener;

    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }

    // --- SYNC_ALL 콜백 등록 ---
    public void setSyncListener(GiveInformation.SyncListener listener) {
        if (giveInformation != null) {
            giveInformation.setSyncListener(listener); // 데이터 수신 클래스에 리스너 설정
        }
    }
    
    public GetInformation getGetInformation() {
        return getInformation;
    }

    public GiveInformation getGiveInformation() {
        return giveInformation;
    }


    // 서버 연결
    public boolean connectToServer(String host, int port) { // 서버 연결 시도 및 스트림 초기화
        try {
            socket = new Socket(host, port); // 소켓 연결 시도
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            getInformation = new GetInformation(this); // 전송 클래스 초기화
            giveInformation = new GiveInformation(this); // 수신 클래스 초기화

            System.out.println("[CLIENT] 서버 연결 성공");

            startReceiveLoop(); // 서버 수신 스레드 시작
            return true;

        } catch (Exception e) {
            System.out.println("[CLIENT] 서버 연결 실패: " + e.getMessage());
            return false;
        }
    }


    // 서버 수신 스레드
    private void startReceiveLoop() { // 서버로부터 응답을 대기하는 스레드 생성

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    // 서버에서 명령어 수신
                    String cmd = in.readUTF();

                    // SYNC_ALL (DB → 클라이언트 로딩)
                    if (cmd.equals("SYNC_ALL")) {
                        giveInformation.handleSyncAll(); // 수신 데이터 처리 위임
                        continue;
                    }

                    // LOGIN_OK
                    if (cmd.equals("LOGIN_OK")) {
                        String nickname = in.readUTF();
                        System.out.println("[CLIENT] 로그인 성공: " + nickname);

                        if (loginListener != null) {
                            loginListener.onLoginSuccess(nickname); // 성공 콜백 호출
                        }
                        continue;
                    }

                    // LOGIN_FAIL
                    if (cmd.equals("LOGIN_FAIL")) {
                        System.out.println("[CLIENT] 로그인 실패");
                        if (loginListener != null) {
                            loginListener.onLoginFail(); // 실패 콜백 호출
                        }
                        continue;
                    }

                    // REGISTER_OK / REGISTER_FAIL 등 처리
                    if (cmd.equals("REGISTER_OK")) {
                        System.out.println("[CLIENT] 회원가입 성공");
                        continue;
                    }
                    if (cmd.equals("REGISTER_FAIL")) {
                        System.out.println("[CLIENT] 회원가입 실패");
                        continue;
                    }

                    // ===============================
                    // 기타 명령어가 올 경우
                    // ===============================
                    System.out.println("[CLIENT] Unknown command from server: " + cmd);
                }

            } catch (Exception e) {
                System.out.println("[CLIENT] 서버 수신 루프 종료");
            }
        });

        t.setDaemon(true); // 데몬 스레드로 설정 (메인 스레드 종료 시 함께 종료)
        t.start();
    }

    // Getter
    public DataOutputStream getOut() { return out; }
    public DataInputStream getIn() { return in; }
    public GetInformation getGetter() { return getInformation; }
    public GiveInformation getGiver() { return giveInformation; }

    // 소켓 종료
    public void close() {
        try {
            if (socket != null) socket.close();
            System.out.println("[CLIENT] 소켓 종료 완료");
        } catch (Exception e) {
            System.out.println("[CLIENT] 소켓 종료 실패: " + e.getMessage());
        }
    }
}