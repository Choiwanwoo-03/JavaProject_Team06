package CommunicateServer;

import Socket.ClientSocket;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

 // 클라이언트 → 서버 요청 전송 클래스 (데이터 직렬화 및 전송 담당)
public class GetInformation {

    private final ClientSocket client;
    private final DataOutputStream out; // 서버로 데이터를 보낼 출력 스트림

    public GetInformation(ClientSocket client) {
        this.client = client;
        this.out = client.getOut(); // 출력 스트림 초기화
    }

    // 로그인 요청 (LOGIN)
    public void sendLogin(String id, String pw) {
        try {
            out.writeUTF("LOGIN"); // 명령 헤더 전송
            out.writeUTF(id);
            out.writeUTF(pw);
            out.flush();
        } catch (IOException e) {
            System.out.println("[CLIENT][GetInformation] 로그인 전송 실패: " + e.getMessage());
        }
    }

    // 회원가입 요청 (REGISTER)
    public void sendRegister(String id, String pw, String nickname) {
        try {
            out.writeUTF("REGISTER"); // 명령 헤더 전송
            out.writeUTF(id);
            out.writeUTF(pw);
            out.writeUTF(nickname);
            out.flush();
        } catch (IOException e) {
            System.out.println("[CLIENT][GetInformation] 회원가입 전송 실패: " + e.getMessage());
        }
    }

    // SYNC_ALL 요청 (REQUEST_SYNC)
    public void sendRequestSync(String nickname) {
        try {
            out.writeUTF("REQUEST_SYNC"); // 명령 헤더 전송
            out.writeUTF(nickname);
            out.flush();
        } catch (IOException e) {
            System.out.println("[CLIENT][GetInformation] REQUEST_SYNC 전송 실패: " + e.getMessage());
        }
    }

    // SAVE_ALL : 종료 시 전체 저장 요청 (모든 사용자 데이터 전송)
    public void sendSaveAll(
            String nickname,
            List<Map<String, Object>> dashboard,
            Map<String, Object> goal,
            Map<String, Object> mission,
            List<String> emoticons
    ) {
        try {
            out.writeUTF("SAVE_ALL"); // 명령 헤더 전송
            out.writeUTF(nickname);

            // Dashboard 데이터 직렬화 및 전송 (개수 포함)
            out.writeInt(dashboard.size());
            for (Map<String, Object> r : dashboard) {
                out.writeUTF(String.valueOf(r.get("DATE")));
                out.writeUTF(String.valueOf(r.get("TIME")));
                out.writeUTF(String.valueOf(r.get("TYPE")));
                out.writeDouble(Double.parseDouble(String.valueOf(r.get("RESULT"))));
                out.writeDouble(Double.parseDouble(String.valueOf(r.get("COUNT"))));
                out.writeUTF(String.valueOf(r.get("UNIT")));
            }

            // Goal 데이터 직렬화 및 전송 (존재 여부 포함)
            out.writeBoolean(goal != null);
            if (goal != null) {
                out.writeUTF(String.valueOf(goal.get("TODAY_RESULT")));
                out.writeUTF(String.valueOf(goal.get("GOAL_RESULT")));
            }

            // Mission 데이터 직렬화 및 전송 (존재 여부 포함)
            out.writeBoolean(mission != null);
            if (mission != null) {
                out.writeUTF(String.valueOf(mission.get("MISSION1_NAME")));
                out.writeInt(Integer.parseInt(String.valueOf(mission.get("MISSION1_SUCCESS"))));

                out.writeUTF(String.valueOf(mission.get("MISSION2_NAME")));
                out.writeInt(Integer.parseInt(String.valueOf(mission.get("MISSION2_SUCCESS"))));

                out.writeUTF(String.valueOf(mission.get("MISSION3_NAME")));
                out.writeInt(Integer.parseInt(String.valueOf(mission.get("MISSION3_SUCCESS"))));
            }

            // Emoticons 데이터 직렬화 및 전송 (개수 포함)
            out.writeInt(emoticons.size());
            for (String name : emoticons) {
                out.writeUTF(name);
            }

            out.flush();

        } catch (IOException e) {
            System.out.println("[CLIENT][GetInformation] SAVE_ALL 전송 실패: " + e.getMessage());
        }
    }

    // UPDATE_GOAL: 목표 설정 시 서버로 전송
    public void sendUpdateGoal(String nickname, String todayResult, String goalResult) {
        try {
            out.writeUTF("UPDATE_GOAL"); // 명령 헤더 전송
            out.writeUTF(nickname);
            out.writeUTF(todayResult);
            out.writeUTF(goalResult);
            out.flush();
            System.out.println("[CLIENT] 목표 업데이트 전송 완료");
        } catch (IOException e) {
            System.out.println("[CLIENT][GetInformation] UPDATE_GOAL 전송 실패: " + e.getMessage());
        }
    }

    // UNLOCK_EMOTICON
    public void sendUnlockEmoticon(String nickname, String name) {
        try {
            out.writeUTF("UNLOCK_EMOTICON"); // 명령 헤더 전송
            out.writeUTF(nickname);
            out.writeUTF(name);
            out.flush();
        } catch (IOException e) {
            System.out.println("[CLIENT][GetInformation] UNLOCK_EMOTICON 전송 실패: " + e.getMessage());
        }
    }
}