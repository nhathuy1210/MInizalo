package server;

import java.net.*;
import java.io.*;
import java.util.*;
import client.model.Message;
import client.model.User;

public class Server {
    private static final int PORT = 9999;
    private static Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);
                ClientHandler clientHandler = new ClientHandler(socket, clients);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    public static void broadcastOnlineStatus(int userId, boolean status) {
        User user = new User(userId, "", null);
        user.setOnline(status);
        
        synchronized(clients) {
            for (ClientHandler client : clients) {
                if (client.getUser().getId() != userId) {
                    client.sendUserStatus(user);
                }
            }
        }
    }

    private static void sendOnlineUsersTo(ClientHandler newClient) {
        synchronized(clients) {
            for (ClientHandler client : clients) {
                if (client != newClient && client.getUser() != null) {
                    User user = new User(client.getUser().getId(), "", null);
                    user.setOnline(true);
                    newClient.sendUserStatus(user);
                }
            }
        }
    }

    public static void forwardMessage(Message message) {
        synchronized(clients) {
            for (ClientHandler client : clients) {
                if (client.getUser().getId() == message.getReceiverId()) {
                    client.sendMessage(message);
                    break;
                }
            }
        }
    }

    public static boolean isUserOnline(int userId) {
        synchronized(clients) {
            for (ClientHandler client : clients) {
                if (client.getUser().getId() == userId) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getOnlineCount() {
        return clients.size();
    }

    public static Set<Integer> getOnlineUserIds() {
        Set<Integer> userIds = new HashSet<>();
        synchronized(clients) {
            for (ClientHandler client : clients) {
                userIds.add(client.getUser().getId());
            }
        }
        return userIds;
    }
}
