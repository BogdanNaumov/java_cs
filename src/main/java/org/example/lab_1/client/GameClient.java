package org.example.lab_1.client;

import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();
    private MessageListener messageListener;

    public interface MessageListener {
        void onMessage(String message);
    }

    public void connect(String username) throws IOException {
        socket = new Socket("localhost", 3214);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        sendMessage(gson.toJson(new ConnectMessage(username))); // Сериализация

        new Thread(this::receiveMessages).start();
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (messageListener != null) {
                    messageListener.onMessage(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении сообщений: " + e.getMessage());
        }
    }

    public void sendMessage(String json) {
        out.println(json);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Ошибка при отключении: " + e.getMessage());
        }
    }

}