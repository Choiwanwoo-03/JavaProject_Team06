package Mission;


import java.util.*;

/** 미션 완료 상태 카운팅/검증 */
public class ClearedMission {
    public static final class Mission {
        public final String text;
        public boolean completed;
        public final boolean autoEvaluated; // 자동 판정(예: 1일 권장 이하 유지)
        public Mission(String text, boolean autoEvaluated) {
            this.text = text; this.autoEvaluated = autoEvaluated;
        }
    }

    public int countCompleted(List<Mission> ms) {
        int n = 0; for (Mission m : ms) if (m.completed) n++; return n;
    }

    public boolean allCompleted(List<Mission> ms) {
        return ms.stream().allMatch(m -> m.completed);
    }
}
