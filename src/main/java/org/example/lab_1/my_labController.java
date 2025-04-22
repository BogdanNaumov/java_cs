package org.example.lab_1;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.example.lab_1.server.GameServer;
import org.example.lab_1.client.GameClient;

import java.io.IOException;
import java.util.Optional;

public class my_labController {
    // Состояние игры
    private boolean play = false;
    private boolean isPaused = false;
    private Thread gameThread = null;
    private boolean isArrowFlying = false;
    private double arrowX = 0;
    private final double arrowSpeed = 60;
    private final double arrowY = 195;

    // Сетевое взаимодействие
    private GameClient client;
    private final Gson gson = new Gson();
    private String username;
    private long gameStartTime;

    // Компоненты UI
    @FXML private Label count_o;
    @FXML private Label l1;
    @FXML private Label l2;
    @FXML private Circle circle_1;
    @FXML private Circle circle_2;
    @FXML private AnchorPane mainPane;
    @FXML private Line arrow;

    @FXML
    public void initialize() {
        setupNetworkConnection();
        initializeGameState();
    }

    private void setupNetworkConnection() {
        TextInputDialog dialog = new TextInputDialog("Player1");
        dialog.setTitle("Имя игрока");
        dialog.setHeaderText("Введите ваше имя:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresentOrElse(name -> {
            username = name;
            try {
                client = new GameClient();
                client.setMessageListener(this::handleServerMessage);
                client.connect(username);
            } catch (IOException e) {
                showError("Ошибка подключения", e.getMessage());
            }
        }, () -> System.exit(0));
    }

    private void initializeGameState() {
        arrow.setVisible(false);
        updateUI(0, 0, 0);
    }

    private void handleServerMessage(String json) {
        Platform.runLater(() -> {
            try {
                if (json.contains("\"action\":\"end\"")) {
                    GameMessage message = gson.fromJson(json, GameMessage.class);
                    showGameOver(message.data);
                } else {
                    GameState state = gson.fromJson(json, GameState.class);
                    updateUI(state);
                }
            } catch (Exception e) {
                System.err.println("Ошибка обработки сообщения: " + e.getMessage());
            }
        });
    }

    private void handleGameStart(ServerMessage message) {
        gameStartTime = Long.parseLong(message.data);
        play = true;
        startGameLoop();
    }

    private void startGameLoop() {
        if (gameThread == null) {
            gameThread = new Thread(() -> {
                while (play) {
                    if (!isPaused) {
                        Platform.runLater(() -> {
                            updateTargetPositions();
                            moveArrow();
                        });
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                gameThread = null;
            });
            gameThread.start();
        }
    }

    private void updateTargetPositions() {
        long elapsedTime = System.currentTimeMillis() - gameStartTime;


        double y1 = (elapsedTime / 50) % mainPane.getHeight();
        circle_1.setLayoutY(y1);


        double y2 = (elapsedTime / 25) % mainPane.getHeight();
        circle_2.setLayoutY(y2);
    }

    private void moveArrow() {
        if (isArrowFlying) {
            arrowX += arrowSpeed;
            arrow.setLayoutX(arrowX);

            if (arrowX > mainPane.getWidth()) {
                resetArrow();
            }
        }
    }

    private void resetArrow() {
        isArrowFlying = false;
        arrow.setVisible(false);
    }

    private void updateUI(GameState_1 state) {
        // Обновление UI для текущего игрока
        state.players.stream()
                .filter(p -> p.username.equals(username))
                .findFirst()
                .ifPresent(player -> {
                    count_o.setText("Счёт: " + player.score);
                    l1.setText("Больших: " + player.bigHits);
                    l2.setText("Маленьких: " + player.smallHits);
                });
    }

    @FXML
    void vist() {
        if (!isArrowFlying && client != null) {
            isArrowFlying = true;
            arrowX = 70;
            arrow.setLayoutX(arrowX);
            arrow.setVisible(true);

            client.sendMessage(gson.toJson(new ShotMessage(username, System.currentTimeMillis())));
        }
    }

    private void resetLocalGame() {
        // Локальный сброс UI
        isArrowFlying = false;
        arrow.setVisible(false);
        arrowX = 70;
        circle_1.setLayoutY(0);
        circle_2.setLayoutY(0);
        updateUI(0, 0, 0);
    }

    public static class ClientMessage {
        public String action; // Делаем поле публичным
        public String username;

        public ClientMessage(String action, String username) {
            this.action = action;
            this.username = username;
        }
    }

    @FXML
    void start() {
        if (client != null) {
            client.sendMessage(gson.toJson(new ClientMessage("ready", username)));
        }
    }

    @FXML
    void stop() {
        play = false;
        resetLocalGame();
        if (client != null) {
            client.sendMessage(gson.toJson(new ClientMessage("stop", username)));
        }
    }

    @FXML
    void pause() {
        isPaused = true;
        if (client != null) {
            client.sendMessage(gson.toJson(new ClientMessage("pause", username)));
        }
    }

    @FXML
    void resume() {
        isPaused = false;
        if (client != null) {
            client.sendMessage(gson.toJson(new ClientMessage("resume", username)));
        }
    }

    private void showGameOver(String winner) {
        play = false;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Игра завершена");
        alert.setHeaderText("Победитель: " + winner);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private static class ServerMessage {
        String action;
        String data;
        int playerScore;
        int bigHits;
        int smallHits;
        String winner;
    }

    private static class ShotMessage {
        String action = "shot";
        String username;
        long timestamp;

        public ShotMessage(String username, long timestamp) {
            this.username = username;
            this.timestamp = timestamp;
        }
    }



}