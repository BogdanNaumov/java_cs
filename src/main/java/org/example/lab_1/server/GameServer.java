package org.example.lab_1.server;

import com.google.gson.Gson;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    private static final int PORT = 3214;
    private static final double MAX_HEIGHT = 400;
    private static final double TARGET_POSITION = 195;
    private static final double TOLERANCE = 10;

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final GameState gameState = new GameState();
    private final Gson gson = new Gson();
    private boolean isGameRunning = false;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    public void checkWinner() {
        for (Player player : gameState.players) {
            if (player.score >= 6) {
                broadcast(gson.toJson(new GameMessage("end", player.username)));
                resetGame();
                break;
            }
        }
    }

    private void resetGame() {
        gameState.players.forEach(p -> {
            p.score = 0;
            p.shots = 0;
            p.bigHits = 0;
            p.smallHits = 0;
        });
        isGameRunning = false;
    }

    // Вложенные классы
    static class GameState {
        long gameStartTime;
        List<Player> players = new ArrayList<>();
    }

    static class Player {
        String username;
        int score;
        int shots;
        int bigHits;
        int smallHits;
    }

    class ClientHandler implements Runnable {
        private final Socket socket;
        private final GameServer server;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);

                String message;
                while ((message = in.readLine()) != null) {
                    GameMessage msg = server.gson.fromJson(message, GameMessage.class);
                    switch (msg.action) {
                        case "connect":
                            handleConnect(msg.username);
                            break;
                        case "shot":
                            handleShot(msg);
                            break;
                        case "ready":
                            startGameIfAllReady();
                            break;
                    }
                }
            } catch (IOException e) {
                server.clients.remove(this);
            }
        }

        private void handleConnect(String username) {
            if (server.gameState.players.stream().anyMatch(p -> p.username.equals(username))) {
                send(server.gson.toJson(new GameMessage("error", "Имя занято")));
                return;
            }
            this.username = username;
            Player player = new Player();
            player.username = username;
            server.gameState.players.add(player);
            send(server.gson.toJson(new GameMessage("connected", "")));
        }

        private void handleShot(ClientHandler client, long shotTime) {
            Player player = findPlayer(client.username);
            if (player == null || !isGameRunning) return;

            // Проверка попаданий
            boolean hitBig = checkHit(shotTime, 50); // 50ms/step для большой мишени
            boolean hitSmall = checkHit(shotTime, 25); // 25ms/step для маленькой

            if (hitBig || hitSmall) {
                player.score += hitBig ? 10 : 20;
                player.shots++;
                if (hitBig) player.bigHits++;
                else player.smallHits++;

                // Проверка победы
                if (player.score >= 6) {
                    broadcast(gson.toJson(new GameMessage("end", player.username)));
                    resetGame();
                } else {
                    broadcast(gson.toJson(gameState));
                }
            }
        }

        private boolean checkHit(long shotTime, int speed) {
            long elapsed = shotTime - gameState.gameStartTime;
            double y = (elapsed / speed) % MAX_HEIGHT;
            return Math.abs(y - TARGET_POSITION) <= TOLERANCE;
        }

        private Player findPlayer(String username) {
            return server.gameState.players.stream()
                    .filter(p -> p.username.equals(username))
                    .findFirst()
                    .orElse(null);
        }

        private void startGameIfAllReady() {
            if (server.clients.size() >= 2 && !server.isGameRunning) {
                server.gameState.gameStartTime = System.currentTimeMillis();
                server.isGameRunning = true;
                server.broadcast(server.gson.toJson(
                        new GameMessage("start", String.valueOf(server.gameState.gameStartTime))
                ));
            }
        }

        public void send(String message) {
            out.println(message);
        }
    }
}

class GameMessage {
    String action;
    String username;
    String data;
    long timestamp;

    public GameMessage(String action, String data) {
        this.action = action;
        this.data = data;
    }
}