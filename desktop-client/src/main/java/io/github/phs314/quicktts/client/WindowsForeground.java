package io.github.phs314.quicktts.client;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

/**
 * 전역 단축키로 띄운 입력창이 클릭하지 않아도 바로 키보드 입력을 받게 한다.
 *
 * <p>윈도우는 다른 프로그램을 쓰는 중에 뒤에서 뜬 창이 앞으로 나와 입력을 가져가는 것을 막는다.
 * 단축키는 JNativeHook 이 엿들은 것이라 윈도우가 보기엔 이 프로그램이 받은 입력이 아니어서,
 * JavaFX 가 창을 앞으로 가져오려 해도(SetForegroundWindow) 거절당하고 창만 보인다.
 * 그래서 창을 띄우기 직전에 빈 마우스 입력을 하나 보내 이 프로그램이 마지막 입력을 보낸 것으로 만들고,
 * 그래도 안 되면 지금 앞에 있는 프로그램의 입력 상태를 잠깐 붙여서 앞으로 가져온다.
 *
 * <p>윈도우가 아니거나 호출이 실패하면 아무것도 하지 않는다. 그때는 입력창을 클릭하면 된다.
 */
final class WindowsForeground {

    private static final boolean WINDOWS = System.getProperty("os.name", "").startsWith("Windows");

    private WindowsForeground() {
    }

    /** 창을 띄우기 직전에 부른다. 커서를 움직이거나 누르지 않는 빈 마우스 입력을 보낸다. */
    static void allowActivation() {
        if (!WINDOWS) {
            return;
        }
        try (Arena arena = Arena.ofConfined()) {
            // INPUT 구조체 하나를 0 으로 채우면 type=INPUT_MOUSE, 이동·버튼 없음이다.
            MemorySegment input = arena.allocate(Win32.INPUT_SIZE);
            // invokeExact 는 돌려받는 타입까지 맞아야 해서 결과를 받되, 쓰지 않으므로 _ 로 버린다.
            int _ = (int) Win32.SEND_INPUT.invokeExact(1, input, (int) Win32.INPUT_SIZE);
        } catch (Throwable ignored) {
            // 앞으로 못 가져와도 입력창은 뜬다.
        }
    }

    /**
     * 창을 띄운 뒤에 부른다. 이 프로그램의 창이 아직 앞에 없으면 제목으로 창을 찾아 앞으로 가져온다.
     * JavaFX 창 스레드(FX 스레드)에서 불러야 한다.
     */
    static void activate(String windowTitle) {
        if (!WINDOWS) {
            return;
        }
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment foreground = (MemorySegment) Win32.GET_FOREGROUND_WINDOW.invokeExact();
            if (isThisProcess(arena, foreground)) {
                return;
            }
            MemorySegment title = arena.allocateFrom(windowTitle, StandardCharsets.UTF_16LE);
            MemorySegment window = (MemorySegment) Win32.FIND_WINDOW.invokeExact(MemorySegment.NULL, title);
            if (window.equals(MemorySegment.NULL) || !isThisProcess(arena, window)) {
                return;
            }
            int foregroundThread = (int) Win32.GET_WINDOW_THREAD_PROCESS_ID.invokeExact(foreground, MemorySegment.NULL);
            int thisThread = (int) Win32.GET_CURRENT_THREAD_ID.invokeExact();
            boolean attached = foregroundThread != 0 && foregroundThread != thisThread
                    && (int) Win32.ATTACH_THREAD_INPUT.invokeExact(thisThread, foregroundThread, 1) != 0;
            try {
                int _ = (int) Win32.BRING_WINDOW_TO_TOP.invokeExact(window);
                int _ = (int) Win32.SET_FOREGROUND_WINDOW.invokeExact(window);
            } finally {
                if (attached) {
                    int _ = (int) Win32.ATTACH_THREAD_INPUT.invokeExact(thisThread, foregroundThread, 0);
                }
            }
        } catch (Throwable ignored) {
            // 앞으로 못 가져와도 입력창은 뜬다.
        }
    }

    private static boolean isThisProcess(Arena arena, MemorySegment window) throws Throwable {
        if (window.equals(MemorySegment.NULL)) {
            return false;
        }
        MemorySegment processId = arena.allocate(JAVA_INT);
        int thread = (int) Win32.GET_WINDOW_THREAD_PROCESS_ID.invokeExact(window, processId);
        return thread != 0 && Integer.toUnsignedLong(processId.get(JAVA_INT, 0)) == ProcessHandle.current().pid();
    }

    /** user32·kernel32 함수. 윈도우에서 처음 쓸 때만 불러온다. */
    private static final class Win32 {

        /** 64비트 윈도우의 INPUT 구조체 크기(DWORD type + 8바이트 정렬된 MOUSEINPUT 32바이트). */
        static final long INPUT_SIZE = 40;

        private static final Linker LINKER = Linker.nativeLinker();
        private static final SymbolLookup USER32 = SymbolLookup.libraryLookup("user32", Arena.global());
        private static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("kernel32", Arena.global());

        static final MethodHandle SEND_INPUT =
                downcall(USER32, "SendInput", FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT));
        static final MethodHandle GET_FOREGROUND_WINDOW =
                downcall(USER32, "GetForegroundWindow", FunctionDescriptor.of(ADDRESS));
        static final MethodHandle FIND_WINDOW =
                downcall(USER32, "FindWindowW", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS));
        static final MethodHandle GET_WINDOW_THREAD_PROCESS_ID =
                downcall(USER32, "GetWindowThreadProcessId", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
        static final MethodHandle ATTACH_THREAD_INPUT =
                downcall(USER32, "AttachThreadInput", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT));
        static final MethodHandle BRING_WINDOW_TO_TOP =
                downcall(USER32, "BringWindowToTop", FunctionDescriptor.of(JAVA_INT, ADDRESS));
        static final MethodHandle SET_FOREGROUND_WINDOW =
                downcall(USER32, "SetForegroundWindow", FunctionDescriptor.of(JAVA_INT, ADDRESS));
        static final MethodHandle GET_CURRENT_THREAD_ID =
                downcall(KERNEL32, "GetCurrentThreadId", FunctionDescriptor.of(JAVA_INT));

        private static MethodHandle downcall(SymbolLookup library, String name, FunctionDescriptor descriptor) {
            return LINKER.downcallHandle(library.findOrThrow(name), descriptor);
        }
    }
}
