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

    public SocketThread(Socket socket) {
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

                Method method = null;
                if ("GET".equals(request.getMethod())) {
                   method = new Get(request,response);
                } else if ("POST".equals(request.getMethod())) {
                    method = new Post(request,response);
                }
                method.execute();

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
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}