package com.alpha.handler;

import com.alpha.request.Request;
import com.alpha.response.Response;
import com.alpha.response.Status;


import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;



public class SocketHandler implements Runnable {
    private Socket socket;

    public SocketHandler(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {
        OutputStream out = null;
        InputStream in = null;

        try {
            socket.setSoTimeout(6 * 1000);
            boolean done = false;

            in = socket.getInputStream();
            out = socket.getOutputStream();

            while (!done) {
                Request request = new Request(in);
                Response response = new Response(out);

                if (!request.parse()) {
                    response.setStatusCode(Status._500);
                    response.send();
                    System.out.println("** parse exception");
                    return;
                }

                if ("GET".equals(request.getMethod())) {
                    MethodHandler.doGet(request, response);
                } else if ("POST".equals(request.getMethod())) {
                    MethodHandler.doPost(request, response);
                }

                if ("close".equals(request.getHeader("Connection"))) {
                    done = true;
                    System.out.println("** connect close **");
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("** timeout **");
//            e.printStackTrace();
        } catch (SocketException e) {
            System.out.println("** Connection reset **");
//            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}