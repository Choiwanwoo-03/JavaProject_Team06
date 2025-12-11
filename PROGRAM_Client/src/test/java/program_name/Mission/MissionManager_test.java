package Mission;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

public class MissionManager_test {

    private final Path todayMissionFile;
    private final Path goalFile; 
    
    private final List<ClearedMission.Mission> baseMissions; // 기본 미션 목록

    private ClearedMission.Mission todayMission;
    private int todaySuccess = 0;

    private String mission2Name = "";
    private int mission2Success = 0;

    private String mission3Name = "";
    private int mission3Success = 0;

    private double dailyGoalEmission = 10.0;

    public MissionManager_test(List<ClearedMission.Mission> defaultMissions, Path missionFile) {
        this.baseMissions = defaultMissions;
        this.todayMissionFile = missionFile;
        this.goalFile = missionFile.resolveSibling("dailyGoal.txt");

        loadTodayMission();
        loadGoal();
        
        // [수정 1] 로드 직후, 이름이 비어있다면 기본값으로 강제 초기화 (핵심)
        ensureMissionNames();
    }

    // 이름이 없으면 기본값 채워넣기
    private void ensureMissionNames() {
        if (todayMission == null || todayMission.name.isEmpty()) {
            setMission1Name(baseMissions.get(0).name);
        }
        if (mission2Name == null || mission2Name.isEmpty()) {
            setMission2Name(baseMissions.get(1).name);
        }
        if (mission3Name == null || mission3Name.isEmpty()) {
            setMission3Name(baseMissions.get(2).name);
        }
    }

    // -----------------------------
    // 이름 세터 (수정됨: 빈 값이면 기본값 사용)
    // -----------------------------
    public void setMission1Name(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = baseMissions.get(0).name;
        }
        this.todayMission = new ClearedMission.Mission(name);
        saveTodayMission();
    }

    public void setMission2Name(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = baseMissions.get(1).name;
        }
        this.mission2Name = name;
    }

    public void setMission3Name(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = baseMissions.get(2).name;
        }
        this.mission3Name = name;
    }

    // -----------------------------
    // 성공 여부 세터
    // -----------------------------
    public void setMission1Success(int success) {
        this.todaySuccess = success;
        saveTodayMission();
    }
    public void setMission2Success(int success) { this.mission2Success = success; }
    public void setMission3Success(int success) { this.mission3Success = success; }

    // -----------------------------
    // 게터
    // -----------------------------
    public String getMission1Name() { 
        return (todayMission != null && !todayMission.name.isEmpty()) 
                ? todayMission.name 
                : baseMissions.get(0).name; 
    }
    public String getMission2Name() { 
        return (!mission2Name.isEmpty()) ? mission2Name : baseMissions.get(1).name;
    }
    public String getMission3Name() { 
        return (!mission3Name.isEmpty()) ? mission3Name : baseMissions.get(2).name;
    }
    
    public int getMission1Success() { return todaySuccess; }
    public int getMission2Success() { return mission2Success; }
    public int getMission3Success() { return mission3Success; }

    // -----------------------------
    // 목표 관련
    // -----------------------------
    public double getDailyGoalEmission() { return dailyGoalEmission; }
    public void setDailyGoalEmission(double goal) {
        this.dailyGoalEmission = goal;
        saveGoal();
    }

    private void loadGoal() {
        if (goalFile == null || !Files.exists(goalFile)) return;
        try {
            List<String> lines = Files.readAllLines(goalFile, StandardCharsets.UTF_8);
            if (!lines.isEmpty()) {
                this.dailyGoalEmission = Double.parseDouble(lines.get(0));
            }
        } catch (Exception ignored) {}
    }

    private void saveGoal() {
        if (goalFile == null) return;
        try {
            Files.write(goalFile, Collections.singletonList(String.valueOf(dailyGoalEmission)), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
    }

    // -----------------------------
    // 파일 로드
    // -----------------------------
    private void loadTodayMission() {
        if (todayMissionFile == null || !Files.exists(todayMissionFile)) {
            ensureMissionNames(); // 파일 없으면 기본값
            return;
        }
        try {
            List<String> lines = Files.readAllLines(todayMissionFile, StandardCharsets.UTF_8);
            if (lines.size() >= 2) {
                this.todayMission = new ClearedMission.Mission(lines.get(0));
                this.todaySuccess = Integer.parseInt(lines.get(1));
            }
        } catch (Exception e) {
            ensureMissionNames();
        }
    }

    public void saveTodayMission() {
        if (todayMissionFile == null) return;
        try {
            String name = (todayMission != null) ? todayMission.name : baseMissions.get(0).name;
            List<String> lines = Arrays.asList(name, String.valueOf(todaySuccess));
            Files.write(todayMissionFile, lines, StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
    }

    public Map<String, Object> exportMissionToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("MISSION1_NAME", getMission1Name());
        map.put("MISSION1_SUCCESS", todaySuccess);
        map.put("MISSION2_NAME", getMission2Name());
        map.put("MISSION2_SUCCESS", mission2Success);
        map.put("MISSION3_NAME", getMission3Name());
        map.put("MISSION3_SUCCESS", mission3Success);
        return map;
    }

    public static List<ClearedMission.Mission> defaultMissions() {
        List<ClearedMission.Mission> list = new ArrayList<>();
        list.add(new ClearedMission.Mission("오늘 목표 이하로 배출량 유지하기"));
        list.add(new ClearedMission.Mission("교통탄소 3kg 이하로 사용하기")); // 미션2 기본값
        list.add(new ClearedMission.Mission("음식물 쓰레기 300g 이하로 줄이기")); // 미션3 기본값
        return list;
    }
}