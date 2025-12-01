package Emoticon;

import java.util.*;

/** 이모티콘 공개(카탈로그) + 조회 유틸 */
public class OpeningEmoticon {
    public static final class EmojiInfo {
        public final String emoji, name, description;
        public EmojiInfo(String emoji, String name, String description) {
            this.emoji = emoji; this.name = name; this.description = description;
        }
    }

    public static final List<EmojiInfo> ALL = List.of(
        new EmojiInfo("🌳", "미니 나무", "나무 심기 운동에 기여했어요!"),
        new EmojiInfo("♻️", "재활용 박사", "분리수거를 완벽하게 해냈어요!"),
        new EmojiInfo("💧", "물방울 친구", "물을 절약했어요!"),
        new EmojiInfo("💡", "절약 전구", "전기를 아꼈어요!"),
        new EmojiInfo("🚲", "자전거 여행자", "탄소 배출을 줄였어요!"),
        new EmojiInfo("🧺", "장바구니 마스터", "일회용 봉투 없이 쇼핑을 완료했어요!"),
        new EmojiInfo("🍲", "잔반 제로", "음식물 쓰레기 없이 식사를 마쳤어요!")
    );

    public static Optional<EmojiInfo> findByName(String name) {
        return ALL.stream().filter(e -> e.name.equals(name)).findFirst();
    }
}
