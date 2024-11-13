package server;

import java.net.*;
import java.io.*;
import java.util.*;
import client.model.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private final Set<ClientHandler> clients;
    private User user;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, Set<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
        initializeStreams();
    }

    private void initializeStreams() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Stream initialization error: " + e.getMessage());
            closeEverything();
        }
    }

    public User getUser() {
        return user;
    }

    public void sendMessage(Message message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            System.out.println("Message send error: " + e.getMessage());
        }
    }

    public void sendUserStatus(User statusUser) {
        try {
            output.writeObject(statusUser);
            output.flush();
        } catch (IOException e) {
            System.out.println("Status broadcast error: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        if (!isActive()) return;
        
        try {
            Object userObj = input.readObject();
            if (userObj instanceof User) {
                this.user = (User) userObj;
                broadcastUserStatus(true);
                sendOnlineUsersTo(this);
            }

            while (running && isActive()) {
                Object messageObj = input.readObject();
                handleMessage(messageObj);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client handler error: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    private void handleMessage(Object messageObj) {
        if (messageObj instanceof Message) {
            Message message = (Message) messageObj;
            synchronized(clients) {
                for (ClientHandler client : clients) {
                    if (client.user != null && client.user.getId() == message.getReceiverId()) {
                        client.sendMessage(message);
                        break;
                    }
                }
            }
        }
    }

    private void broadcastUserStatus(boolean online) {
        if (user != null) {
            user.setOnline(online);
            synchronized(clients) {
                for (ClientHandler client : clients) {
                    if (client != this && client.isActive()) {
                        client.sendUserStatus(user);
                    }
                }
            }
        }
    }

    private void sendOnlineUsersTo(ClientHandler newClient) {
        synchronized(clients) {
            for (ClientHandler client : clients) {
                if (client != newClient && client.user != null) {
                    User onlineUser = new User(client.user.getId(), client.user.getUsername(), null);
                    onlineUser.setOnline(true);
                    newClient.sendUserStatus(onlineUser);
                }
            }
        }
    }

    private boolean isActive() {
        return running && socket != null && !socket.isClosed() && output != null && input != null;
    }

    private void closeEverything() {
        running = false;
        broadcastUserStatus(false);
        
        synchronized(clients) {
            clients.remove(this);
        }
        
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("Resource closure error: " + e.getMessage());
        }
    }
}
