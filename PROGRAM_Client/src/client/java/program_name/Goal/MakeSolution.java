package Goal;



import java.util.*;

/** 오늘 목표 달성/초과 결과에 따른 해결책/칭찬 메시지 생성 */
public class MakeSolution {
	// 목표 초과 시: 즉시 실천 팁 리스트 생성
    public List<String> suggestForOverGoal(double overByKg) {
        List<String> tips = new ArrayList<>();
        tips.add("샤워 시간을 5분 이내로 줄여보세요.");
        tips.add("외출 시 모든 조명과 멀티탭 전원을 꺼두세요.");
        tips.add("3km 이내는 도보/자전거, 그 이상은 대중교통을 고려하세요.");
        tips.add("플라스틱·캔·유리는 세척 후 분리배출하여 순환을 늘리세요.");
        tips.add("장볼 때 장바구니·텀블러를 사용하세요.");
        if (overByKg > 2.0) tips.add("내일은 스트리밍 대신 다운로드 이용 등 '디지털 탄소'도 줄여보세요.");
        return tips;
    }

    public List<String> praiseForWithinGoal(double savedKg) {
        return List.of(
            "아주 좋아요! 오늘 " + String.format("%.1f", savedKg) + "kg 절감!",
            "내일은 '대중교통 + 재활용'으로 연속 성공을 노려보세요."
        );
    }
}

