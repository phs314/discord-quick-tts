package io.github.phs314.quicktts.client;

import java.util.Optional;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 트레이 메뉴의 Change Shortcut... 으로 여는 창. 새 단축키를 직접 눌러서 정한다.
 * 키 입력은 {@link GlobalHotkey} 가 받아 {@link #onPressed(Hotkey)} 로 넘겨 준다.
 * 입력창과 같은 디스코드 어두운 색으로 그리려고 윈도우 제목 표시줄 없이 직접 그린다(hotkey-dialog.css).
 */
final class HotkeyDialog {

    /** 카드 둘레의 그림자 여백. */
    private static final double SHADOW_MARGIN = 20;
    private static final PseudoClass NEW = PseudoClass.getPseudoClass("new");
    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private static final PseudoClass WARNING = PseudoClass.getPseudoClass("warning");
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private final Stage stage = new Stage(StageStyle.TRANSPARENT);
    private final Label tag = new Label();
    private final HBox keys = new HBox(6);
    private final VBox keyBox = new VBox(10, tag, keys);
    private final Label message = new Label();
    private final Button saveButton = button("저장", "save");
    private Hotkey chosen;
    private Hotkey result;
    private double dragX;
    private double dragY;

    HotkeyDialog(Hotkey current) {
        stage.setTitle("Discord Quick TTS");
        stage.getIcons().add(new Image(HotkeyDialog.class.getResource("app-icon.png").toExternalForm()));
        stage.setResizable(false);

        VBox card = new VBox(header(), body(), footer());
        card.getStyleClass().add("card");
        card.setPrefWidth(420);
        StackPane root = new StackPane(card);
        root.setPadding(new Insets(SHADOW_MARGIN));
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(HotkeyDialog.class.getResource("hotkey-dialog.css").toExternalForm());
        // 누른 키는 GlobalHotkey 로 받으니, 창 안에서는 Space 로 버튼이 눌리는 일 없게 키 입력을 막는다. Esc 만 닫기로 둔다.
        scene.addEventFilter(KeyEvent.ANY, event -> {
            boolean plainEscape = event.getCode() == KeyCode.ESCAPE && !event.isControlDown()
                    && !event.isAltDown() && !event.isShiftDown() && !event.isMetaDown();
            if (!plainEscape) {
                event.consume();
            } else if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                stage.close();
            }
        });
        stage.setScene(scene);

        display(current, "지금 쓰는 단축키예요. 바꾸려면 새 조합을 누르세요.", false);
    }

    /** 창을 띄우고 닫힐 때까지 기다린다. 저장을 눌렀을 때만 새 단축키가 있다. */
    Optional<Hotkey> showAndWait() {
        stage.showAndWait();
        return Optional.ofNullable(result);
    }

    /** 누른 키 조합을 보여 준다. 어느 스레드에서 불러도 된다. */
    void onPressed(Hotkey hotkey) {
        Platform.runLater(() -> {
            if (hotkey.validationProblem().isPresent()) {
                chosen = null;
                display(hotkey, hotkey.validationProblem().get(), false);
            } else {
                chosen = hotkey;
                display(hotkey, hotkey.win()
                        ? "저장을 누르면 바로 바뀌어요. Win 이 들어간 조합은 윈도우가 먼저 가로챌 수 있어요."
                        : "저장을 누르면 바로 바뀌어요.", true);
            }
        });
    }

    /** 앱 아이콘과 제목, 닫기 단추. 제목 표시줄 대신이라 끌어서 창을 옮길 수 있다. */
    private Node header() {
        ImageView icon = new ImageView(stage.getIcons().getFirst());
        icon.setFitWidth(18);
        icon.setFitHeight(18);
        Label title = new Label("단축키 바꾸기");
        title.getStyleClass().add("title");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button close = button("✕", "close");
        close.setOnAction(event -> stage.close());

        HBox header = new HBox(8, icon, title, space, close);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setOnMousePressed(event -> {
            dragX = stage.getX() - event.getScreenX();
            dragY = stage.getY() - event.getScreenY();
        });
        header.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + dragX);
            stage.setY(event.getScreenY() + dragY);
        });
        return header;
    }

    private Node body() {
        Label prompt = new Label("입력창을 열 새 단축키를 눌러 주세요.");
        prompt.getStyleClass().add("prompt");
        tag.getStyleClass().add("key-tag");
        keys.setAlignment(Pos.CENTER_LEFT);
        keyBox.getStyleClass().add("key-box");
        message.getStyleClass().add("message");
        message.setWrapText(true);
        // 안내가 두 줄이 되어도 창 크기가 그대로이도록 처음부터 두 줄 자리를 잡아 둔다.
        message.setMinHeight(34);
        message.setAlignment(Pos.TOP_LEFT);

        VBox body = new VBox(10, prompt, keyBox, message);
        body.getStyleClass().add("body");
        return body;
    }

    private Node footer() {
        Button reset = button("기본값으로 (" + Hotkey.DEFAULT + ")", "reset");
        reset.setOnAction(event -> onPressed(Hotkey.DEFAULT));
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button cancel = button("취소", "cancel");
        cancel.setOnAction(event -> stage.close());
        saveButton.setOnAction(event -> {
            result = chosen;
            stage.close();
        });

        HBox footer = new HBox(8, reset, space, cancel, saveButton);
        footer.getStyleClass().add("footer");
        footer.setAlignment(Pos.CENTER_LEFT);
        return footer;
    }

    private void display(Hotkey hotkey, String text, boolean canSave) {
        boolean invalid = hotkey.validationProblem().isPresent();
        tag.setText(canSave || invalid ? "새 단축키" : "지금 단축키");
        keys.getChildren().clear();
        for (String name : hotkey.keys()) {
            if (!keys.getChildren().isEmpty()) {
                keys.getChildren().add(styled(new Label("+"), "key-plus"));
            }
            keys.getChildren().add(styled(new Label(name), "keycap"));
        }
        keyBox.pseudoClassStateChanged(NEW, canSave);
        keyBox.pseudoClassStateChanged(INVALID, invalid);
        message.setText(text);
        message.pseudoClassStateChanged(ERROR, invalid);
        message.pseudoClassStateChanged(WARNING, canSave && hotkey.win());
        saveButton.setDisable(!canSave);
    }

    /** 단축키를 누르다가 Space 나 Enter 로 눌리지 않도록 단추는 포커스를 받지 않는다. */
    private static Button button(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setFocusTraversable(false);
        return button;
    }

    private static <T extends Parent> T styled(T node, String styleClass) {
        node.getStyleClass().add(styleClass);
        return node;
    }
}
