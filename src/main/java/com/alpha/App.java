package com.alpha;


import com.alpha.server.HttpServer;


public class App {

    public static void main(String[] args) {
//        HttpServer server = new HttpServer(HttpServer.getValidPortParam(args));
        HttpServer server = new HttpServer(8888);
        server.start();
    }
}

