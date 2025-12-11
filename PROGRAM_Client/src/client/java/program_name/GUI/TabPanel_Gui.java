package GUI;

import Socket.ClientSocket;
import CommunicateServer.GetInformation;
import Emoticon.EmoticonManager;
import Mission.MissionManager;
import Emoticon.OpeningEmoticon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabPanel_Gui extends JFrame {

    private final String nickname;
    private final ClientSocket client;
    private final GetInformation sender;

    private final DashBoard_Gui dashboardPanel;
    private final AddAction_Gui addActionPanel;
    private final Guide_Gui guidePanel;
    private final EmoticonManager emoticonManager;
    private final EmoticonBook_Gui emoticonBookPanel;
    private final MissionManager missionManager;
    private AchieveGoal_Gui goalPanel = null;

    public TabPanel_Gui(String nickname, ClientSocket client) {
        this.nickname = nickname;
        this.client = client;
        this.sender = client.getGetter();

        setTitle("탄소 발자국 관리 - " + nickname);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // 1. 매니저 초기화
        this.emoticonManager = new EmoticonManager();
        this.missionManager = new MissionManager(
                MissionManager.defaultMissions(),
                Paths.get("mission_" + nickname + ".txt")
        );

        // 2. 패널 생성
        dashboardPanel = new DashBoard_Gui();

        goalPanel = new AchieveGoal_Gui(
                dashboardPanel::calculateTotalEmission,
                () -> missionManager.getDailyGoalEmission(),
                missionManager,
                (newGoal) -> {
                    missionManager.setDailyGoalEmission(newGoal);
                    if (goalPanel != null) goalPanel.refreshUI();
                },
                this::checkAllMissionsCleared // 미션 완료 체크 콜백
        );

        addActionPanel = new AddAction_Gui();
        addActionPanel.setLogActionListener(logData -> {
            String date = (String) logData.get("date");
            String type = (String) logData.get("type");
            double result = (Double) logData.get("result");
            double count = (Double) logData.get("count");
            String unit = (String) logData.get("unit");

            dashboardPanel.addLog(date, type, result, count, unit);
            if (goalPanel != null) goalPanel.refreshUI();
            
            checkAllMissionsCleared(); // 행동 추가 시에도 체크
        });

        guidePanel = new Guide_Gui();
        emoticonBookPanel = new EmoticonBook_Gui(emoticonManager);

        // 3. 탭 추가
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab("대시보드", dashboardPanel);
        tabPane.addTab("행동 추가", addActionPanel);
        tabPane.addTab("목표 & 미션", goalPanel);
        tabPane.addTab("이모티콘 도감", emoticonBookPanel);
        tabPane.addTab("가이드", guidePanel);

        add(tabPane, BorderLayout.CENTER);

        // 4. 서버 데이터 동기화
        client.setSyncListener((dashboard, goalMap, missionMap, emos) -> {
            SwingUtilities.invokeLater(() -> {
                // (1) 대시보드 로드
                dashboardPanel.loadFromServer(dashboard);

                // (2) 목표 로드
                if (goalMap != null) {
                    try {
                        double gVal = Double.parseDouble(String.valueOf(goalMap.get("GOAL_RESULT")));
                        missionManager.setDailyGoalEmission(gVal);
                    } catch (Exception e) {}
                }

                // (3) 미션 로드
                if (missionMap != null) {
                    missionManager.setMission1Name(String.valueOf(missionMap.get("MISSION1_NAME")));
                    missionManager.setMission1Success(Integer.parseInt(String.valueOf(missionMap.get("MISSION1_SUCCESS"))));
                    
                    missionManager.setMission2Name(String.valueOf(missionMap.get("MISSION2_NAME")));
                    missionManager.setMission2Success(Integer.parseInt(String.valueOf(missionMap.get("MISSION2_SUCCESS"))));
                    
                    missionManager.setMission3Name(String.valueOf(missionMap.get("MISSION3_NAME")));
                    missionManager.setMission3Success(Integer.parseInt(String.valueOf(missionMap.get("MISSION3_SUCCESS"))));
                }

                // (4) 이모티콘 로드
                if (emos != null) {
                    emoticonManager.unlockAll(emos);
                    emoticonBookPanel.refreshGrid();
                }

                // (5) UI 갱신 및 ★보상 체크★
                if (goalPanel != null) goalPanel.refreshUI();
                
                // [수정] 데이터 로드 완료 후 미션 달성 여부를 한 번 더 확인 (서버 데이터 기준)
                checkAllMissionsCleared();
            });
        });

        // 5. 초기 실행
        SwingUtilities.invokeLater(() -> {
            sender.sendRequestSync(nickname);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
        
        setVisible(true);
    }

    // 미션 완료 체크 및 보상 지급
    private void checkAllMissionsCleared() {
        // 1, 2, 3번 미션이 모두 1(성공)인지 확인
        if (missionManager.getMission1Success() == 1 &&
            missionManager.getMission2Success() == 1 &&
            missionManager.getMission3Success() == 1) {
            
        	String rewardName = emoticonManager.getRandomLockedEmoticon();
            
            // 아직 해금되지 않았다면 해금 처리
            if (!emoticonManager.isUnlocked(rewardName)) {
                emoticonManager.unlock(rewardName);
                sender.sendUnlockEmoticon(nickname, rewardName); // 서버 DB에 저장
                emoticonBookPanel.refreshGrid(); // 도감 UI 갱신
                
                // 팝업 표시
                OpeningEmoticon.showReward(this, rewardName);
            }
        }
    }

    private void onExit() {
        try {
            List<Map<String, Object>> rows = dashboardPanel.exportForSave();
            Map<String, Object> goal = new HashMap<>();
            goal.put("TODAY_RESULT", String.valueOf(dashboardPanel.calculateTotalEmission()));
            goal.put("GOAL_RESULT", String.valueOf(missionManager.getDailyGoalEmission()));
            Map<String, Object> mission = missionManager.exportMissionToMap();
            List<String> emoticons = emoticonManager.getUnlockedNamesList();

            sender.sendSaveAll(nickname, rows, goal, mission, emoticons);
            System.out.println("[CLIENT] 저장 요청 전송 완료");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            client.close();
            dispose();
            System.exit(0);
        }
    }
}