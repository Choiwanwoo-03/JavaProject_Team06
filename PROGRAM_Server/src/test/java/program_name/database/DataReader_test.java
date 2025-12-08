package database;

import java.sql.*;
import java.util.*;

public class DataReader {

    private Connection connect() throws SQLException {
    	 String url = "jdbc:mysql://localhost:3306/ecoactiontracker?serverTimezone=Asia/Seoul";
         String user = "root";
         String pw   = "vkdnj3028@";
        return DriverManager.getConnection(url, user, pw);
    }

    // ============================================================
    // 1) 로그인 (ID + PW → 닉네임)
    // ============================================================
    public String login(String id, String pw) {
        String sql = "SELECT USER_NICKNAME FROM USER_TABLE WHERE USER_ID = ? AND USER_PWD = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, pw);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("USER_NICKNAME");
            }

        } catch (SQLException e) {
            System.out.println("[DB][login] 오류: " + e.getMessage());
        }
        return null;
    }

    // ============================================================
    // 2) Dashboard 조회
    // ============================================================
    public List<Map<String, Object>> getDashboard(String nickname) {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
                "SELECT DATE, TIME, TYPE, COUNT, UNIT, RESULT "
              + "FROM DASHBOARD_TABLE WHERE USER_NICKNAME = ? ORDER BY DATE, TIME";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nickname);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("DATE", rs.getString("DATE"));
                row.put("TIME", rs.getString("TIME"));
                row.put("TYPE", rs.getString("TYPE"));
                row.put("COUNT", rs.getDouble("COUNT"));
                row.put("UNIT", rs.getString("UNIT"));
                row.put("RESULT", rs.getDouble("RESULT"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.out.println("[DB][getDashboard] 오류: " + e.getMessage());
        }

        return list;
    }

    // ============================================================
    // 3) Goal 조회
    // ============================================================
    public Map<String, Object> getGoal(String nickname) {

        String sql =
                "SELECT TODAY_RESULT, GOAL_RESULT "
              + "FROM GOAL_TABLE WHERE USER_NICKNAME = ? AND DATE = CURDATE()";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nickname);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("TODAY_RESULT", rs.getString("TODAY_RESULT"));
                map.put("GOAL_RESULT",  rs.getString("GOAL_RESULT"));
                return map;
            }

        } catch (SQLException e) {
            System.out.println("[DB][getGoal] 오류: " + e.getMessage());
        }

        return null;
    }

    // ============================================================
    // 4) Mission 조회 (MISSION1/2/3 + SUCCESS)
    // ============================================================
    public Map<String, Object> getMission(String nickname) {

        String sql =
                "SELECT MISSION1_NAME, MISSION1_SUCCESS, "
              + "       MISSION2_NAME, MISSION2_SUCCESS, "
              + "       MISSION3_NAME, MISSION3_SUCCESS "
              + "FROM MISSION_TABLE WHERE USER_NICKNAME = ? AND DATE = CURDATE()";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nickname);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> map = new HashMap<>();

                map.put("MISSION1_NAME",    rs.getString("MISSION1_NAME"));
                map.put("MISSION1_SUCCESS", rs.getInt("MISSION1_SUCCESS"));

                map.put("MISSION2_NAME",    rs.getString("MISSION2_NAME"));
                map.put("MISSION2_SUCCESS", rs.getInt("MISSION2_SUCCESS"));

                map.put("MISSION3_NAME",    rs.getString("MISSION3_NAME"));
                map.put("MISSION3_SUCCESS", rs.getInt("MISSION3_SUCCESS"));

                return map;
            }

        } catch (SQLException e) {
            System.out.println("[DB][getMission] 오류: " + e.getMessage());
        }

        return null;
    }

    // ============================================================
    // 5) 이모티콘 조회
    // ============================================================
    public List<String> getEmoticons(String nickname) {
        List<String> list = new ArrayList<>();
        // [수정] ORDER BY DATE -> ORDER BY EMOTICON_id (PK 사용)
        String sql = "SELECT RELEASED_EMOTICON FROM EMOTICON_TABLE WHERE USER_NICKNAME = ? ORDER BY EMOTICON_id";
        
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nickname);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("RELEASED_EMOTICON"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
