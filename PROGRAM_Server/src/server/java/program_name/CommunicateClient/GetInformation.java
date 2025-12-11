package CommunicateClient;

import database.DataWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class GetInformation {

    private final DataWriter writer = new DataWriter(); // DB 저장 객체 초기화

    // 클라이언트의 명령(cmd)에 따라 데이터 처리 메서드를 분기
    public void handle(String cmd, DataInputStream in) throws IOException {

        switch (cmd) {
            case "SAVE_ALL":
                handleSaveAll(in);
                break;

            case "UPDATE_MISSION":
                handleUpdateMission(in);
                break;

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

    // SAVE_ALL: 대시보드, 목표, 미션, 이모티콘 등 모든 데이터를 읽어 DB에 저장
    private void handleSaveAll(DataInputStream in) throws IOException {

        String nickname = in.readUTF();

        // ------------------ Dashboard ------------------
        int rowCount = in.readInt();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < rowCount; i++) {
            // 대시보드 데이터를 스트림에서 읽어 Map에 저장
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
            // 목표 데이터를 스트림에서 읽음
            todayResult = in.readUTF();
            goalResult  = in.readUTF();
        }

        // ------------------ Mission ------------------
        boolean hasMission = in.readBoolean();
        Map<String, Object> mission = new HashMap<>();
        if (hasMission) {
            // 미션 데이터를 스트림에서 읽음
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
            // 이모티콘 이름을 스트림에서 읽음
            emoticons.add(in.readUTF());
        }

        // DB 저장 시작
        writer.replaceDashboard(nickname, rows);
        
        if (hasGoal) {
            writer.upsertGoal(nickname, todayResult, goalResult); // 목표 저장/업데이트
        }
        
        if (hasMission) {
            writer.upsertMission(nickname, mission); // 미션 저장/업데이트
        }
        
        if (!emoticons.isEmpty()) {
            writer.insertEmoticon(nickname, emoticons); // 이모티콘 저장
        }

        System.out.println("[SERVER] SAVE_ALL 처리 완료: " + nickname);
    }

    // UPDATE_MISSION: 미션 데이터만 업데이트
    private void handleUpdateMission(DataInputStream in) throws IOException {
        String nickname = in.readUTF();

        Map<String, Object> mission = new HashMap<String, Object>();
        mission.put("MISSION1_NAME", in.readUTF());
        mission.put("MISSION1_SUCCESS", in.readInt());

        mission.put("MISSION2_NAME", in.readUTF());
        mission.put("MISSION2_SUCCESS", in.readInt());

        mission.put("MISSION3_NAME", in.readUTF());
        mission.put("MISSION3_SUCCESS", in.readInt());

        writer.upsertMission(nickname, mission); // 미션 데이터 DB 저장

        System.out.println("[SERVER] UPDATE_MISSION 저장 완료 (" + nickname + ")");
    }

    // UPDATE_GOAL: 목표 데이터만 업데이트
    private void handleUpdateGoal(DataInputStream in) throws IOException {
        String nickname = in.readUTF();
        String todayResult = in.readUTF();
        String goalResult = in.readUTF();

        writer.upsertGoal(nickname, todayResult, goalResult); // 목표 데이터 DB 저장

        System.out.println("[SERVER] UPDATE_GOAL 저장 완료 (" + nickname + ") : " + goalResult);
    }

    // UNLOCK_EMOTICON: 이모티콘 해금 정보 저장
    private void handleUnlockEmoticon(DataInputStream in) throws IOException {
        String nickname = in.readUTF();
        String name = in.readUTF();

        writer.insertSingleEmoticon(nickname, name); // 단일 이모티콘 해금 정보 DB 저장

        System.out.println("[SERVER] 이모티콘 해금: " + name);
    }
}