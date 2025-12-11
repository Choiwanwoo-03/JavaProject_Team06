package Goal;

import java.util.*;

/**
 * MakeSolution
 * - CompareResult의 결과(success, diff 등)에 따라
 *   칭찬/권장/절약 팁 문구를 제공하는 클래스
 * - AchieveGoal_Gui에서 사용
 */
public class MakeSolution {

    private static final List<String> successList = Arrays.asList(
            "아주 좋아요! 오늘 탄소 배출을 멋지게 관리했어요!",
            "대단해요! 오늘 목표를 충분히 달성했습니다!",
            "지구가 환하게 웃고 있어요! 👍"
    );

    private static final List<String> failList = Arrays.asList(
            "오늘 조금 초과되었지만 내일 더 잘할 수 있어요!",
            "실천은 조금 어렵지만 꾸준하면 충분히 달성할 수 있어요.",
            "작은 습관을 바꿔보면 큰 도움이 될 거예요!"
    );

    private static final List<String> tipsList = Arrays.asList(
            "대중교통을 이용하거나 도보로 이동해보세요.",
            "조명을 끄고, 대기전력을 줄여보세요.",
            "음식물 쓰레기를 줄이면 탄소를 크게 절약할 수 있어요.",
            "플라스틱 대신 텀블러와 장바구니를 사용해보세요.",
            "가까운 거리는 자전거나 걸어서 다녀보세요!"
    );

    private final Random rnd = new Random();

    /** 랜덤 문구 1개 선택 */
    private String pick(List<String> list) {
        return list.get(rnd.nextInt(list.size()));
    }

    /**
     * 오늘 결과에 따른 솔루션 생성
     */
    public String buildMessage(CompareResult result) {
        if (result.success) {
            return pick(successList);
        } else {
            return pick(failList) + "\nTip: " + pick(tipsList);
        }
    }
}
