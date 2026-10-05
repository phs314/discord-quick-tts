package io.github.phs314.quicktts.client;

import io.github.phs314.quicktts.common.QuickChatApi;
import io.github.phs314.quicktts.common.VoiceChannelResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * 디스코드 입력창처럼 생긴 quick chat 입력 막대.
 * 왼쪽에는 문장이 읽힐 서버 아이콘과 음성 채널, 오른쪽에는 글자 수와 단축키 안내가 있다.
 */
final class InputBar {

    static final double WIDTH = 680;

    private static final String FONT = "-fx-font-family: 'Malgun Gothic';";
    private static final double SERVER_ICON_SIZE = 18;

    private final TextField input = new TextField();
    private final HBox channelChip = new HBox(6);
    private final Label countLabel = new Label();
    private final HBox bar;
    private final Map<String, Image> serverIcons = new HashMap<>();

    InputBar() {
        input.setPromptText("읽어 줄 문장을 입력하세요");
        input.setStyle("-fx-background-color: transparent; -fx-text-fill: #dbdee1; -fx-font-size: 16px;"
                + "-fx-prompt-text-fill: #80848e; -fx-padding: 0 4 0 4;" + FONT);
        HBox.setHgrow(input, Priority.ALWAYS);
        input.textProperty().addListener((observable, before, text) -> updateCount(text.length()));
        updateCount(0);

        channelChip.setAlignment(Pos.CENTER_LEFT);
        channelChip.setMaxWidth(220);

        Label keys = new Label("Enter 보내기 · Esc 닫기");
        keys.setStyle("-fx-text-fill: #80848e; -fx-font-size: 12px;" + FONT);
        keys.setMinWidth(Label.USE_PREF_SIZE);
        countLabel.setMinWidth(Label.USE_PREF_SIZE);
        HBox right = new HBox(10, countLabel, separator(), keys);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.setMinWidth(HBox.USE_PREF_SIZE);

        bar = new HBox(10, channelChip, input, right);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 12));
        bar.setPrefWidth(WIDTH);
        bar.setStyle("-fx-background-color: #383a40; -fx-background-radius: 10;");
        bar.setEffect(new DropShadow(18, 0, 4, Color.rgb(0, 0, 0, 0.35)));
        showChecking();
    }

    Node node() {
        return bar;
    }

    TextField input() {
        return input;
    }

    void showChecking() {
        setChip(null, "확인 중…", false, null);
    }

    void showChannel(Optional<VoiceChannelResponse> channel) {
        channel.ifPresentOrElse(
                found -> setChip(serverIcon(found), found.channelName(), true, found.serverName() + " · " + found.channelName()),
                () -> setChip(null, "음성 채널 없음", false, "봇이 있는 서버의 음성 채널에 들어가야 읽어 줄 수 있어요."));
    }

    void showServerUnreachable() {
        setChip(null, "서버 연결 안 됨", false, "봇 서버에 연결하지 못했어요.");
    }

    private void setChip(Node icon, String text, boolean active, String tooltip) {
        Label label = new Label(text);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setStyle("-fx-text-fill: " + (active ? "white" : "#b5bac1") + "; -fx-font-size: 12px; -fx-font-weight: bold;" + FONT);
        channelChip.getChildren().setAll(icon == null ? speakerMark(active) : icon, label);
        channelChip.setPadding(new Insets(4, 10, 4, icon == null ? 8 : 4));
        channelChip.setStyle("-fx-background-color: " + (active ? "#5865f2" : "#4e5058") + "; -fx-background-radius: 999;");
        Tooltip.install(channelChip, tooltip == null ? null : new Tooltip(tooltip));
    }

    /** 서버 아이콘을 동그랗게 잘라 보여 준다. 아이콘이 없는 서버는 이름 첫 글자로 대신한다. */
    private Node serverIcon(VoiceChannelResponse channel) {
        if (channel.serverIconUrl() == null) {
            Label initial = new Label(channel.serverName().isEmpty() ? "?" : channel.serverName().substring(0, 1));
            initial.setStyle("-fx-text-fill: #5865f2; -fx-font-size: 10px; -fx-font-weight: bold;" + FONT);
            StackPane badge = new StackPane(new Circle(SERVER_ICON_SIZE / 2, Color.WHITE), initial);
            badge.setMinSize(SERVER_ICON_SIZE, SERVER_ICON_SIZE);
            return badge;
        }
        Image image = serverIcons.computeIfAbsent(channel.serverIconUrl(),
                url -> new Image(url + "?size=64", SERVER_ICON_SIZE * 2, SERVER_ICON_SIZE * 2, true, true, true));
        ImageView view = new ImageView(image);
        view.setFitWidth(SERVER_ICON_SIZE);
        view.setFitHeight(SERVER_ICON_SIZE);
        view.setClip(new Circle(SERVER_ICON_SIZE / 2, SERVER_ICON_SIZE / 2, SERVER_ICON_SIZE / 2));
        return view;
    }

    private static Label speakerMark(boolean active) {
        Label mark = new Label("🔊");
        mark.setStyle("-fx-text-fill: " + (active ? "white" : "#b5bac1") + "; -fx-font-size: 11px;");
        return mark;
    }

    private static Label separator() {
        Label dot = new Label("|");
        dot.setStyle("-fx-text-fill: #4e5058; -fx-font-size: 12px;");
        return dot;
    }

    private void updateCount(int length) {
        countLabel.setText(length + "/" + QuickChatApi.MAX_TEXT_LENGTH);
        boolean tooLong = length > QuickChatApi.MAX_TEXT_LENGTH;
        countLabel.setStyle("-fx-text-fill: " + (tooLong ? "#f23f43" : "#80848e") + "; -fx-font-size: 12px;" + FONT);
    }
}
