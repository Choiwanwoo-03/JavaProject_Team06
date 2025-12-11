package Calculation;

import java.util.Map;

/**
 * CarbonCalculator
 * - 사용자의 행동(actionName)과 수량(count)을 기반으로 CO2e(kg)를 계산하는 클래스
 * - AddAction_Gui에서 호출됨
 * - co2eFactors: 행동별 계수
 *
 * 예)
 *  비행기: 0.152 kg/km
 *  노트북: 0.012 kg/hour
 *  음식물 쓰레기: 0.45 g → (0.45 / 1000) kg 로 저장됨
 */
public class CarbonCalculator {

    private final Map<String, Double> co2eFactors;

    public CarbonCalculator(Map<String, Double> co2eFactors) {
        this.co2eFactors = co2eFactors;
    }

    /**
     * 행동에 따른 CO2e 계산
     * @param actionName 행동 타입 (예: "비행기", "음식물 쓰레기")
     * @param count      수량 (km, 시간, g 등)
     * @param category   교통/에너지/쓰레기 구분용 (현재는 정보용)
     */
    public double CalculateActionCarbon(String actionName, double count, String category) {
        if (!co2eFactors.containsKey(actionName)) {
            System.out.println("[Carbon] 등록되지 않은 행동입니다: " + actionName);
            return 0.0;
        }

        double factor = co2eFactors.get(actionName);

        // 가중치 × 수량
        double co2e = factor * count;

        // 소수점 3자리까지
        return Math.round(co2e * 1000.0) / 1000.0;
    }
}
