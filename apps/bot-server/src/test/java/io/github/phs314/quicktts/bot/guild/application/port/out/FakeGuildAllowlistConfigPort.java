package io.github.phs314.quicktts.bot.guild.application.port.out;

/** 허용 목록 설정 대신 쓰는 대역. 처음에는 켜져 있다. */
public class FakeGuildAllowlistConfigPort implements GuildAllowlistConfigPort {

    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
