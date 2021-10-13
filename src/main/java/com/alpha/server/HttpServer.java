package com.alpha.server;

import com.alpha.handler.Handler;


import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServer {
    private int port;
    private ExecutorService pool = Executors.newFixedThreadPool(8);
    private static int DEFAULT_PORT = 8888;
    private Map<String, Map<String, Handler>> handlers = new HashMap<String, Map<String, Handler>>();
    public final static String HOME = System.getProperty("user.home");

    public HttpServer(int port) {
        this.port = port;
    }


    public void start() {
        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(port);
            System.out.println("Listening on port " + port);
            Socket connectionSocket;

            while ((connectionSocket = welcomeSocket.accept()) != null) {
                System.out.println("** Received connection from " + connectionSocket.getRemoteSocketAddress().toString());
                SocketThread handler = new SocketThread(connectionSocket);

                pool.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int getValidPort(String args[]) throws NumberFormatException {
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
