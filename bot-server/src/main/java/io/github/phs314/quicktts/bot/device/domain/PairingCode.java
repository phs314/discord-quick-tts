package io.github.phs314.quicktts.bot.device.domain;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.random.RandomGenerator;

/**
 * 디스코드의 {@code /연결} 명령으로 받아 클라이언트에 한 번 입력하는 일회용 연결 코드.
 * 헷갈리는 글자(0, O, 1, I, L)를 뺀 8자리를 {@code ABCD-EFGH} 모양으로 보여 준다.
 */
public record PairingCode(String value) {

    static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    static final int LENGTH = 8;

    private static final RandomGenerator RANDOM = new SecureRandom();

    public PairingCode {
        if (value == null) {
            throw new InvalidDomainValueException("연결 코드가 비어 있습니다.");
        }
        value = normalize(value);
        if (value.length() != LENGTH || !value.chars().allMatch(c -> ALPHABET.indexOf(c) >= 0)) {
            throw new InvalidDomainValueException("연결 코드 형식이 아닙니다.");
        }
    }

    public static PairingCode generate() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new PairingCode(code.toString());
    }

    /** 사용자에게 보여 줄 모양. */
    public String display() {
        return value.substring(0, LENGTH / 2) + "-" + value.substring(LENGTH / 2);
    }

    /** 사용자가 붙여 넣은 값의 공백, 하이픈, 대소문자 차이를 없앤다. */
    private static String normalize(String raw) {
        return raw.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }
}
