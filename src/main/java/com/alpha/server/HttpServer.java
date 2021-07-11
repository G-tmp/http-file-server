package com.alpha.server;

import com.alpha.handler.Handler;
import com.alpha.handler.SocketHandler;


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
                System.out.println("** Received connection from " + clientSocket.getRemoteSocketAddress().toString());
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


    public void addHandler(String method, String path, Handler handler)  {
        Map<String, Handler> methodHandlers = handlers.get(method);
        if (methodHandlers == null)  {
            methodHandlers = new HashMap<String, Handler>();
            handlers.put(method, methodHandlers);
        }
        methodHandlers.put(path, handler);
    }

}
