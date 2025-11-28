package Goal;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;


public class GoalManager {
    private final Path file;
    private final double defaultGoal;

    public GoalManager(Path file, double defaultGoal) {
        this.file = file; this.defaultGoal = defaultGoal;
    }

    // 목표값 로드 (없으면 기본값) 
    public double load() {
        try {
            if (Files.exists(file)) {
                return Double.parseDouble(Files.readString(file).trim());
            }
        } catch (Exception ignored) {}
        return defaultGoal;
    }

    // 목표값 저장 (설정 화면에서 호출) 
    public void save(double goal) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, String.valueOf(goal), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ignored) {}
    }
}

