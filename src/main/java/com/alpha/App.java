package com.alpha;


import com.alpha.server.HttpServer;


public class App {

    public static void main(String[] args) {
        HttpServer server = new HttpServer(HttpServer.getValidPort(args));
        server.start();
    }
}

