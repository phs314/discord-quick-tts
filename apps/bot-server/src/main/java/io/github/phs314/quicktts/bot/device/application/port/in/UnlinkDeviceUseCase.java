package io.github.phs314.quicktts.bot.device.application.port.in;

import io.github.phs314.quicktts.bot.device.application.port.in.command.UnlinkDeviceCommand;

/**
 * 사용자가 자기에게 연결된 PC 하나의 연결을 끊는다. 다른 사람의 기기는 건드릴 수 없다.
 */
public interface UnlinkDeviceUseCase {

    /** 그 사용자의 기기가 아니거나 이미 해제됐으면 false. */
    boolean unlink(UnlinkDeviceCommand command);
}
