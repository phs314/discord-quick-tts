package io.github.phs314.quicktts.bot.speech.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 사용자가 quick chat 으로 보낸, 음성으로 읽어 줄 문장.
 * 앞뒤 공백은 지우고, 비어 있거나 {@link #MAX_LENGTH} 자를 넘으면 만들 수 없다.
 * 글자 수는 보낸 그대로 센다. 엔진에 보낼 때는 {@link #toSpokenText()} 로 다듬는다.
 */
public record QuickChatMessage(String text) {

    public static final int MAX_LENGTH = 200;

    private static final Pattern LINK = Pattern.compile("(?i)(?:https?://|www\\.)\\S+");
    private static final String LINK_SPOKEN = "링크를 보냈어요";

    // 그림 문자와 그에 붙는 피부색, 이음 문자(ZWJ), 국기용 지역 문자, 키캡·변형 선택자, 태그 문자.
    private static final Pattern EMOJI = Pattern.compile(
            "[\\p{IsExtended_Pictographic}\\p{IsEmoji_Modifier}\\x{1F1E6}-\\x{1F1FF}"
                    + "\\x{200D}\\x{20E3}\\x{FE0E}\\x{FE0F}\\x{E0020}-\\x{E007F}]");

    /** 따로 쓴 자음을 읽는 법. 반복된 만큼 그대로 읽는다 (ㅋㅋㅋ → 키키키). */
    private static final Map<Character, String> CONSONANT_SOUNDS = Map.of(
            'ㅋ', "키",
            'ㅎ', "히",
            'ㅇ', "이응",
            'ㄴ', "니은",
            'ㄱ', "고");

    private static final Pattern SPACES = Pattern.compile("\\s+");

    public QuickChatMessage {
        if (text == null || text.isBlank()) {
            throw new InvalidDomainValueException("문장이 비어 있습니다.");
        }
        text = text.strip();
        if (text.length() > MAX_LENGTH) {
            throw new InvalidDomainValueException("문장은 " + MAX_LENGTH + "자를 넘을 수 없습니다.");
        }
    }

    /**
     * 소리 내 읽기 좋게 다듬는다. 링크는 "링크를 보냈어요" 로 바꾸고, 이모지는 지우고,
     * ㅋ·ㅎ·ㅇ·ㄴ·ㄱ 는 키·히·이응·니은·고 로 읽는다. 그 밖의 글자는 그대로 둔다.
     *
     * @throws InvalidDomainValueException 다듬고 나니 읽을 내용이 없을 때 (예: 이모지만 보냄)
     */
    public SpokenText toSpokenText() {
        String spoken = LINK.matcher(text).replaceAll(Matcher.quoteReplacement(LINK_SPOKEN));
        spoken = EMOJI.matcher(spoken).replaceAll("");
        spoken = readConsonants(spoken);
        return new SpokenText(SPACES.matcher(spoken).replaceAll(" ").strip());
    }

    private static String readConsonants(String text) {
        StringBuilder spoken = new StringBuilder(text.length());
        text.chars().forEach(c -> spoken.append(CONSONANT_SOUNDS.getOrDefault((char) c, String.valueOf((char) c))));
        return spoken.toString();
    }
}
