package database;

import java.sql.*;
import java.util.*;

/**
 * DB에 데이터를 저장(INSERT / UPDATE / DELETE)하는 전용 클래스
 * - 회원가입
 * - 대시보드 저장
 * - 목표 저장
 * - 미션 저장
 * - 이모티콘 저장 담당
 */
public class DataWriter {

    /**
     * DB 연결 생성 메소드
     */
    private Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/ecoactiontracker?serverTimezone=Asia/Seoul";
        String user = "root";
        String pw   = "vkdnj3028@";
        return DriverManager.getConnection(url, user, pw);
    }

    // ============================================================
    // 1) 회원가입 (USER_TABLE에 계정 정보 저장)
    // ============================================================
    public boolean registerUser(String id, String pw, String nickname) {
        String sql = "INSERT INTO USER_TABLE (USER_ID, USER_PWD, USER_NICKNAME) VALUES (?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, pw);
            ps.setString(3, nickname);

            // 1건 이상 삽입되면 성공
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("[DB][registerUser] 실패: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    // 2) Dashboard 전체 저장 (기존 데이터 삭제 후 전체 재삽입)
    // ============================================================
    public void replaceDashboard(String nickname, List<Map<String, Object>> rows) {
        String deleteSQL = "DELETE FROM DASHBOARD_TABLE WHERE USER_NICKNAME = ?";
        String insertSQL = "INSERT INTO DASHBOARD_TABLE (USER_NICKNAME, DATE, TIME, TYPE, COUNT, UNIT, RESULT) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);  // 트랜잭션 시작

            // 기존 대시보드 기록 삭제
            try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                ps.setString(1, nickname);
                ps.executeUpdate();
            }

            // 새로운 대시보드 기록 일괄 삽입
            try (PreparedStatement ps2 = conn.prepareStatement(insertSQL)) {
                for (Map<String, Object> row : rows) {
                    ps2.setString(1, nickname);
                    ps2.setString(2, String.valueOf(row.get("DATE")));
                    ps2.setString(3, String.valueOf(row.get("TIME")));
                    ps2.setString(4, String.valueOf(row.get("TYPE")));
                    ps2.setDouble(5, Double.parseDouble(String.valueOf(row.get("COUNT"))));
                    ps2.setString(6, String.valueOf(row.get("UNIT")));
                    ps2.setDouble(7, Double.parseDouble(String.valueOf(row.get("RESULT"))));

                    ps2.addBatch();
                }
                ps2.executeBatch(); // 일괄 실행
            }

            conn.commit(); // 최종 반영
            System.out.println("[DB] Dashboard 저장 완료");

        } catch (SQLException e) {
            System.out.println("[DB][replaceDashboard] 오류: " + e.getMessage());
        }
    }

    // ============================================================
    // 3) Goal 저장 (있으면 UPDATE, 없으면 INSERT)
    // ============================================================
    public void upsertGoal(String nickname, String todayResult, String goalResult) {
        String sql =
                "INSERT INTO GOAL_TABLE (USER_NICKNAME, DATE, TODAY_RESULT, GOAL_RESULT) "
                + "VALUES (?, CURDATE(), ?, ?) "
                + "ON DUPLICATE KEY UPDATE TODAY_RESULT = ?, GOAL_RESULT = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nickname);
            ps.setString(2, todayResult);
            ps.setString(3, goalResult);

            // 중복일 경우 UPDATE 값
            ps.setString(4, todayResult);
            ps.setString(5, goalResult);

            ps.executeUpdate();
            System.out.println("[DB] Goal 저장 완료");

        } catch (SQLException e) {
            System.out.println("[DB][upsertGoal] 오류: " + e.getMessage());
        }
    }

    // ============================================================
    // 4) Mission 저장 (MISSION 1~3 + 성공 여부 UPSERT)
    // ============================================================
    public void upsertMission(String nickname, Map<String, Object> mission) {

        String sql =
                "INSERT INTO MISSION_TABLE (USER_NICKNAME, DATE, "
                + "MISSION1_NAME, MISSION1_SUCCESS, "
                + "MISSION2_NAME, MISSION2_SUCCESS, "
                + "MISSION3_NAME, MISSION3_SUCCESS) "
                + "VALUES (?, CURDATE(), ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "MISSION1_NAME = ?, MISSION1_SUCCESS = ?, "
                + "MISSION2_NAME = ?, MISSION2_SUCCESS = ?, "
                + "MISSION3_NAME = ?, MISSION3_SUCCESS = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 미션 데이터 추출
            String m1 = String.valueOf(mission.get("MISSION1_NAME"));
            int s1 = Integer.parseInt(String.valueOf(mission.get("MISSION1_SUCCESS")));

            String m2 = String.valueOf(mission.get("MISSION2_NAME"));
            int s2 = Integer.parseInt(String.valueOf(mission.get("MISSION2_SUCCESS")));

            String m3 = String.valueOf(mission.get("MISSION3_NAME"));
            int s3 = Integer.parseInt(String.valueOf(mission.get("MISSION3_SUCCESS")));

            // INSERT 값
            ps.setString(1, nickname);
            ps.setString(2, m1);
            ps.setInt(3, s1);
            ps.setString(4, m2);
            ps.setInt(5, s2);
            ps.setString(6, m3);
            ps.setInt(7, s3);

            // UPDATE 값
            ps.setString(8,  m1);
            ps.setInt(9,     s1);
            ps.setString(10, m2);
            ps.setInt(11,    s2);
            ps.setString(12, m3);
            ps.setInt(13,    s3);

            ps.executeUpdate();
            System.out.println("[DB] Mission 저장 완료");

        } catch (SQLException e) {
            System.out.println("[DB][upsertMission] 오류: " + e.getMessage());
        }
    }

    // ============================================================
    // 5) 여러 이모티콘 저장 (SAVE_ALL 시 사용)
    // ============================================================
    public void insertEmoticon(String nickname, List<String> names) {

        if (names == null || names.isEmpty()) return;

        String sql =
                "INSERT IGNORE INTO EMOTICON_TABLE (USER_NICKNAME, EMOTICON_NAME, DATE) "
                + "VALUES (?, ?, CURDATE())";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 보유한 이모티콘 전체 일괄 저장
            for (String name : names) {
                ps.setString(1, nickname);
                ps.setString(2, name);
                ps.addBatch();
            }

            ps.executeBatch();
            System.out.println("[DB] 이모티콘 목록 저장 완료");

        } catch (SQLException e) {
            System.out.println("[DB][insertEmoticon] 오류: " + e.getMessage());
        }
    }

    // ============================================================
    // 6) 단일 이모티콘 저장 (보상 획득 시 1개 추가)
    // ============================================================
    public void insertSingleEmoticon(String nickname, String name) {
        String sql =
                "INSERT IGNORE INTO EMOTICON_TABLE (USER_NICKNAME, EMOTICON_NAME, DATE) "
                + "VALUES (?, ?, CURDATE())";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nickname);
            ps.setString(2, name);
            ps.executeUpdate();

            System.out.println("[DB] 이모티콘 1개 저장 완료");

        } catch (SQLException e) {
            System.out.println("[DB][insertSingleEmoticon] 오류: " + e.getMessage());
        }
    }
}
