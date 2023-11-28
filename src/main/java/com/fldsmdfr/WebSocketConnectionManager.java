package com.fldsmdfr;

import com.fldsmdfr.event.*;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class WebSocketConnectionManager extends Thread implements MyEventListener {

    private ServerSocket serverSocket;
    private volatile HashMap<String, WebSocketHandler> clients;
    private volatile boolean started;

    private MyClassEventManager myClassEventManager; // Notificaciones del Cliente al Manejador de conecciones
    private EventServerManager eventServerManager; // Notificacion a la vista

    public WebSocketConnectionManager() {
        myClassEventManager = new MyClassEventManager();
        myClassEventManager.addMyEventListener(this);
    }

    public void setEventServerManager(EventServerManager eventServerManager) {
        this.eventServerManager = eventServerManager;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (!started) {
                    return;
                }

                String id = clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort();
                acceptClient(clientSocket, id);
            }
        } catch (IOException ex) {

        }
    }

    private void acceptClient(Socket clientSocket, String id) {
        WebSocketHandler client = new WebSocketHandler(clientSocket, id, myClassEventManager);
        System.out.println("Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress());
        clients.put(id, client);
        client.start();
    }

    public void startManager(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.clients = new HashMap<>();
        this.started = true;
        this.start();
    }

    public void stopManager() {
        this.started = false;
    }

    @Override
    public void myEventOccurred(MyEvent evt) {
        JSONObject data = evt.getData();
        String action = data.getString("action");
        String id = data.getString("id");
        WebSocketHandler client = clients.get(id);
        switch (action) {
            case WebSocketHandler.ACTION_USERNAME: {
                JSONObject listClients = new JSONObject();
                for (var entry : clients.entrySet()) {
                    listClients.put(entry.getKey(), entry.getValue().userName);
                    if (!id.equals(entry.getKey())) {
                        entry.getValue().sendConnectClient(id, client.userName);
                    }
                }
                client.sendListClients(listClients.toString());
                this.notifyEventServer("Nuevo Cliente conectado " + id + " - " + client.userName);
                break;
            }
            case WebSocketHandler.ACTION_MESSAGE: {
                String target = data.getString("target");
                String message = data.getString("message");
                this.notifyEventServer(data.toString());
                if (target.equals("server")) {
                    return;
                }
                if (target.equals("all")) {
                    for (var entry : clients.entrySet()) {
                        if (!id.equals(entry.getKey())) {
                            entry.getValue().sendMessage(id, target, message);
                        }
                    }
                }

                WebSocketHandler clientTarget = clients.get(target);
                if (clientTarget != null) {
                    clientTarget.sendMessage(id, target, message);

                }
                break;
            }
            case WebSocketHandler.ACTION_FILE: {
                this.notifyEventServer(data.toString());
//                String target = data.getString("target");
//                String message = data.getString("message");
//                if (target.equals("server")) {
//                    return;
//                }
//                if (target.equals("all")) {
//                    for (var entry : clients.entrySet()) {
//                        if (!id.equals(entry.getKey())) {
//                            entry.getValue().sendMessage(id, target, message);
//                        }
//                    }
//                }
//
//                WebSocketHandler clientTarget = clients.get(target);
//                if (clientTarget != null) {
//                    clientTarget.sendMessage(id, target, message);
//
//                }
                break;
            }
            case WebSocketHandler.ACTION_DISCONNECT_CLIENT: {
                clients.remove(id);
                for (var entry : clients.entrySet()) {
                    if (!id.equals(entry.getKey())) {
                        entry.getValue().sendDisconnectClient(id);
                    }
                }
                this.notifyEventServer("Cliente Desconectado " + id + " - " + client.userName);
                break;
            }
        }
    }

    public JSONObject clientsToJSONObject() {
        JSONObject listClients = new JSONObject();
        for (var entry : clients.entrySet()) {
            listClients.put(entry.getKey(), entry.getValue().userName);
        }
        return listClients;
    }


    public void notifyEventServer(String log) {
        if (eventServerManager == null) {
            return;
        }
        JSONObject data = new JSONObject();
        data.put("isStarted", this.started);
        data.put("clientsConnected", this.clientsToJSONObject());
        data.put("log", log);
        eventServerManager.fireEventServer(new EventServer(this, data));
    }
}
