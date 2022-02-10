package com.alpha.server;

import com.alpha.httpRequest.Request;
import com.alpha.httpRequest.method.Get;
import com.alpha.httpRequest.method.Method;
import com.alpha.httpRequest.method.Post;
import com.alpha.httpResponse.Response;
import com.alpha.httpResponse.Status;


import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;



public class SocketThread implements Runnable {
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public SocketThread(Socket socket) throws IOException {
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }


    @Override
    public void run() {
        boolean done = false;
        Request request = null;
        Response response = null;

        try {
            socket.setSoTimeout(180 * 1000);

            while (!done) {
                request = new Request(in);
                response = new Response(out);

                if (!request.parse()) {
                    response.setStatusCode(Status._500);
                    response.sendHeader();
                    return;
                }

                Method method = null;
                if ("GET".equals(request.getMethod())) {
                   method = new Get(request,response);
                } else if ("POST".equals(request.getMethod())) {
                    method = new Post(request,response);
                }
                method.execute();

                if ("close".equals(request.getHeader("Connection"))) {
                    done = true;
                    System.out.println("** client close connection **");
                    continue;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.printf("** %d timeout **",socket.getPort());
//            e.printStackTrace();
        } catch (SocketException e) {
            System.out.println("** Connection reset **");
//            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("** IOException  **");
//            e.printStackTrace();
        } finally {
            if (!socket.isClosed()){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}