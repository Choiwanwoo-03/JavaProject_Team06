package database;

import java.sql.*;
import java.util.*;

public class DataWriter {

    private Connection connect() throws SQLException {
        // [본인 환경에 맞게 수정 필요]
        String url = "jdbc:mysql://localhost:3306/ecoactiontracker?serverTimezone=Asia/Seoul";
        String user = "root";
        String pw   = "vkdnj3028@"; 
        return DriverManager.getConnection(url, user, pw);
    }

    // 1. 회원가입 (기존 유지)
    public boolean registerUser(String id, String pw, String nickname) {
        String sql = "INSERT INTO USER_TABLE (USER_ID, USER_PWD, USER_NICKNAME) VALUES (?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, pw);
            ps.setString(3, nickname);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 2. 대시보드 저장 (기존 유지)
    public void replaceDashboard(String nickname, List<Map<String, Object>> rows) {
        String deleteSQL = "DELETE FROM DASHBOARD_TABLE WHERE USER_NICKNAME = ?";
        String insertSQL = "INSERT INTO DASHBOARD_TABLE (USER_NICKNAME, DATE, TIME, TYPE, RESULT, COUNT, UNIT) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                ps.setString(1, nickname);
                ps.executeUpdate();
            }
            if (rows != null && !rows.isEmpty()) {
                try (PreparedStatement ps2 = conn.prepareStatement(insertSQL)) {
                    for (Map<String, Object> row : rows) {
                        ps2.setString(1, nickname);
                        ps2.setString(2, String.valueOf(row.get("DATE")));
                        ps2.setString(3, String.valueOf(row.get("TIME")));
                        ps2.setString(4, String.valueOf(row.get("TYPE")));
                        ps2.setDouble(5, Double.parseDouble(String.valueOf(row.get("RESULT"))));
                        ps2.setDouble(6, Double.parseDouble(String.valueOf(row.get("COUNT"))));
                        ps2.setString(7, String.valueOf(row.get("UNIT")));
                        ps2.addBatch();
                    }
                    ps2.executeBatch();
                }
            }
            conn.commit();
            System.out.println("[DB] Dashboard 저장 완료");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 3. 목표 저장 (기존 유지)
    public void upsertGoal(String nickname, String todayResult, String goalResult) {
        String sql = "INSERT INTO GOAL_TABLE (USER_NICKNAME, DATE, TODAY_RESULT, GOAL_RESULT) VALUES (?, CURDATE(), ?, ?) " +
                     "ON DUPLICATE KEY UPDATE TODAY_RESULT=VALUES(TODAY_RESULT), GOAL_RESULT=VALUES(GOAL_RESULT)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nickname);
            ps.setString(2, todayResult);
            ps.setString(3, goalResult);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 4. [수정] 미션 3개 저장 (DB 테이블 변경 반영)
    public void upsertMission(String nickname, Map<String, Object> mission) {
        String sql = "INSERT INTO MISSION_TABLE (USER_NICKNAME, DATE, " +
                     "MISSION1_NAME, MISSION1_SUCCESS, " +
                     "MISSION2_NAME, MISSION2_SUCCESS, " +
                     "MISSION3_NAME, MISSION3_SUCCESS) " +
                     "VALUES (?, CURDATE(), ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "MISSION1_NAME=VALUES(MISSION1_NAME), MISSION1_SUCCESS=VALUES(MISSION1_SUCCESS), " +
                     "MISSION2_NAME=VALUES(MISSION2_NAME), MISSION2_SUCCESS=VALUES(MISSION2_SUCCESS), " +
                     "MISSION3_NAME=VALUES(MISSION3_NAME), MISSION3_SUCCESS=VALUES(MISSION3_SUCCESS)";

        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nickname);
            
            // Map에서 값 꺼내기 (null 체크)
            ps.setString(2, String.valueOf(mission.getOrDefault("MISSION1_NAME", "")));
            ps.setInt(3, parseInt(mission.get("MISSION1_SUCCESS")));
            ps.setString(4, String.valueOf(mission.getOrDefault("MISSION2_NAME", "")));
            ps.setInt(5, parseInt(mission.get("MISSION2_SUCCESS")));
            ps.setString(6, String.valueOf(mission.getOrDefault("MISSION3_NAME", "")));
            ps.setInt(7, parseInt(mission.get("MISSION3_SUCCESS")));

            ps.executeUpdate();
            System.out.println("[DB] 미션 저장 완료");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 5. 이모티콘 저장 (기존 유지)
    public void insertEmoticon(String nickname, List<String> emoticons) {
        if(emoticons==null || emoticons.isEmpty()) return;
        String sql = "INSERT IGNORE INTO EMOTICON_TABLE (USER_NICKNAME, RELEASED_EMOTICON) VALUES (?, ?)"; // 컬럼명 DB와 일치시킴
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for(String name : emoticons) {
                ps.setString(1, nickname);
                ps.setString(2, name);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    public void insertSingleEmoticon(String nickname, String name) {
        String sql = "INSERT IGNORE INTO EMOTICON_TABLE (USER_NICKNAME, RELEASED_EMOTICON) VALUES (?, ?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nickname);
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private int parseInt(Object o) {
        try { return Integer.parseInt(String.valueOf(o)); } catch(Exception e) { return 0; }
    }
}