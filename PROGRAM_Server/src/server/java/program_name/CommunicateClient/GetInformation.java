package CommunicateClient;

import database.DataWriter;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 서버 ← 클라이언트 데이터 수신 담당
 * 수신한 데이터는 DataWriter를 호출하여 DB에 저장한다.
 * (DB 직접 접근은 절대 하지 않음)
 */
public class GetInformation {

    private final DataWriter writer = new DataWriter();

    /**
     * 클라이언트로부터 명령어를 읽어 적절한 처리 함수를 호출한다.
     */
    public void handle(DataInputStream in) throws IOException {

        String cmd = in.readUTF();

        switch (cmd) {
            case "SAVE_ALL" -> handleSaveAll(in);
            case "UPDATE_MISSION" -> handleUpdateMission(in);
            case "UNLOCK_EMOTICON" -> handleUnlockEmoticon(in);
            default -> System.out.println("[SERVER][GetInformation] Unknown Command: " + cmd);
        }
    }

    /**
     * 프로그램 종료 시 전체 데이터 저장
     * - Dashboard
     * - Goal
     * - Mission
     * - Emoticons
     */
    private void handleSaveAll(DataInputStream in) throws IOException {

        String nickname = in.readUTF();

        // --- Dashboard 데이터 수신 ---
        int rowCount = in.readInt();
        List<Map<String, Object>> dashboardRows = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("DATE", in.readUTF());
            row.put("TIME", in.readUTF());
            row.put("TYPE", in.readUTF());
            row.put("RESULT", in.readDouble());
            row.put("COUNT", in.readDouble());
            row.put("UNIT", in.readUTF());
            dashboardRows.add(row);
        }

        // --- Goal 수신 ---
        boolean hasGoal = in.readBoolean();
        String todayResult = null;
        String goalResult = null;
        if (hasGoal) {
            todayResult = in.readUTF();
            goalResult = in.readUTF();
        }

        // --- Mission 수신 ---
        boolean hasMission = in.readBoolean();
        String todayMission = null;
        int successMission = 0;
        if (hasMission) {
            todayMission = in.readUTF();
            successMission = in.readInt();
        }

        // --- Emoticons 수신 ---
        int emoCnt = in.readInt();
        List<String> emoticons = new ArrayList<>();
        for (int i = 0; i < emoCnt; i++) {
            emoticons.add(in.readUTF());
        }

        // ========== DB 저장 시작 ==========

        // Dashboard 갱신
        writer.replaceDashboard(nickname, dashboardRows);

        // Goal 저장
        if (hasGoal && !dashboardRows.isEmpty()) {
            String date = (String) dashboardRows.get(0).get("DATE");
            writer.upsertGoal(nickname, date, todayResult, goalResult);
        }

        // Mission 저장
        if (hasMission && !dashboardRows.isEmpty()) {
            String date = (String) dashboardRows.get(0).get("DATE");
            writer.upsertMission(nickname, date, todayMission, successMission);
        }

        // Emoticon 저장
        for (String emo : emoticons) {
            writer.insertEmoticon(nickname, emo);
        }

        System.out.println("[SERVER] SAVE_ALL Completed: " + nickname);
    }

    /**
     * 미션이 실시간으로 성공하거나 변경될 때 저장
     */
    private void handleUpdateMission(DataInputStream in) throws IOException {

        String nickname = in.readUTF();
        String date = in.readUTF();
        String todayMission = in.readUTF();
        int successMission = in.readInt();

        writer.upsertMission(nickname, date, todayMission, successMission);
    }

    /**
     * 이모티콘이 실시간으로 해금될 때 저장
     */
    private void handleUnlockEmoticon(DataInputStream in) throws IOException {

        String nickname = in.readUTF();
        String emoticonName = in.readUTF();

        writer.insertEmoticon(nickname, emoticonName);
    }
}
