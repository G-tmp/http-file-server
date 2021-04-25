package com.alpha.server;

import com.alpha.handler.Handler;
import com.alpha.handler.SocketHandler;
import com.alpha.response.ContentType;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServer {
    private int port;
    private ExecutorService pool = Executors.newFixedThreadPool(8);
    private static int DEFAULT_PORT = 8888;


    public HttpServer(int port) {
        this.port = port;
    }


    public void start() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Listening on port " + port);
            Socket clientSocket;

            while ((clientSocket = serverSocket.accept()) != null) {
                System.out.println("Received connection from " + clientSocket.getRemoteSocketAddress().toString());
                SocketHandler handler = new SocketHandler(clientSocket);
                pool.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int getValidPortParam(String args[]) throws NumberFormatException {
        if (args.length > 0) {
            int port = Integer.parseInt(args[0]);
            if (port > 0 && port < 65535) {
                return port;
            } else {
                throw new NumberFormatException("Invalid port! Port value is a number between 0 and 65535");
            }
        }

        return DEFAULT_PORT;
    }




}