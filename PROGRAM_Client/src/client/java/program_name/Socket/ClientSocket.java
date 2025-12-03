package Socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientSocket {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    // 서버 연결
    public boolean connectToServer(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            System.out.println("[CLIENT] 서버 연결 성공: " + ip + ":" + port);

            // 서버로부터 데이터 읽는 스레드는 이후 구성 예정
            // new Thread(this::receiveLoop).start();

            return true;
        } catch (Exception e) {
            System.out.println("[CLIENT] 서버 연결 실패: " + e.getMessage());
            return false;
        }
    }

    // UTF 메시지 보내기
    public synchronized void sendUTF(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("[CLIENT] 메시지 전송 오류: " + e.getMessage());
        }
    }

    // 서버 메시지 수신 루프 (기능은 CommunicateServer에서 구현 예정)
    private void receiveLoop() {
        try {
            while (true) {
                String msg = in.readUTF();
                System.out.println("[SERVER] " + msg);
                // 실제 처리 로직은 CommunicateServer로 분리 예정
            }
        } catch (Exception e) {
            System.out.println("[CLIENT] 서버 메시지 수신 종료");
        }
    }

    // 소켓 종료
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
    }

    // Getter (다른 클래스에서 스트림 직접 사용 가능하도록)
    public DataInputStream getIn() { return in; }
    public DataOutputStream getOut() { return out; }
}
