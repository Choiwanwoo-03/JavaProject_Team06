package CommunicateServer;

import Socket.ClientSocket;

import java.io.DataInputStream;
import java.util.*;

public class GiveInformation {

    private final ClientSocket client;

    // 데이터를 GUI 또는 상위 로직으로 전달하기 위한 리스너
    public interface SyncListener {
        void onSyncAll(
                List<Map<String, Object>> dashboard,
                Map<String, Object> goal,
                Map<String, Object> mission,
                List<String> emoticons
        );
    }

    private SyncListener listener;

    public void setSyncListener(SyncListener listener) {
        this.listener = listener;
    }

    public GiveInformation(ClientSocket client) {
        this.client = client;
    }

    // 서버 메시지를 읽는 루프에서 이 메서드를 호출해야 함
    public void handleSyncAll() {
        try {
            DataInputStream in = client.getIn();

            // Dashboard
            int rows = in.readInt();
            List<Map<String, Object>> dashboard = new ArrayList<>();

            for (int i = 0; i < rows; i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("DATE", in.readUTF());
                row.put("TIME", in.readUTF());
                row.put("TYPE", in.readUTF());
                row.put("RESULT", in.readDouble());
                row.put("COUNT", in.readDouble());
                row.put("UNIT", in.readUTF());
                dashboard.add(row);
            }

            // Goal
            Map<String, Object> goal = null;
            boolean hasGoal = in.readBoolean();
            if (hasGoal) {
                goal = new HashMap<>();
                goal.put("TODAY_RESULT", in.readUTF());
                goal.put("GOAL_RESULT", in.readUTF());
            }

            // Mission
            Map<String, Object> mission = null;
            boolean hasMission = in.readBoolean();
            if (hasMission) {
                mission = new HashMap<>();
                mission.put("TODAY_MISSION", in.readUTF());
                mission.put("SUCCESS_MISSION", in.readInt());
            }

            // Emoticons
            int emoCnt = in.readInt();
            List<String> emoticons = new ArrayList<>();
            for (int i = 0; i < emoCnt; i++) {
                emoticons.add(in.readUTF());
            }

            // 전달
            if (listener != null) {
                listener.onSyncAll(dashboard, goal, mission, emoticons);
            }

        } catch (Exception e) {
            System.out.println("[CLIENT] SYNC_ALL 수신 오류: " + e.getMessage());
        }
    }
}
