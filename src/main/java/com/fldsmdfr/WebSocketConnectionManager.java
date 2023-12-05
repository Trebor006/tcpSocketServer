package com.fldsmdfr;

import com.fldsmdfr.event.*;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class WebSocketConnectionManager extends Thread implements EventProcessPackageListener, EventReceivePackageListener {

    private ServerSocket serverSocket;
    private volatile HashMap<String, WebSocketHandler> clients;
    private volatile boolean started;

    private EventReceivePackageManager eventReceivePackageManager; // Notificaciones del Socket Handler al Manejador de conecciones
    private EventProcessPackageManager eventProcessPackageManager; // Notificaciones del Package Handlar al Manejador de conecciones
    private EventServerManager eventServerManager; // Notificacion a la vista

    public WebSocketConnectionManager() {
        eventReceivePackageManager = new EventReceivePackageManager();
        eventReceivePackageManager.addEventListener(this);
        eventProcessPackageManager = new EventProcessPackageManager();
        eventProcessPackageManager.addEventListener(this);
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
        WebSocketHandler client = new WebSocketHandler(clientSocket, id, eventReceivePackageManager);
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

    @Override
    public void eventProcessPackageOccurred(EventProcessPackage evt) {
        DataPackage dataPackage = evt.getData();
        String id = dataPackage.getSource();
        WebSocketHandler client = clients.get(id);

        switch (dataPackage.getAction()) {
            case Protocol.ACTION_USERNAME: {
                client.userName = dataPackage.getData();

                JSONObject data = new JSONObject();
                data.put("id", id);
                data.put("userName", client.userName);
                DataPackage dataPackageSend = new DataPackage("server", null, data.toString(), Protocol.ACTION_CONNECT_CLIENT, null);
                this.sendAll(dataPackageSend, id);

                JSONObject listClients = clientsToJSONObject();
                dataPackageSend = new DataPackage("server", id, listClients.toString(), Protocol.ACTION_LIST_CLIENTS, null);
                client.send(dataPackageSend.toString());

                this.notifyEventServer("Nuevo Cliente conectado " + id + " - " + client.userName);
                break;
            }
            case Protocol.ACTION_MESSAGE: {
                String target = dataPackage.getTarget();
                this.notifyEventServer(dataPackage.toString());

                if (target.equals("server")) {
                    return;
                }

                DataPackage dataPackageSend = new DataPackage(dataPackage.getSource(), null, dataPackage.getData(), dataPackage.getAction(), null);
                if (target.equals("all")) {
                    this.sendAll(dataPackageSend, id);
                    return;
                }

                this.send(dataPackageSend, target);
                break;
            }
            case Protocol.ACTION_FILE: {
                FileInformation fileInformation = new FileInformation();
                fileInformation.toFileInformation(new JSONObject(dataPackage.getData()));
                dataPackage.setAction(Protocol.ACTION_FILE_PART);
                client.send(dataPackage.toString());
                this.notifyEventServer("Recibiendo Cabecera " + fileInformation.name + " " + "Parte " + fileInformation.partNumber + " de " + fileInformation.partsTotal + " | " + fileInformation.sizePart + " bytes a enviar " + " | " + fileInformation.sizeSend + " bytes en el server " + " | " + fileInformation.size + " bytes totales");
                break;
            }
            case Protocol.ACTION_FILE_PART: {
                FileInformation fileInformation = new FileInformation();
                fileInformation.toFileInformation(new JSONObject(dataPackage.getData()));
                client.send(dataPackage.toString());
                this.notifyEventServer("Recibiendo  " + fileInformation.name + " " + "Parte " + fileInformation.partNumber + " de " + fileInformation.partsTotal + " | " + fileInformation.sizePart + " bytes a enviar " + " | " + fileInformation.sizeSend + " bytes en el server " + " | " + fileInformation.size + " bytes totales");
                break;
            }
            case Protocol.ACTION_FILE_END: {
                FileInformation fileInformation = new FileInformation();
                fileInformation.toFileInformation(new JSONObject(dataPackage.getData()));
                client.send(dataPackage.toString());
                this.notifyEventServer("Termino de Recibir  " + fileInformation.name + " " + "Parte " + fileInformation.partNumber + " de " + fileInformation.partsTotal + " | " + fileInformation.sizePart + " bytes a enviar " + " | " + fileInformation.sizeSend + " bytes en el server " + " | " + fileInformation.size + " bytes totales");
                break;
            }
            case Protocol.ACTION_DISCONNECT_CLIENT: {
                clients.remove(id);
                DataPackage dataPackageSend = new DataPackage("server", null, "", Protocol.ACTION_DISCONNECT_CLIENT, null);
                this.sendAll(dataPackageSend, "");

                this.notifyEventServer("Cliente Desconectado " + id + " - " + client.userName);
                break;
            }
        }
    }

    public void sendAll(DataPackage dataPackageSend, String idIgnore) {
        for (var entry : clients.entrySet()) {
            if (!idIgnore.equals(entry.getKey())) {
                dataPackageSend.setTarget(entry.getKey());
                entry.getValue().send(dataPackageSend.toString());
            }
        }
    }

    public void send(DataPackage dataPackageSend, String target) {
        WebSocketHandler clientTarget = clients.get(target);
        if (clientTarget != null) {
            dataPackageSend.setTarget(target);
            clientTarget.send(dataPackageSend.toString());
        }
    }

    @Override
    public void eventReceivePackageOccurred(EventReceivePackage evt) {
        PackageHandler packageHandler = new PackageHandler(evt.getData(), this.eventProcessPackageManager);
        packageHandler.start();
    }
}
