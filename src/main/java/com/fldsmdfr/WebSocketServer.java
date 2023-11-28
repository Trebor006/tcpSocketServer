package com.fldsmdfr;

import java.io.IOException;
import java.net.ServerSocket;

public class WebSocketServer {

    private ServerSocket serverSocket;
    private int port;
    private volatile boolean started;
    private WebSocketConnectionManager connectionManager;

    public static final int DEFAULT_PORT = 8080;

    public WebSocketServer(int port) {
        this.init(port);
    }

    public WebSocketServer() {
        this.init(DEFAULT_PORT);
    }

    private void init(int port) {
        this.port = port;
        this.started = false;
        this.connectionManager = new WebSocketConnectionManager();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor WebSocket en ejecución en el puerto " + port);
            connectionManager.startManager(serverSocket);
            this.started = true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al iniciar Servidor WebSocket en el puerto " + port);
        }
    }

    public void stop() {
        this.started = false;
        connectionManager.stopManager();
    }


    public static void main(String[] args) {
        WebSocketServer webSocketServer =  new WebSocketServer();
        webSocketServer.start();
    }
}
