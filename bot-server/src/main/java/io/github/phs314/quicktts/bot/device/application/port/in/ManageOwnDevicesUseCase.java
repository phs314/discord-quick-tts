package io.github.phs314.quicktts.bot.device.application.port.in;

import io.github.phs314.quicktts.bot.device.domain.Device;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.List;

/**
 * 사용자가 자기에게 연결된 PC 를 보고 연결을 끊는다. 다른 사람의 기기는 건드릴 수 없다.
 */
public interface ManageOwnDevicesUseCase {

    List<Device> listDevices(DiscordUserId owner);

    /** 그 사용자의 기기가 아니거나 이미 해제됐으면 false. */
    boolean unlink(DiscordUserId owner, DeviceId id);

    /** 해제한 기기 수를 돌려준다. */
    int unlinkAll(DiscordUserId owner);
}
