package io.github.phs314.quicktts.bot.speech.domain.normalization;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.vo.SpokenText;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 보낸 문장을 소리 내 읽기 좋은 글로 바꾸는 읽기 다듬기(text normalization) 규칙.
 * 링크는 "링크를 보냈어요" 로 바꾸고, 이모지는 지우고, ㅋ·ㅎ·ㅇ·ㄴ·ㄱ 는 키·히·이응·니은·고 로 읽는다.
 * 그 밖의 글자는 그대로 둔다. 저장하는 것 없이 글만 바꾸는 도메인 서비스다.
 */
public final class TextNormalizer {

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

    private TextNormalizer() {
    }

    /**
     * @throws InvalidDomainValueException 다듬고 나니 읽을 내용이 없을 때 (예: 이모지만 보냄)
     */
    public static SpokenText normalize(QuickChatMessage message) {
        String spoken = LINK.matcher(message.text()).replaceAll(Matcher.quoteReplacement(LINK_SPOKEN));
        spoken = EMOJI.matcher(spoken).replaceAll("");
        spoken = spellOutConsonants(spoken);
        return new SpokenText(SPACES.matcher(spoken).replaceAll(" ").strip());
    }

    private static String spellOutConsonants(String text) {
        StringBuilder spoken = new StringBuilder(text.length());
        text.chars().forEach(c -> spoken.append(CONSONANT_SOUNDS.getOrDefault((char) c, String.valueOf((char) c))));
        return spoken.toString();
    }
}
