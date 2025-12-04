package database;

import java.sql.*;
import java.util.*;

public class DataReader {

    // TODO: 실제 DB 정보로 변경해야 함
    private static final String URL = "jdbc:mysql://localhost:3306/ecoactiontracker?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "vkdnj3028@";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** 
     * 로그인 확인 
     * 입력한 ID, PWD가 USER_TABLE에 존재하면 닉네임 반환
     * 실패하면 null 반환
     */
    public String login(String userId, String userPwd) {
        String sql = "SELECT USER_NICKNAME FROM USER_TABLE WHERE USER_ID = ? AND USER_PWD = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, userPwd);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("USER_NICKNAME");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // 로그인 실패
    }

    /** 
     * DASHBOARD_TABLE: 특정 유저의 전체 기록 읽기 
     */
    public List<Map<String, Object>> readDashboard(String nickname) {
        String sql = "SELECT * FROM DASHBOARD_TABLE WHERE USER_NICKNAME = ? ORDER BY DATE, TIME";

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                row.put("DATE", rs.getDate("DATE").toString());
                row.put("TIME", rs.getTime("TIME").toString());
                row.put("TYPE", rs.getString("TYPE"));
                row.put("RESULT", rs.getDouble("RESULT"));
                row.put("COUNT", rs.getDouble("COUNT"));
                row.put("UNIT", rs.getString("UNIT"));

                result.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /** 
     * GOAL_TABLE: 해당 날짜의 목표/오늘결과 읽기
     */
    public Map<String, Object> readGoal(String nickname, String date) {
        String sql = "SELECT TODAY_RESULT, GOAL_RESULT FROM GOAL_TABLE WHERE USER_NICKNAME = ? AND DATE = ?";
        Map<String, Object> map = new HashMap<>();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            pstmt.setString(2, date);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                map.put("TODAY_RESULT", rs.getString("TODAY_RESULT"));
                map.put("GOAL_RESULT", rs.getString("GOAL_RESULT"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    /** 
     * MISSION_TABLE: 해당 날짜의 미션 정보 읽기
     */
    public Map<String, Object> readMission(String nickname, String date) {
        String sql = "SELECT TODAY_MISSION, SUCCESS_MISSION FROM MISSION_TABLE WHERE USER_NICKNAME = ? AND DATE = ?";
        Map<String, Object> map = new HashMap<>();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            pstmt.setString(2, date);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                map.put("TODAY_MISSION", rs.getString("TODAY_MISSION"));
                map.put("SUCCESS_MISSION", rs.getInt("SUCCESS_MISSION"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    /** 
     * EMOTICON_TABLE: 해금된 이모티콘 목록 읽기
     */
    public List<String> readEmoticons(String nickname) {
        String sql = "SELECT RELEASED_EMOTICON FROM EMOTICON_TABLE WHERE USER_NICKNAME = ?";

        List<String> list = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(rs.getString("RELEASED_EMOTICON"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
