package io.github.phs314.quicktts.bot.domain.device;

import io.github.phs314.quicktts.bot.domain.InvalidDomainValueException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 등록된 PC 가 봇 서버에 자신을 증명하는 비밀 토큰. 서버에는 해시만 저장한다.
 */
public record DeviceToken(String value) {

    private static final SecureRandom RANDOM = new SecureRandom();

    public DeviceToken {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainValueException("기기 토큰이 비어 있습니다.");
        }
    }

    public static DeviceToken generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return new DeviceToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    /** 저장과 조회에 쓰는 SHA-256 해시 (16진수). */
    public String hash() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 을 쓸 수 없습니다.", e);
        }
    }

    @Override
    public String toString() {
        return "DeviceToken[****]";
    }
}
