package org.example.lab_1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class my_labController {
    boolean play = false;
    boolean isPaused = false;
    Thread t = null;
    int cou = 0;
    int c1 = 0;
    int c2 = 0;


    boolean isArrowFlying = false;
    double arrowSpeed = 40;
    double arrowX = 0;
    double arrowY = 195;

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

    @FXML
    private Line arrow;

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

    void moveArrow() {
        if (isArrowFlying) {
            arrowX += arrowSpeed;
            arrow.setLayoutX(arrowX);


            if (circle_1.getBoundsInParent().contains(arrowX, arrowY)) {
                cou += 10;
                c1 += 1;
                updateCountLabel1();
                updateCountLabel2();
                resetArrow();
            } else if (circle_2.getBoundsInParent().contains(arrowX, arrowY)) {
                cou += 20;
                c2 += 1;
                updateCountLabel1();
                updateCountLabel3();
                resetArrow();
            }
            if (arrowX > mainPane.getWidth()) {
                resetArrow();
            }
        }
    }

    void resetArrow() {
        isArrowFlying = false;
        arrow.setVisible(false);
    }

    @FXML
    void vist() {
        if (!isArrowFlying) {
            isArrowFlying = true;
            arrowX = 70;
            arrowY = 195;
            arrow.setLayoutX(arrowX);
            arrow.setLayoutY(arrowY);
            arrow.setVisible(true);
        }
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
                                    moveArrow(); 
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
            c1 = 0;
            c2 = 0;
            updateCountLabel1();
            updateCountLabel2();
            updateCountLabel3();
            play = false;
            t = null;
            resetArrow();
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