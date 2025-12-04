package database;

import java.sql.*;
import java.util.*;

public class DataWriter {

    private static final String URL =
            "jdbc:mysql://localhost:3306/ecoactiontracker"
            + "?serverTimezone=UTC"
            + "&useSSL=false"
            + "&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD = "vkdnj3028@";

    // ★ 공통 DB 커넥션 — 이제 connect() 하나만 사용
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ★ getConnection() 삭제 (또는 connect() 호출하도록 변경)
    // 기존 getConnection()은 DB 이름/비번이 달라서 문제의 원인이었음

    /** 회원가입 INSERT */
    public boolean registerUser(String userId, String userPwd, String nickname) {
        String sql = "INSERT INTO USER_TABLE (USER_ID, USER_PWD, USER_NICKNAME) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userPwd);
            pstmt.setString(3, nickname);
            pstmt.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** DASHBOARD 전체 교체 저장 */
    public boolean replaceDashboard(String nickname, List<Map<String, Object>> rows) {

        String deleteSql = "DELETE FROM dashboard_table WHERE USER_NICKNAME = ?";
        String insertSql =
                "INSERT INTO dashboard_table (USER_NICKNAME, DATE, TIME, TYPE, RESULT, COUNT, UNIT)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement psDelete = conn.prepareStatement(deleteSql);
             PreparedStatement psInsert = conn.prepareStatement(insertSql)) {

            psDelete.setString(1, nickname);
            psDelete.executeUpdate();

            for (Map<String, Object> r : rows) {
                psInsert.setString(1, nickname);
                psInsert.setString(2, (String) r.get("DATE"));
                psInsert.setString(3, (String) r.get("TIME"));
                psInsert.setString(4, (String) r.get("TYPE"));
                psInsert.setDouble(5, (Double) r.get("RESULT"));
                psInsert.setDouble(6, (Double) r.get("COUNT"));
                psInsert.setString(7, (String) r.get("UNIT"));
                psInsert.addBatch();
            }

            psInsert.executeBatch();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** GOAL UPSERT */
    public void upsertGoal(String nickname, String date, String todayResult, String goalResult) {
        String sql =
                "INSERT INTO GOAL_TABLE (USER_NICKNAME, DATE, TODAY_RESULT, GOAL_RESULT) "
                        + "VALUES (?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE TODAY_RESULT = ?, GOAL_RESULT = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            pstmt.setString(2, date);
            pstmt.setString(3, todayResult);
            pstmt.setString(4, goalResult);
            pstmt.setString(5, todayResult);
            pstmt.setString(6, goalResult);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** MISSION UPSERT */
    public void upsertMission(String nickname, String date, String todayMission, int successMission) {
        String sql =
                "INSERT INTO MISSION_TABLE (USER_NICKNAME, DATE, TODAY_MISSION, SUCCESS_MISSION) "
                        + "VALUES (?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE TODAY_MISSION = ?, SUCCESS_MISSION = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            pstmt.setString(2, date);
            pstmt.setString(3, todayMission);
            pstmt.setInt(4, successMission);
            pstmt.setString(5, todayMission);
            pstmt.setInt(6, successMission);

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** EMOTICON INSERT */
    public void insertEmoticon(String nickname, String emoticonName) {
        String sql =
                "INSERT IGNORE INTO EMOTICON_TABLE (USER_NICKNAME, RELEASED_EMOTICON) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            pstmt.setString(2, emoticonName);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** 
     * 오늘 미션을 완전히 덮어쓰기 위해
     * 기존 데이터를 DELETE 후 INSERT
     */
    public void replaceMission(String nickname, String date, String todayMission, int successMission) {

        String deleteSql = "DELETE FROM MISSION_TABLE WHERE USER_NICKNAME = ? AND DATE = ?";
        String insertSql = 
            "INSERT INTO MISSION_TABLE (USER_NICKNAME, DATE, TODAY_MISSION, SUCCESS_MISSION) " +
            "VALUES (?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement psDelete = conn.prepareStatement(deleteSql);
             PreparedStatement psInsert = conn.prepareStatement(insertSql)) {

            // DELETE
            psDelete.setString(1, nickname);
            psDelete.setString(2, date);
            psDelete.executeUpdate();

            // INSERT
            psInsert.setString(1, nickname);
            psInsert.setString(2, date);
            psInsert.setString(3, todayMission);
            psInsert.setInt(4, successMission);

            psInsert.executeUpdate();

            System.out.println("[DB] MISSION_TABLE replaced for " + nickname + " / " + date);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
