package CommunicateClient;

import database.DataWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class GetInformation {

    private final DataWriter writer = new DataWriter();

    // ConnectedClient에서 cmd를 먼저 읽고 넘겨줌
    public void handle(String cmd, DataInputStream in) throws IOException {

        switch (cmd) {
            case "SAVE_ALL":
                handleSaveAll(in);
                break;

            case "UPDATE_MISSION":
                handleUpdateMission(in);
                break;

            // [추가] 목표만 별도로 업데이트
            case "UPDATE_GOAL":
                handleUpdateGoal(in);
                break;

            case "UNLOCK_EMOTICON":
                handleUnlockEmoticon(in);
                break;

            default:
                System.out.println("[SERVER][GetInformation] Unknown command: " + cmd);
        }
    }

    // =====================================================================
    // SAVE_ALL : Dashboard + Goal + Mission(1/2/3) + Emoticon 저장
    // =====================================================================
    private void handleSaveAll(DataInputStream in) throws IOException {

        String nickname = in.readUTF();

        // ------------------ Dashboard ------------------
        int rowCount = in.readInt();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < rowCount; i++) {
            Map<String, Object> r = new HashMap<String, Object>();
            r.put("DATE", in.readUTF());
            r.put("TIME", in.readUTF());
            r.put("TYPE", in.readUTF());

            r.put("RESULT", in.readDouble());
            r.put("COUNT", in.readDouble());
            r.put("UNIT", in.readUTF());

            rows.add(r);
        }

        // ------------------ Goal ------------------
        boolean hasGoal = in.readBoolean();
        String todayResult = "0";
        String goalResult  = "0";
        if (hasGoal) {
            todayResult = in.readUTF();
            goalResult  = in.readUTF();
        }

        // ------------------ Mission ------------------
        boolean hasMission = in.readBoolean();
        Map<String, Object> mission = new HashMap<>();
        if (hasMission) {
            mission.put("MISSION1_NAME", in.readUTF());
            mission.put("MISSION1_SUCCESS", in.readInt());

            mission.put("MISSION2_NAME", in.readUTF());
            mission.put("MISSION2_SUCCESS", in.readInt());

            mission.put("MISSION3_NAME", in.readUTF());
            mission.put("MISSION3_SUCCESS", in.readInt());
        }

        // ------------------ Emoticons ------------------
        int emoCount = in.readInt();
        List<String> emoticons = new ArrayList<>();
        for (int i = 0; i < emoCount; i++) {
            emoticons.add(in.readUTF());
        }

        // ====================================
        // DB 저장
        // ====================================
        writer.replaceDashboard(nickname, rows);
        
        // 목표 저장
        if (hasGoal) {
            writer.upsertGoal(nickname, todayResult, goalResult);
        }
        
        // 미션 저장
        if (hasMission) {
            writer.upsertMission(nickname, mission);
        }
        
        // 이모티콘 저장
        if (!emoticons.isEmpty()) {
            writer.insertEmoticon(nickname, emoticons);
        }

        System.out.println("[SERVER] SAVE_ALL 처리 완료: " + nickname);
    }

    // =====================================================================
    // UPDATE_MISSION : 미션 1/2/3 업데이트
    // =====================================================================
    private void handleUpdateMission(DataInputStream in) throws IOException {
        String nickname = in.readUTF();

        Map<String, Object> mission = new HashMap<String, Object>();
        mission.put("MISSION1_NAME", in.readUTF());
        mission.put("MISSION1_SUCCESS", in.readInt());

        mission.put("MISSION2_NAME", in.readUTF());
        mission.put("MISSION2_SUCCESS", in.readInt());

        mission.put("MISSION3_NAME", in.readUTF());
        mission.put("MISSION3_SUCCESS", in.readInt());

        writer.upsertMission(nickname, mission);

        System.out.println("[SERVER] UPDATE_MISSION 저장 완료 (" + nickname + ")");
    }

    // =====================================================================
    // [추가] UPDATE_GOAL : 목표 업데이트
    // =====================================================================
    private void handleUpdateGoal(DataInputStream in) throws IOException {
        String nickname = in.readUTF();
        String todayResult = in.readUTF();
        String goalResult = in.readUTF();

        // DB에 저장 (DataWriter에 이미 upsertGoal 메서드가 있음)
        writer.upsertGoal(nickname, todayResult, goalResult);

        System.out.println("[SERVER] UPDATE_GOAL 저장 완료 (" + nickname + ") : " + goalResult);
    }

    // =====================================================================
    // UNLOCK_EMOTICON
    // =====================================================================
    private void handleUnlockEmoticon(DataInputStream in) throws IOException {
        String nickname = in.readUTF();
        String name = in.readUTF();

        writer.insertSingleEmoticon(nickname, name);

        System.out.println("[SERVER] 이모티콘 해금: " + name);
    }
}