package io.github.phs314.quicktts.bot.identity.application.port.in;

import io.github.phs314.quicktts.bot.identity.application.port.in.command.RegisterDeviceCommand;
import io.github.phs314.quicktts.bot.identity.domain.vo.DeviceToken;

/**
 * 연결 코드를 쓰고 그 PC 전용 기기 토큰을 발급한다. 코드는 한 번만 쓸 수 있다.
 */
public interface RegisterDeviceUseCase {

    /**
     * @throws io.github.phs314.quicktts.bot.identity.domain.InvalidPairingCodeException
     *         없는 코드이거나 만료된 코드일 때
     */
    DeviceToken register(RegisterDeviceCommand command);
}
