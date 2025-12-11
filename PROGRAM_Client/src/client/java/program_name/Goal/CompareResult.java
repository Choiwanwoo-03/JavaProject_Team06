package Goal;

/**
 * CompareResult:
 * - AchieveGoal_Gui에서 목표 vs 오늘 결과를 계산해 UI로 넘겨주는 DTO
 * - boolean isSuccess
 * - double diff (목표 대비 차이)
 * - double percent (달성률 %)
 */
public class CompareResult {

    public final double todayEmission;
    public final double goalEmission;
    public final boolean success;
    public final double diff;
    public final double percent;

    public CompareResult(double today, double goal) {
        this.todayEmission = today;
        this.goalEmission = goal;

        this.success = today <= goal;

        this.diff = success ? (goal - today) : (today - goal);

        if (goal > 0) {
            this.percent = (today / goal) * 100.0;
        } else {
            this.percent = 0;
        }
    }
}
