package CommunicateClient;

import database.DataReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 서버 → 클라이언트 데이터 전송 담당
 * DB 조회는 DataReader가 수행하고,
 * 이 클래스는 그 데이터를 오직 클라이언트로 보내는 역할만 수행한다.
 */
public class GiveInformation {

    private final DataReader reader = new DataReader();

    /**
     * 로그인 성공 후, 클라이언트에게 전체 동기화 데이터(SYNC_ALL)를 전송한다.
     * 전달 내용: Dashboard, Goal, Mission, Emoticons
     */
    public void sendAllUserData(String nickname, DataOutputStream out) throws IOException {

        String today = LocalDate.now().toString();

        // DB에서 데이터 읽기 (DataReader 사용)
        List<Map<String, Object>> dashboard = reader.readDashboard(nickname);
        Map<String, Object> goal = reader.readGoal(nickname, today);
        Map<String, Object> mission = reader.readMission(nickname, today);
        List<String> emoticons = reader.readEmoticons(nickname);

        // ===== 클라이언트로 데이터 전송 =====
        out.writeUTF("SYNC_ALL");

        // 대시보드 전송
        out.writeInt(dashboard.size());
        for (Map<String, Object> row : dashboard) {
            out.writeUTF((String) row.get("DATE"));
            out.writeUTF((String) row.get("TIME"));
            out.writeUTF((String) row.get("TYPE"));
            out.writeDouble((Double) row.get("RESULT"));
            out.writeDouble((Double) row.get("COUNT"));
            out.writeUTF((String) row.get("UNIT"));
        }

        // 목표 전송
        boolean hasGoal = goal.get("TODAY_RESULT") != null;
        out.writeBoolean(hasGoal);
        if (hasGoal) {
            out.writeUTF((String) goal.get("TODAY_RESULT"));
            out.writeUTF((String) goal.get("GOAL_RESULT"));
        }

        // 미션 전송
        boolean hasMission = mission.get("TODAY_MISSION") != null;
        out.writeBoolean(hasMission);
        if (hasMission) {
            out.writeUTF((String) mission.get("TODAY_MISSION"));
            out.writeInt((Integer) mission.getOrDefault("SUCCESS_MISSION", 0));
        }

        // 이모티콘 전송
        out.writeInt(emoticons.size());
        for (String emo : emoticons) {
            out.writeUTF(emo);
        }

        out.flush();
    }
}
