package Emoticon;

import java.util.*;


 // 클라이언트 이모티콘 상태를 관리하는 클래스
public class EmoticonManager {

    // 이모티콘 정보 구조체
    public static class EmoticonInfo {
        public final String emoji;       // 이모지 자체 (🌳, ♻️ 등)
        public final String name;        // 이모티콘 이름 (DB에 저장되는 이름)
        public final String description; // 설명

        public EmoticonInfo(String emoji, String name, String description) {
            this.emoji = emoji;
            this.name = name;
            this.description = description;
        }
    }

    // 전체 이모티콘 목록 (name 기준으로 정렬 보존을 위해 LinkedHashMap 사용)
    private final Map<String, EmoticonInfo> allEmoticons = new LinkedHashMap<>();

    // 해금된 이모티콘 이름들 (name 기준)
    private final Set<String> unlockedNames = new HashSet<>();

    public EmoticonManager() {
        initDefaultEmoticons();
    }

    // 기본 이모티콘 목록 정의
    private void initDefaultEmoticons() {
        // 이름은 DB IMOTICON_TABLE과 맞추면 좋음 (여기서는 예시)
        add("🌳", "미니 나무", "하루 동안 목표 이하의 탄소를 배출했을 때 주어지는 작은 나무 이모티콘입니다.");
        add("💡", "절약 전구", "전기 사용을 줄이고 에너지를 절약했을 때 해금되는 전구 이모티콘입니다.");
        add("🚲", "친환경 라이더", "자동차 대신 대중교통이나 자전거를 이용했을 때 얻을 수 있는 이모티콘입니다.");
        add("♻️", "재활용 마스터", "분리수거를 성실하게 수행했을 때 획득하는 이모티콘입니다.");
        add("🌏", "지구 지킴이", "여러 날 동안 꾸준히 탄소 배출을 줄인 사용자에게 주어지는 특별한 이모티콘입니다.");
        add("🏅", "챌린지 완주", "특정 기간 동안 연속으로 목표를 달성했을 때 획득하는 메달 이모티콘입니다.");
    }

    private void add(String emoji, String name, String description) {
        allEmoticons.put(name, new EmoticonInfo(emoji, name, description));
    }

    // 전체 이모티콘 정보를 순서대로 반환 (도감 표시용)
    public Collection<EmoticonInfo> getAllEmoticons() {
        return Collections.unmodifiableCollection(allEmoticons.values());
    }

    // 이름으로 이모티콘 정보 조회
    public EmoticonInfo getInfo(String name) {
        return allEmoticons.get(name);
    }

    // 특정 이모티콘이 해금되어 있는지 여부
    public boolean isUnlocked(String name) {
        return unlockedNames.contains(name);
    }

    // 하나의 이모티콘 해금
    public void unlock(String name) {
        if (allEmoticons.containsKey(name)) {
            unlockedNames.add(name);
        }
    }

    // 여러 개를 한 번에 해금 (서버에서 받은 목록을 반영할 때 사용)
    public void unlockAll(Collection<String> names) {
        if (names == null) return;
        for (String n : names) {
            if (allEmoticons.containsKey(n)) {
                unlockedNames.add(n);
            }
        }
    }

    public void loadUnlockedFromCsv(String csv) {
        unlockedNames.clear();
        if (csv == null || csv.isEmpty()) return;

        String[] parts = csv.split(",");
        for (String p : parts) {
            String name = p.trim();
            if (!name.isEmpty() && allEmoticons.containsKey(name)) {
                unlockedNames.add(name);
            }
        }
    }

    public String buildUnlockedCsv() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String name : unlockedNames) {
            if (i > 0) sb.append(",");
            sb.append(name);
            i++;
        }
        return sb.toString();
    }

    // EmoticonManager.java 내부에 추가
    public List<String> getUnlockedNamesList() {
        return new ArrayList<>(unlockedNames);
    }
    
    public String getRandomLockedEmoticon() {
        // 1. 잠겨있는(아직 못 얻은) 이모티콘 목록 만들기
        List<String> lockedList = new ArrayList<>();
        
        for (String name : allEmoticons.keySet()) {
            if (!unlockedNames.contains(name)) {
                lockedList.add(name);
            }
        }

        // 2. 다 모았으면 null 반환
        if (lockedList.isEmpty()) {
            return null;
        }

        // 3. 랜덤으로 하나 뽑기
        Random rnd = new Random();
        int index = rnd.nextInt(lockedList.size());
        return lockedList.get(index);
    }
}
