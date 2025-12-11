package CommunicateClient;

import database.DataReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class GiveInformation { // 클라이언트에 데이터 제공

    private final DataReader reader = new DataReader();
    private final DataOutputStream out;

    public GiveInformation(DataOutputStream out) { // 출력 스트림 초기화
        this.out = out;
    }

    // SYNC_ALL : 로그인 직후 클라이언트 전체 데이터 동기화
    public void sendAllUserData(String nickname) throws IOException {

        String today = java.time.LocalDate.now().toString();

        // ---- DB에서 데이터 조회 ----
        List<Map<String, Object>> dashboard = reader.getDashboard(nickname);
        Map<String, Object> goal = reader.getGoal(nickname);
        Map<String, Object> mission = reader.getMission(nickname);
        List<String> emoticons = reader.getEmoticons(nickname);

        // ---- SYNC_ALL 헤더 ----
        out.writeUTF("SYNC_ALL"); // 명령 헤더 전송

        // Dashboard
        out.writeInt(dashboard.size()); // 항목 개수 전송

        for (Map<String, Object> r : dashboard) { // 대시보드 데이터 전송
            out.writeUTF(String.valueOf(r.get("DATE")));
            out.writeUTF(String.valueOf(r.get("TIME")));
            out.writeUTF(String.valueOf(r.get("TYPE")));

            out.writeDouble(Double.parseDouble(String.valueOf(r.get("RESULT"))));
            out.writeDouble(Double.parseDouble(String.valueOf(r.get("COUNT"))));

            out.writeUTF(String.valueOf(r.get("UNIT")));
        }

        // Goal
        if (goal != null) {
            out.writeBoolean(true); // 목표 존재 여부
            out.writeUTF(String.valueOf(goal.get("TODAY_RESULT")));
            out.writeUTF(String.valueOf(goal.get("GOAL_RESULT")));
        } else {
            out.writeBoolean(false);
        }

        // Mission
        if (mission != null) {
            out.writeBoolean(true); // 미션 존재 여부
            
            // 미션 1 데이터 전송
            out.writeUTF(String.valueOf(mission.getOrDefault("MISSION1_NAME", "")));
            out.writeInt(parseIntSafe(mission.get("MISSION1_SUCCESS")));

            // 미션 2 데이터 전송
            out.writeUTF(String.valueOf(mission.getOrDefault("MISSION2_NAME", "")));
            out.writeInt(parseIntSafe(mission.get("MISSION2_SUCCESS")));

            // 미션 3 데이터 전송
            out.writeUTF(String.valueOf(mission.getOrDefault("MISSION3_NAME", "")));
            out.writeInt(parseIntSafe(mission.get("MISSION3_SUCCESS")));
            
        } else {
            out.writeBoolean(false);
        }

        // Emoticon 목표
        out.writeInt(emoticons.size()); // 이모티콘 개수 전송
        for (String e : emoticons) {
            out.writeUTF(e); // 이모티콘 이름 전송
        }

        out.flush(); // 데이터 전송
        System.out.println("[SERVER] SYNC_ALL 전송 완료 (" + nickname + ")");
    }
    
    // Helper: Null 안전한 int 변환
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