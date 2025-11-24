public class Main {

    public static void main(String[] args) {
        System.out.println("[SERVER] 서버 프로그램을 시작합니다...");

        // ServerSocket 클래스 실행
        ServerSocket server = new ServerSocket();
        server.startServer();  // 서버 시작
    }
}
