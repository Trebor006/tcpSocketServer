package com.fldsmdfr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WebSocketHandler extends Thread {
  private final Socket clientSocket;
  private PrintWriter out;
  private BufferedReader in;

  public WebSocketHandler(Socket socket) {
    this.clientSocket = socket;
  }

  public void run() {
    try {
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        System.out.println("Mensaje recibido del cliente: " + inputLine);
        // Puedes procesar los mensajes aquí y enviar respuestas si es necesario.

      }

      in.close();
      out.close();
      clientSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
