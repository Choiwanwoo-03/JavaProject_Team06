package Mission;

import java.util.*;

public class ClearedMission {
    /** 미션 1개 단위 */
    public static final class Mission {
        public final String text;
        public boolean completed;
        public final boolean autoEvaluated;
        public Mission(String text, boolean autoEvaluated) {
            this.text = text; this.autoEvaluated = autoEvaluated;
        }
    }

    // 완료 개수 
    public int countCompleted(List<Mission> ms) {
        int n = 0; for (Mission m : ms) if (m.completed) n++; return n;
    }

    // 전부 완료? (보상 지급 조건 등)
    public boolean allCompleted(List<Mission> ms) {
        return ms.stream().allMatch(m -> m.completed);
    }
}
