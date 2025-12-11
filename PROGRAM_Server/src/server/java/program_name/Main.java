public class Main { // 메인 클래스

    public static void main(String[] args) { // 프로그램 시작점
        // 서버 시작 메시지 출력
        System.out.println("[SERVER] 서버 프로그램을 시작합니다...");

        // Socket.ServerSocket 객체 생성
        Socket.ServerSocket server = new Socket.ServerSocket();
        
        // 서버 구동 메서드 호출
        server.startServer();
    }
}