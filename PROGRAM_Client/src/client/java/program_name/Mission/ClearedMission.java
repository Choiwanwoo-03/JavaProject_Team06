package Mission;

/**
 * 미션 달성 로직을 담는 클래스
 * - MissionManager에서 사용
 * - 미션 1개 = Mission(name)
 */
public class ClearedMission {

    /** 단일 미션 정의 */
    public static class Mission {

        public final String name; // "오늘 목표 이하로 유지" 같은 문구

        public Mission(String name) {
            this.name = name;
        }

        /**
         * 미션 달성 여부를 계산
         * @param todayEmission 오늘 배출량(kg)
         * @param todayGoal     오늘 목표(kg)
         */
        public boolean isCompleted(double todayEmission, double todayGoal) {

            // 가장 기본적인 형태:
            // "오늘 목표 이하 배출" 미션의 기본 로직
            if (name.contains("목표") && name.contains("유지")) {
                return todayEmission <= todayGoal;
            }

            // 교통 탄소 3kg 이하
            if (name.contains("교통") && name.contains("3kg")) {
                return todayEmission <= 3.0;
            }

            // 음식물 쓰레기 300g 이하 (todayEmission을 CO2e 추정값으로 사용하는 예시)
            if (name.contains("음식물") && name.contains("300g")) {
                return todayEmission <= 0.3;
            }

            // 전기 사용 2시간 이하 (todayEmission을 전기 CO2e 로 간주하는 예시)
            if (name.contains("전기") && name.contains("2시간")) {
                return todayEmission <= 0.06; // 2시간 전기 사용 CO₂e 0.06kg 가정
            }

            // 조건에 명시되지 않았다면 "목표 이하 유지" 기본 규칙 적용
            return todayEmission <= todayGoal;
        }
    }

    /** 미션 성공 개수 세기 — 필요 시 사용 */
    public int countCompleted(Mission mission, double todayEmission, double todayGoal) {
        return mission.isCompleted(todayEmission, todayGoal) ? 1 : 0;
    }

    /** 모든 미션을 완료했는지 판단 — 단일 미션 기준에서는 todaySuccess == 1 의미 */
    public boolean allCompleted(int todaySuccess) {
        return todaySuccess == 1;
    }
}
