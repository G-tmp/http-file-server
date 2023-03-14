package com.alpha.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServer {
    private final static int DEFAULT_PORT = 8888;
    private final static int CORES = Runtime.getRuntime().availableProcessors();
    private final ExecutorService pool = Executors.newFixedThreadPool(CORES * 2);
    private final int port;
//    private Map<String, Map<String, Handler>> handlers = new HashMap<String, Map<String, Handler>>();


    public HttpServer(int port) {
        this.port = port;
    }


    public void start() {
        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(port);
            System.out.println("Listening on port " + port);
            Socket connectionSocket = null;

            while ((connectionSocket = welcomeSocket.accept()) != null) {
                SocketThread handler = new SocketThread(connectionSocket);
                pool.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int getValidPort(String[] args) throws NumberFormatException {
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
