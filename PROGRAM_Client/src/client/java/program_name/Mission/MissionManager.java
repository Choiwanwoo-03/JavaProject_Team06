package Mission;



import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 오늘 미션 선정 + 파일 저장
 * 반환: (오늘의 이모지 주제명, 미션 3개[고정1+주제2])
 */
public class MissionManager {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Map<String, String[]> emojiMissions;
    private final Path storeFile;

    public MissionManager(Map<String, String[]> emojiMissions, Path storeFile) {
        this.emojiMissions = emojiMissions; this.storeFile = storeFile;
    }

    public static Map<String, String[]> defaultEmojiMissions() {
        Map<String, String[]> m = new LinkedHashMap<>();
        m.put("미니 나무", new String[]{"전자문서로 과제·메모 작성하기", "노트/종이 양면 활용하기"});
        m.put("재활용 박사", new String[]{"플라스틱 용기 세척 후 분리배출하기", "캔·유리 재질/색상별로 분리하기"});
        m.put("물방울 친구", new String[]{"샤워 5분 이내로 줄이기", "세탁물 모아서 1회만 세탁하기"});
        m.put("절약 전구", new String[]{"외출 전 모든 조명 끄기", "사용하지 않는 플러그 뽑기"});
        m.put("자전거 여행자", new String[]{"3km 이내 도보·자전거로 이동하기", "대중교통 이용 기록 추가하기"});
        m.put("장바구니 마스터", new String[]{"장보기 시 장바구니 사용 인증하기", "개인 텀블러 사용하기"});
        m.put("잔반 제로", new String[]{"남김 없이 식사 완료하기", "유통기한 임박 식품 먼저 소비하기"});
        return m;
    }

    public Map.Entry<String, List<ClearedMission.Mission>> pickTodayMissions(double fixedDailyGoal, double todayEmission) {
        List<String> topics = new ArrayList<>(emojiMissions.keySet());
        Collections.shuffle(topics);
        String topic = topics.get(0);
        String[] two = emojiMissions.get(topic);

        List<ClearedMission.Mission> missions = new ArrayList<>();
        // 고정 자동미션
        ClearedMission.Mission fixed = new ClearedMission.Mission(
            "1일 권장 배출량 " + fixedDailyGoal + "kg 이하 유지 (자동판정)", true
        );
        fixed.completed = (todayEmission <= fixedDailyGoal + 1e-9);
        missions.add(fixed);

        // 주제 2개
        if (two != null && two.length >= 2) {
            missions.add(new ClearedMission.Mission(two[0], false));
            missions.add(new ClearedMission.Mission(two[1], false));
        }
        return new AbstractMap.SimpleEntry<>(topic, missions);
    }

    public void save(LocalDate date, String topic, List<ClearedMission.Mission> missions) {
        try {
            Files.createDirectories(storeFile.getParent());
            List<String> lines = new ArrayList<>();
            lines.add(date.format(DATE_FMT));
            lines.add(topic);
            for (var m : missions) lines.add(m.text + "," + m.completed + "," + m.autoEvaluated);
            Files.write(storeFile, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ignored) {}
    }
}

