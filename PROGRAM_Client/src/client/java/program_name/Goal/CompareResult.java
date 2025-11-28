package Goal;


/** 목표 vs 오늘 비교(절감량, 달성률, 초과 여부 계산) */
public class CompareResult {
    public static final class Result {
        public final double todayEmission, goal, saved, achievement; // %
        public final boolean withinGoal;
        public Result(double todayEmission, double goal) {
            this.todayEmission = todayEmission;
            this.goal = goal;
            this.saved = Math.max(0, goal - todayEmission);
            this.withinGoal = todayEmission <= goal + 1e-9;
            this.achievement = withinGoal ? (saved / goal) * 100.0 : 0.0;
        }
    }

    public Result compare(double todayEmission, double goal) {
        return new Result(todayEmission, goal);
    }
}

