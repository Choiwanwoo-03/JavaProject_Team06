package CommunicateClient;

import database.DataReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class GiveInformation {

    private final DataReader reader = new DataReader();
    private final DataOutputStream out;

    public GiveInformation(DataOutputStream out) {
        this.out = out;
    }

    // =====================================================================
    //  SYNC_ALL : 로그인 직후 클라이언트 전체 데이터 동기화
    // =====================================================================
    public void sendAllUserData(String nickname) throws IOException {

        String today = java.time.LocalDate.now().toString();

        // ---- DB에서 데이터 조회 ----
        List<Map<String, Object>> dashboard = reader.getDashboard(nickname);
        Map<String, Object> goal = reader.getGoal(nickname);
        Map<String, Object> mission = reader.getMission(nickname);
        List<String> emoticons = reader.getEmoticons(nickname);

        // ---- SYNC_ALL 헤더 ----
        out.writeUTF("SYNC_ALL");

        // ============================================================
        // 1) Dashboard
        // ============================================================
        out.writeInt(dashboard.size());

        for (Map<String, Object> r : dashboard) {
            out.writeUTF(String.valueOf(r.get("DATE")));
            out.writeUTF(String.valueOf(r.get("TIME")));
            out.writeUTF(String.valueOf(r.get("TYPE")));

            // RESULT / COUNT → 클라이언트는 double 로 읽음
            out.writeDouble(Double.parseDouble(String.valueOf(r.get("RESULT"))));
            out.writeDouble(Double.parseDouble(String.valueOf(r.get("COUNT"))));

            out.writeUTF(String.valueOf(r.get("UNIT")));
        }

        // ============================================================
        // 2) Goal (boolean + 데이터)
        // ============================================================
        if (goal != null) {
            out.writeBoolean(true);
            out.writeUTF(String.valueOf(goal.get("TODAY_RESULT")));
            out.writeUTF(String.valueOf(goal.get("GOAL_RESULT")));
        } else {
            out.writeBoolean(false);
        }

        // ============================================================
        // 3) Mission (boolean + todayMission + successMission)
        // ============================================================
        if (mission != null) {
            out.writeBoolean(true);
            
            // 미션 1
            out.writeUTF(String.valueOf(mission.getOrDefault("MISSION1_NAME", "")));
            out.writeInt(parseIntSafe(mission.get("MISSION1_SUCCESS")));

            // 미션 2
            out.writeUTF(String.valueOf(mission.getOrDefault("MISSION2_NAME", "")));
            out.writeInt(parseIntSafe(mission.get("MISSION2_SUCCESS")));

            // 미션 3
            out.writeUTF(String.valueOf(mission.getOrDefault("MISSION3_NAME", "")));
            out.writeInt(parseIntSafe(mission.get("MISSION3_SUCCESS")));
            
        } else {
            out.writeBoolean(false);
        }

        // ============================================================
        // 4) Emoticon 목록
        // ============================================================
        out.writeInt(emoticons.size());
        for (String e : emoticons) {
            out.writeUTF(e);
        }

        out.flush();
        System.out.println("[SERVER] SYNC_ALL 전송 완료 (" + nickname + ")");
    }
    
    private int parseIntSafe(Object o) {
        if (o == null) return 0;
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return 0; }
    }

    // Helper: Null 안전한 Double 변환
    private double parseDoubleSafe(Object o) {
        if (o == null) return 0.0;
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return 0.0; }
    }
}
