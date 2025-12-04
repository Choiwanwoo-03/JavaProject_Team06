public class Main {

    public static void main(String[] args) {
        System.out.println("[SERVER] 서버 프로그램을 시작합니다...");

        // 패키지 경로를 명확히 지정해 이름 충돌 제거
        Socket.ServerSocket server = new Socket.ServerSocket();
        server.startServer();
    }
}
