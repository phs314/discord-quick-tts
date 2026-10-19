package io.github.phs314.quicktts.bot.device.application.port.in;

import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkAllDevicesCommand;

/**
 * 사용자가 자기에게 연결된 PC 를 모두 끊는다. 다른 사람의 기기는 건드릴 수 없다.
 */
public interface UnlinkAllDevicesUseCase {

    /** 해제한 기기 수를 돌려준다. */
    int unlinkAll(UnlinkAllDevicesCommand command);
}
