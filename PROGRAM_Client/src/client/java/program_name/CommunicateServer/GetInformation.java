package CommunicateServer;

import Socket.ClientSocket;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GetInformation {

    private final ClientSocket client;

    public GetInformation(ClientSocket client) {
        this.client = client;
    }

    // ------------------------------
    // 로그인 요청
    // ------------------------------
    public void sendLogin(String id, String pw) {
        try {
            DataOutputStream out = client.getOut();

            out.writeUTF("LOGIN");
            out.writeUTF(id);
            out.writeUTF(pw);
            out.flush();

        } catch (IOException e) {
            System.out.println("[CLIENT] 로그인 전송 오류: " + e.getMessage());
        }
    }

    // ------------------------------
    // 회원가입 요청
    // ------------------------------
    public void sendRegister(String id, String pw, String nickname) {
        try {
            DataOutputStream out = client.getOut();

            out.writeUTF("REGISTER");
            out.writeUTF(id);
            out.writeUTF(pw);
            out.writeUTF(nickname);
            out.flush();

        } catch (IOException e) {
            System.out.println("[CLIENT] 회원가입 전송 오류: " + e.getMessage());
        }
    }

    // ------------------------------
    // 프로그램 종료 시 전체 SAVE
    // ------------------------------
    public void sendSaveAll(
            String nickname,
            List<Map<String, Object>> dashboard,
            Map<String, Object> goal,
            Map<String, Object> mission,
            List<String> emoticons
    ) {
        try {
            DataOutputStream out = client.getOut();

            out.writeUTF("SAVE_ALL");
            out.writeUTF(nickname);

            // Dashboard
            out.writeInt(dashboard.size());
            for (Map<String, Object> row : dashboard) {
                out.writeUTF((String) row.get("DATE"));
                out.writeUTF((String) row.get("TIME"));
                out.writeUTF((String) row.get("TYPE"));
                out.writeDouble((Double) row.get("RESULT"));
                out.writeDouble((Double) row.get("COUNT"));
                out.writeUTF((String) row.get("UNIT"));
            }

            // Goal
            boolean hasGoal = goal != null;
            out.writeBoolean(hasGoal);
            if (hasGoal) {
                out.writeUTF(goal.get("TODAY_RESULT").toString());
                out.writeUTF(goal.get("GOAL_RESULT").toString());
            }

            // Mission
            boolean hasMission = mission != null;
            out.writeBoolean(hasMission);
            if (hasMission) {
                out.writeUTF((String) mission.get("TODAY_MISSION"));
                out.writeInt((Integer) mission.get("SUCCESS_MISSION"));
            }

            // Emoticons
            out.writeInt(emoticons.size());
            for (String emo : emoticons) {
                out.writeUTF(emo);
            }

            out.flush();

        } catch (Exception e) {
            System.out.println("[CLIENT] SAVE_ALL 전송 오류: " + e.getMessage());
        }
    }

    // ------------------------------
    // 실시간 미션 업데이트
    // ------------------------------
    public void sendUpdateMission(String nickname, String date, String todayMission, int successCount) {
        try {
            DataOutputStream out = client.getOut();

            out.writeUTF("UPDATE_MISSION");
            out.writeUTF(nickname);
            out.writeUTF(date);
            out.writeUTF(todayMission);
            out.writeInt(successCount);
            out.flush();

        } catch (Exception e) {
            System.out.println("[CLIENT] UPDATE_MISSION 전송 오류: " + e.getMessage());
        }
    }

    // ------------------------------
    // 이모티콘 해금
    // ------------------------------
    public void sendUnlockEmoticon(String nickname, String emoticonName) {
        try {
            DataOutputStream out = client.getOut();

            out.writeUTF("UNLOCK_EMOTICON");
            out.writeUTF(nickname);
            out.writeUTF(emoticonName);
            out.flush();

        } catch (Exception e) {
            System.out.println("[CLIENT] UNLOCK_EMOTICON 전송 오류: " + e.getMessage());
        }
    }
}