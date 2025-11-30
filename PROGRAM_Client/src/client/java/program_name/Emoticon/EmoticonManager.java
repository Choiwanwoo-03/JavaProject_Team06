package Emoticon;

import java.util.*;

/**
 * 클라이언트 이모티콘 상태를 관리하는 클래스
 * - 전체 이모티콘 목록
 * - 공개(해금)된 이모티콘 이름 관리
 * - 서버 전송용 문자열 생성 (GiveInformation에서 사용)
 */
public class EmoticonManager {

    // 이모티콘 정보 구조체
    public static class EmoticonInfo {
        public final String emoji;       // 이모지 자체 (🌳, ♻️ 등)
        public final String name;        // 이모티콘 이름
        public final String description; // 설명

        public EmoticonInfo(String emoji, String name, String description) {
            this.emoji = emoji;
            this.name = name;
            this.description = description;
        }
    }

    // 전체 이모티콘 목록 (EnvironmentalActionTrackerApp의 EmojiCollection과 동일한 구성)
    private static final List<EmoticonInfo> ALL_EMOTICONS = List.of(
        new EmoticonInfo("🌳", "미니 나무", "나무 심기 운동에 기여했어요!"),
        new EmoticonInfo("♻️", "재활용 박사", "분리수거를 완벽하게 해냈어요!"),
        new EmoticonInfo("💧", "물방울 친구", "샤워 시간을 줄이고 물을 절약했어요."),
        new EmoticonInfo("💡", "절약 전구", "대기전력을 차단하고 전기를 아꼈어요."),
        new EmoticonInfo("🚲", "자전거 여행자", "자전거를 타고 탄소 배출을 줄였어요."),
        new EmoticonInfo("🧺", "장바구니 마스터", "일회용 봉투 없이 쇼핑을 완료했어요."),
        new EmoticonInfo("🍲", "잔반 제로", "음식물 쓰레기 없이 식사를 마쳤어요.")
    );

    // 해금된(공개된) 이모티콘 이름들
    private final Set<String> unlockedNames = new HashSet<>();

    public EmoticonManager() {
        // 필요하면 기본 해금 이모티콘을 여기서 추가할 수 있음
        // 예: unlockedNames.add("미니 나무");
    }

    /** 전체 이모티콘 리스트 반환 (GUI에서 사용) */
    public List<EmoticonInfo> getAllEmoticons() {
        return ALL_EMOTICONS;
    }

    /** 특정 이모티콘이 해금 상태인지 여부 */
    public boolean isUnlocked(String name) {
        return unlockedNames.contains(name);
    }

    /** 이모티콘 해금 */
    public void unlock(String name) {
        unlockedNames.add(name);
    }

    /** 여러 개를 한 번에 해금 (서버에서 받아올 때 사용 가능) */
    public void unlockAll(Collection<String> names) {
        unlockedNames.addAll(names);
    }

    /**
     * 서버에서 "미니 나무,절약 전구,..." 이런 형태의 문자열을 보냈다고 가정하고
     * 클라이언트에서 해금 상태로 반영할 때 사용할 수 있는 메소드
     */
    public void loadUnlockedFromCsv(String csv) {
        unlockedNames.clear();
        if (csv == null || csv.isEmpty()) return;

        String[] parts = csv.split(",");
        for (String p : parts) {
            String name = p.trim();
            if (!name.isEmpty()) {
                unlockedNames.add(name);
            }
        }
    }

    /**
     * 클라이언트 종료 시 서버로 보낼 데이터 문자열 생성
     *
     * 프로토콜 예시:
     *   EMOTICON_DATA///미니 나무,절약 전구,잔반 제로///END
     *
     * GiveInformation.java에서 이 문자열을 그대로 writeUTF 하면 됨.
     */
    public String buildSendData() {
        StringBuilder sb = new StringBuilder();
        sb.append("EMOTICON_DATA///");

        int i = 0;
        for (String name : unlockedNames) {
            sb.append(name);
            if (i < unlockedNames.size() - 1) {
                sb.append(",");
            }
            i++;
        }

        sb.append("///END");
        return sb.toString();
    }

    /** 현재 해금된 이모티콘 이름들만 반환 (필요시 사용) */
    public Set<String> getUnlockedNames() {
        return new HashSet<>(unlockedNames);
    }
}
