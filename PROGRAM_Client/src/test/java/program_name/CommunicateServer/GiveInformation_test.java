package CommunicateServer;

import Socket.ClientSocket;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiveInformation {

    private final ClientSocket client;
    private final DataInputStream in;

    // SYNC_ALL 결과를 GUI로 전달하는 리스너
    public interface SyncListener {
        void onSync(
                List<Map<String, Object>> dashboard,
                Map<String, Object> goal,
                Map<String, Object> mission,
                List<String> emoticons
        );
    }

    private SyncListener syncListener;

    // 리스너 등록 전 캐시
    private List<Map<String, Object>> cachedDashboard;
    private Map<String, Object> cachedGoal;
    private Map<String, Object> cachedMission;
    private List<String> cachedEmoticons;
    private boolean hasCachedSync = false;

    public GiveInformation(ClientSocket client) {
        this.client = client;
        this.in = client.getIn();
    }

    public void setSyncListener(SyncListener listener) {
        this.syncListener = listener;

        if (listener != null && hasCachedSync) {
            listener.onSync(cachedDashboard, cachedGoal, cachedMission, cachedEmoticons);
            hasCachedSync = false;
        }
    }

    // ================================
    // SYNC_ALL 수신 처리
    // ================================
    public void handleSyncAll() {
        try {
            // ---------- 1. 대시보드 ----------
            int rowCount = in.readInt();
            List<Map<String, Object>> dashboard = new ArrayList<>();

            for (int i = 0; i < rowCount; i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("DATE", in.readUTF());
                row.put("TIME", in.readUTF());
                row.put("TYPE", in.readUTF());
                row.put("RESULT", in.readDouble());
                row.put("COUNT", in.readDouble());
                row.put("UNIT", in.readUTF());
                dashboard.add(row);
            }

            // ---------- 2. 목표 ----------
            boolean hasGoal = in.readBoolean();
            Map<String, Object> goal = null;
            if (hasGoal) {
                goal = new HashMap<>();
                goal.put("TODAY_RESULT", in.readUTF());
                goal.put("GOAL_RESULT", in.readUTF());
            }

            // ---------- 3. 미션 (MISSION1/2/3) ----------
            boolean hasMission = in.readBoolean();
            Map<String, Object> mission = null;

            if (hasMission) {
                String m1 = in.readUTF();
                int s1 = in.readInt();

                String m2 = in.readUTF();
                int s2 = in.readInt();

                String m3 = in.readUTF();
                int s3 = in.readInt();

                mission = new HashMap<>();
                mission.put("MISSION1_NAME", m1);
                mission.put("MISSION1_SUCCESS", s1);
                mission.put("MISSION2_NAME", m2);
                mission.put("MISSION2_SUCCESS", s2);
                mission.put("MISSION3_NAME", m3);
                mission.put("MISSION3_SUCCESS", s3);
            }

            // ---------- 4. 이모티콘 ----------
            int emoCount = in.readInt();
            List<String> emoticons = new ArrayList<>();
            for (int i = 0; i < emoCount; i++) {
                emoticons.add(in.readUTF());
            }

            // ---------- 최종 전달 ----------
            if (syncListener != null) {
                syncListener.onSync(dashboard, goal, mission, emoticons);
            } else {
                cachedDashboard = dashboard;
                cachedGoal = goal;
                cachedMission = mission;
                cachedEmoticons = emoticons;
                hasCachedSync = true;
            }

            System.out.println("[CLIENT] SYNC_ALL 처리 완료");

        } catch (Exception e) {
            System.out.println("[CLIENT] SYNC_ALL 처리 실패: " + e.getMessage());
        }
    }
}
