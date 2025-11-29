package database;

import java.sql.*;
import java.util.*;

public class DataWriter {

	private static final String URL = "jdbc:mysql://localhost:3306/your_db_name?serverTimezone=UTC";
    private static final String USER = "your_username";
    private static final String PASSWORD = "your_password";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

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
    public void replaceDashboard(String nickname, List<Map<String, Object>> rows) {
        String deleteSql = "DELETE FROM DASHBOARD_TABLE WHERE USER_NICKNAME = ?";
        String insertSql = "INSERT INTO DASHBOARD_TABLE (USER_NICKNAME, DATE, TIME, TYPE, RESULT, COUNT, UNIT) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect()) {

            conn.setAutoCommit(false);

            // 1) 기존 데이터 삭제
            try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                del.setString(1, nickname);
                del.executeUpdate();
            }

            // 2) 새 데이터가 없으면 여기서 종료
            if (rows == null || rows.isEmpty()) {
                conn.commit();
                return;
            }

            // 3) 새 데이터 삽입
            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                for (Map<String, Object> row : rows) {
                    ins.setString(1, nickname);
                    ins.setString(2, (String) row.get("DATE"));
                    ins.setString(3, (String) row.get("TIME"));
                    ins.setString(4, (String) row.get("TYPE"));
                    ins.setDouble(5, (Double) row.get("RESULT"));
                    ins.setDouble(6, (Double) row.get("COUNT"));
                    ins.setString(7, (String) row.get("UNIT"));
                    ins.addBatch();
                }
                ins.executeBatch();
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
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

        String sql = "INSERT IGNORE INTO EMOTICON_TABLE (USER_NICKNAME, RELEASED_EMOTICON) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            pstmt.setString(2, emoticonName);

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
