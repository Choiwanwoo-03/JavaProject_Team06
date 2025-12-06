package Socket;

import CommunicateServer.GetInformation;
import CommunicateServer.GiveInformation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * 클라이언트에서 서버와 직접 통신을 담당하는 소켓 클래스
 * - 서버 연결
 * - 로그인/회원가입 결과 수신
 * - SYNC_ALL 수신 처리
 */
public class ClientSocket {

    // 서버와 연결된 소켓 및 입출력 스트림
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    // --- 통신 처리 클래스 ---
    private GetInformation getInformation;   // 서버 → 클라이언트 수신 처리
    private GiveInformation giveInformation; // 클라이언트 → 서버 송신 처리

    // --- 로그인 결과 콜백 인터페이스 ---
    public interface LoginListener {
        void onLoginSuccess(String nickname); // 로그인 성공 시 호출
        void onLoginFail();                   // 로그인 실패 시 호출
    }
    private LoginListener loginListener;

    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }

    // --- SYNC_ALL 수신 콜백 등록 ---
    public void setSyncListener(GiveInformation.SyncListener listener) {
        if (giveInformation != null) {
            giveInformation.setSyncListener(listener);
        }
    }
    
    public GetInformation getGetInformation() {
        return getInformation;
    }

    public GiveInformation getGiveInformation() {
        return giveInformation;
    }

    // ======================================================
    // 서버 연결
    // ======================================================
    public boolean connectToServer(String host, int port) {
        try {
            // 서버 소켓 연결 및 스트림 생성
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // 통신 처리 객체 생성
            getInformation = new GetInformation(this);
            giveInformation = new GiveInformation(this);

            System.out.println("[CLIENT] 서버 연결 성공");

            // 서버 수신 대기 스레드 시작
            startReceiveLoop();
            return true;

        } catch (Exception e) {
            System.out.println("[CLIENT] 서버 연결 실패: " + e.getMessage());
            return false;
        }
    }

    // ======================================================
    // 서버 수신 스레드 (계속 서버 메시지를 대기)
    // ======================================================
    private void startReceiveLoop() {

        Thread t = new Thread(() -> {
            try {
                while (true) {

                    // 서버에서 명령어 수신
                    String cmd = in.readUTF();

                    // ===============================
                    // SYNC_ALL (DB 데이터 전체 수신)
                    // ===============================
                    if (cmd.equals("SYNC_ALL")) {
                        giveInformation.handleSyncAll();
                        continue;
                    }

                    // ===============================
                    // LOGIN_OK (로그인 성공)
                    // ===============================
                    if (cmd.equals("LOGIN_OK")) {
                        String nickname = in.readUTF();
                        System.out.println("[CLIENT] 로그인 성공: " + nickname);

                        if (loginListener != null) {
                            loginListener.onLoginSuccess(nickname);
                        }
                        continue;
                    }

                    // ===============================
                    // LOGIN_FAIL (로그인 실패)
                    // ===============================
                    if (cmd.equals("LOGIN_FAIL")) {
                        System.out.println("[CLIENT] 로그인 실패");
                        if (loginListener != null) {
                            loginListener.onLoginFail();
                        }
                        continue;
                    }

                    // ===============================
                    // REGISTER_OK (회원가입 성공)
                    // ===============================
                    if (cmd.equals("REGISTER_OK")) {
                        System.out.println("[CLIENT] 회원가입 성공");
                        continue;
                    }

                    // ===============================
                    // REGISTER_FAIL (회원가입 실패)
                    // ===============================
                    if (cmd.equals("REGISTER_FAIL")) {
                        System.out.println("[CLIENT] 회원가입 실패");
                        continue;
                    }

                    // ===============================
                    // 알 수 없는 명령어 수신
                    // ===============================
                    System.out.println("[CLIENT] Unknown command from server: " + cmd);
                }

            } catch (Exception e) {
                System.out.println("[CLIENT] 서버 수신 루프 종료");
            }
        });

        // 프로그램 종료 시 같이 종료되도록 데몬 스레드 설정
        t.setDaemon(true);
        t.start();
    }

    // ======================================================
    // Getter (외부 클래스에서 스트림 접근용)
    // ======================================================
    public DataOutputStream getOut() { return out; }
    public DataInputStream getIn() { return in; }
    public GetInformation getGetter() { return getInformation; }
    public GiveInformation getGiver() { return giveInformation; }

    // ======================================================
    // 소켓 종료
    // ======================================================
    public void close() {
        try {
            if (socket != null) socket.close();
            System.out.println("[CLIENT] 소켓 종료 완료");
        } catch (Exception e) {
            System.out.println("[CLIENT] 소켓 종료 실패: " + e.getMessage());
        }
    }
}
