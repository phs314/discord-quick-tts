package io.github.phs314.quicktts.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
 * 입력창과 같은 디스코드 어두운 색으로 그리는 창. 윈도우 제목 표시줄 없이 머리말·본문·아래 단추 줄을 직접 그린다(dialog.css).
 * 머리말을 끌면 창이 옮겨지고, ✕ 나 Esc 로 닫는다.
 */
final class ThemedWindow {

    /** 카드 둘레의 그림자 여백. */
    private static final double SHADOW_MARGIN = 20;
    private static final Image APP_ICON = new Image(ThemedWindow.class.getResource("app-icon.png").toExternalForm());

    private final Stage stage = new Stage(StageStyle.TRANSPARENT);
    private double dragX;
    private double dragY;

    ThemedWindow(String title, Node body, Node footer) {
        stage.setTitle("Discord Quick TTS");
        stage.getIcons().add(APP_ICON);
        stage.setResizable(false);

        body.getStyleClass().add("body");
        footer.getStyleClass().add("footer");
        VBox card = new VBox(header(title), body, footer);
        card.getStyleClass().add("card");
        card.setPrefWidth(420);
        StackPane root = new StackPane(card);
        root.setPadding(new Insets(SHADOW_MARGIN));
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(ThemedWindow.class.getResource("dialog.css").toExternalForm());
        // 글 입력란이 Esc 를 먼저 써 버리지 않도록 필터로 받는다.
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });
        stage.setScene(scene);
    }

    Scene scene() {
        return stage.getScene();
    }

    /** 창을 띄우고 닫힐 때까지 기다린다. */
    void showAndWait() {
        stage.showAndWait();
    }

    void close() {
        stage.close();
    }

    /** 왼쪽에 {@code left}(없으면 null), 오른쪽에 {@code right} 단추들을 놓은 아래 단추 줄. */
    static HBox footer(Node left, Button... right) {
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox footer = new HBox(8);
        if (left != null) {
            footer.getChildren().add(left);
        }
        footer.getChildren().add(space);
        footer.getChildren().addAll(right);
        footer.setAlignment(Pos.CENTER_LEFT);
        return footer;
    }

    /** 키 입력으로 눌리지 않도록 포커스를 받지 않는 단추. */
    static Button button(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setFocusTraversable(false);
        return button;
    }

    /** 앱 아이콘과 제목, 닫기 단추. 제목 표시줄 대신이라 끌어서 창을 옮길 수 있다. */
    private Node header(String title) {
        ImageView icon = new ImageView(APP_ICON);
        icon.setFitWidth(18);
        icon.setFitHeight(18);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button close = button("✕", "close");
        close.setOnAction(event -> close());

        HBox header = new HBox(8, icon, titleLabel, space, close);
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
}
