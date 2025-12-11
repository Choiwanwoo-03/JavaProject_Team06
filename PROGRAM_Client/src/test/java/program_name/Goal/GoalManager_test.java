package Goal;

import java.nio.file.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * GoalManager
 * - 사용자의 하루 탄소배출 목표량(kg)을 관리
 * - 파일 저장/로드 (goal.txt)
 * - 서버 SYNC_ALL에서 goal이 오면 해당 값으로 덮어씀
 * - 프로그램 종료(SAVE_ALL) 시 goal/today_result 함께 서버로 전송
 */
public class GoalManager_test {

    private final Path filePath;
    private double goalValue;  // kg

    /**
     * @param filePath    goal.txt 파일 경로
     * @param defaultGoal 값이 없을 때 기본 목표값
     */
    public GoalManager_test(Path filePath, double defaultGoal) {
        this.filePath = filePath;
        this.goalValue = defaultGoal;

        load();
    }

    /**
     * goal.txt에서 목표값을 로드
     */
    public void load() {
        BufferedReader reader = null;
        try {
            if (Files.exists(filePath)) {
                reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
                String text = reader.readLine();
                if (text != null) {
                    text = text.trim();
                    this.goalValue = Double.parseDouble(text);
                    System.out.println("[GOAL] 목표값 로드: " + goalValue);
                }
            } else {
                save(goalValue);
            }
        } catch (Exception e) {
            System.out.println("[GOAL] 로드 실패, 기본값 사용: " + e.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ignore) {}
            }
        }
    }

    /**
     * 목표값을 파일에 저장
     */
    public void save(double value) {
        this.goalValue = value;
        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            writer.write(String.valueOf(goalValue));
            System.out.println("[GOAL] 목표값 저장: " + goalValue);
        } catch (Exception e) {
            System.out.println("[GOAL] 저장 실패: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException ignore) {}
            }
        }
    }

    /** 현재 목표값 반환 */
    public double getGoalValue() {
        return goalValue;
    }

    /** 목표값 수정 */
    public void setGoalValue(double goalValue) {
        this.goalValue = goalValue;
        save(goalValue);
    }

    /**
     * 서버 SYNC_ALL에서 GOAL_RESULT가 내려오면 그것으로 덮어씀
     */
    public void applyGoalFromServer(String goal) {
        try {
            double v = Double.parseDouble(goal);
            setGoalValue(v);
        } catch (Exception ignore) {}
    }
}

