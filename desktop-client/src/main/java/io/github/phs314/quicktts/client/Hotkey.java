package io.github.phs314.quicktts.client;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 입력창을 여는 전역 단축키. 설정 파일에는 {@code Ctrl+Shift+Space} 처럼 글자로 저장한다.
 * 키 번호는 JNativeHook 의 {@code VC_} 값이라 키보드 배치나 한/영 상태와 상관없이 같은 키를 가리킨다.
 */
public record Hotkey(boolean ctrl, boolean alt, boolean shift, boolean win, int keyCode) {

    public static final Hotkey DEFAULT = new Hotkey(true, false, true, false, NativeKeyEvent.VC_SPACE);

    /** 단독으로는 단축키의 주 키가 될 수 없는 조합 키. */
    private static final Set<Integer> MODIFIER_KEYS = Set.of(
            NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_ALT, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_META);

    /** 키 번호와 이름. {@code VC_PAGE_UP} 이면 {@code PageUp} 이다. */
    private static final Map<Integer, String> KEY_NAMES = keyNames();

    /** 문장을 치다가 잘못 눌리지 않도록, F 키가 아니면 Ctrl·Alt·Win 중 하나는 같이 눌러야 한다. */
    public Optional<String> validationProblem() {
        if (ctrl || alt || win || isFunctionKey()) {
            return Optional.empty();
        }
        return Optional.of("Ctrl, Alt, Win 중 하나와 같이 눌러 주세요. F1~F24 는 혼자 써도 돼요.");
    }

    /** JNativeHook 이 알려 준 키 입력이 이 단축키인지. 조합 키는 정확히 같아야 한다. */
    public boolean matches(int modifiers, int pressedKeyCode) {
        return equals(of(modifiers, pressedKeyCode));
    }

    /** 키 입력을 단축키로 바꾼다. 조합 키만 눌렀거나 이름을 모르는 키면 비어 있다. */
    public static Optional<Hotkey> fromEvent(int modifiers, int pressedKeyCode) {
        if (MODIFIER_KEYS.contains(pressedKeyCode) || !KEY_NAMES.containsKey(pressedKeyCode)) {
            return Optional.empty();
        }
        return Optional.of(of(modifiers, pressedKeyCode));
    }

    /** {@code Ctrl+Shift+Space} 같은 글자를 읽는다. 대소문자는 가리지 않고, 못 읽거나 쓸 수 없는 단축키면 비어 있다. */
    public static Optional<Hotkey> parse(String text) {
        boolean ctrl = false, alt = false, shift = false, win = false;
        Integer keyCode = null;
        for (String part : text.split("\\+")) {
            String name = part.strip().toLowerCase(Locale.ROOT);
            switch (name) {
                case "ctrl" -> ctrl = true;
                case "alt" -> alt = true;
                case "shift" -> shift = true;
                case "win" -> win = true;
                default -> {
                    if (keyCode != null) {
                        return Optional.empty();
                    }
                    keyCode = keyCodeOf(name);
                    if (keyCode == null) {
                        return Optional.empty();
                    }
                }
            }
        }
        if (keyCode == null) {
            return Optional.empty();
        }
        Hotkey hotkey = new Hotkey(ctrl, alt, shift, win, keyCode);
        return hotkey.validationProblem().isEmpty() ? Optional.of(hotkey) : Optional.empty();
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        if (ctrl) text.append("Ctrl+");
        if (alt) text.append("Alt+");
        if (shift) text.append("Shift+");
        if (win) text.append("Win+");
        return text.append(KEY_NAMES.getOrDefault(keyCode, "Key" + keyCode)).toString();
    }

    private boolean isFunctionKey() {
        return KEY_NAMES.getOrDefault(keyCode, "").matches("F\\d+");
    }

    private static Hotkey of(int modifiers, int keyCode) {
        return new Hotkey(
                (modifiers & NativeKeyEvent.CTRL_MASK) != 0,
                (modifiers & NativeKeyEvent.ALT_MASK) != 0,
                (modifiers & NativeKeyEvent.SHIFT_MASK) != 0,
                (modifiers & NativeKeyEvent.META_MASK) != 0,
                keyCode);
    }

    private static Integer keyCodeOf(String lowerCaseName) {
        for (Map.Entry<Integer, String> entry : KEY_NAMES.entrySet()) {
            if (entry.getValue().toLowerCase(Locale.ROOT).equals(lowerCaseName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static Map<Integer, String> keyNames() {
        Map<Integer, String> names = new LinkedHashMap<>();
        for (Field field : NativeKeyEvent.class.getFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) || field.getType() != int.class || !field.getName().startsWith("VC_")) {
                continue;
            }
            try {
                int code = field.getInt(null);
                if (code != NativeKeyEvent.VC_UNDEFINED) {
                    names.putIfAbsent(code, pascalCase(field.getName().substring("VC_".length())));
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return Collections.unmodifiableMap(names);
    }

    private static String pascalCase(String upperSnake) {
        StringBuilder name = new StringBuilder();
        for (String word : upperSnake.split("_")) {
            name.append(word.charAt(0)).append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return name.toString();
    }
}
