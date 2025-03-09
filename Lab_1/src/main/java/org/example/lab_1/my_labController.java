package org.example.lab_1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class my_labController {
    boolean play = false;
    boolean isPaused = false;
    Thread t = null;
    int cou = 0;
    int c1=0;
    int c2=0;

    @FXML
    private Label count_o;

    @FXML
    private Label l1;

    @FXML
    private Label l2;

    @FXML
    private Circle circle_1;

    @FXML
    private Circle circle_2;

    @FXML
    private AnchorPane mainPane;

    private void updateCountLabel1() {
        count_o.setText("Счёт: " + cou);
    }

    private void updateCountLabel2() {
        l1.setText("Попаданий по большой: " + c1);
    }

    private void updateCountLabel3() {
        l2.setText("Попаданий по маленькой: " + c2);
    }

    void moveCircle1() {
        if (mainPane != null) {
            double height = mainPane.getHeight();
            double y = circle_1.getLayoutY();
            y += 10;
            if (y > height) {
                circle_1.setLayoutY(0);
            } else {
                circle_1.setLayoutY(y);
            }
        } else {
            System.err.println("Ошибка: mainPane не инициализирован!");
        }
    }

    void moveCircle2() {
        if (mainPane != null) {
            double height = mainPane.getHeight();
            double y = circle_2.getLayoutY();
            y += 50;
            if (y > height) {
                circle_2.setLayoutY(0);
            } else {
                circle_2.setLayoutY(y);
            }
        } else {
            System.err.println("Ошибка: mainPane не инициализирован!");
        }
    }

    @FXML
    void vist() {
        if (circle_1.getLayoutY() >= 180 && circle_1.getLayoutY() <= 220) {
            cou += 10;
            c1+=1;
            updateCountLabel1();
            updateCountLabel2();
            changeCircleColor1();
        } else if (circle_2.getLayoutY() >= 180 && circle_2.getLayoutY() <= 220) {
            cou += 20;
            c2+=1;
            updateCountLabel1();
            updateCountLabel3();
            changeCircleColor2();
        }
    }

    private void changeCircleColor1() {
        Platform.runLater(() -> {
            circle_1.setFill(Color.RED);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("Ошибка при задержке!");
                }
                Platform.runLater(() -> circle_1.setFill(Color.DODGERBLUE));
            }).start();
        });
    }

    private void changeCircleColor2() {
        Platform.runLater(() -> {
            circle_2.setFill(Color.RED);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("Ошибка при задержке!");
                }
                Platform.runLater(() -> circle_2.setFill(Color.DODGERBLUE));
            }).start();
        });
    }

    @FXML
    void start() {
        if (t == null) {
            t = new Thread(
                    () -> {
                        play = true;
                        while (play) {
                            if (!isPaused) {
                                Platform.runLater(() -> {
                                    moveCircle1();
                                    moveCircle2();
                                });
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                System.err.println("Поток прерван!");
                                break;
                            }
                        }
                        t = null;
                    }
            );
            t.start();
        }
    }

    @FXML
    void stop() {
        if (t != null) {
            t.interrupt();
            circle_1.setLayoutY(50);
            circle_2.setLayoutY(50);
            cou = 0;
            c1=0;
            c2=0;
            updateCountLabel1();
            updateCountLabel2();
            updateCountLabel3();
            play = false;
            t = null;
        }
    }

    @FXML
    void pause() {
        isPaused = true;
    }

    @FXML
    void resume() {
        isPaused = false;
    }
}