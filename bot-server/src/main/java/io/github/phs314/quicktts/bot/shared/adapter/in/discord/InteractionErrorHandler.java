package io.github.phs314.quicktts.bot.shared.adapter.in.discord;

import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import java.util.Objects;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 슬래시 명령 하나를 감싸서, 명령이 잡지 않은 실패에도 명령을 친 사람에게 답을 보낸다.
 * 답이 없으면 디스코드는 "애플리케이션이 응답하지 않았어요" 만 보여 준다.
 * 예상한 실패({@link BusinessException})는 그 문구를, 그 밖의 실패는 다시 해 달라는 안내를 보내고 로그를 남긴다.
 * 명령은 안내 문구를 바꾸고 싶은 예외만 직접 잡는다 (ADR 0025).
 */
public class InteractionErrorHandler implements EventListener {

    static final String UNEXPECTED_FAILURE_REPLY = "처리하다가 문제가 생겼어요. 잠시 뒤에 다시 해 주세요.";

    private static final Logger log = LoggerFactory.getLogger(InteractionErrorHandler.class);

    private final EventListener command;

    public InteractionErrorHandler(EventListener command) {
        this.command = Objects.requireNonNull(command);
    }

    @Override
    public void onEvent(GenericEvent event) {
        try {
            command.onEvent(event);
        } catch (BusinessException e) {
            if (e.kind() == ErrorKind.UNAVAILABLE) {
                log.warn("바깥 서비스가 실패했습니다.", e);
            }
            replyIfNotAnswered(event, e.getMessage());
        } catch (RuntimeException e) {
            log.error("디스코드 명령을 처리하지 못했습니다: {}", command.getClass().getSimpleName(), e);
            replyIfNotAnswered(event, UNEXPECTED_FAILURE_REPLY);
        }
    }

    private static void replyIfNotAnswered(GenericEvent event, String message) {
        if (event instanceof IReplyCallback callback && !callback.isAcknowledged()) {
            callback.reply(message).setEphemeral(true).queue();
        }
    }
}
