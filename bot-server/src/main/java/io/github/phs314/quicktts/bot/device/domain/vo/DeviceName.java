package io.github.phs314.quicktts.bot.device.domain.vo;

/**
 * 사용자에게 보여 줄 PC 이름. 클라이언트가 컴퓨터 이름을 보내 주고, 없으면 기본 이름을 쓴다.
 * 디스코드 메뉴 항목 길이에 맞춰 {@link #MAX_LENGTH} 자로 자른다.
 */
public record DeviceName(String value) {

    public static final int MAX_LENGTH = 50;

    private static final String DEFAULT = "이름 없는 PC";

    public DeviceName {
        value = value == null || value.isBlank() ? DEFAULT : value.strip();
        if (value.length() > MAX_LENGTH) {
            value = value.substring(0, MAX_LENGTH);
        }
    }
}
