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
        System.out.println("** Received connection from " + socket.getRemoteSocketAddress().toString());
        boolean done = false;

        try {
            socket.setSoTimeout(10 * 1000);

            while (!done) {
                Request request = new Request(in);
                Response response = new Response(out);

                if (!request.parse()) {
                    response.setStatusCode(Status._500);
                    response.setContentLength(0);
                    response.sendHeader();
                    break;
                }

                Method method = null;
                if ("GET".equals(request.getMethod())) {
                   method = new Get(request,response);
                } else if ("POST".equals(request.getMethod())) {
                    method = new Post(request,response);
                }else {
                    break;
                }

                method.execute();

                if ("close".equals(request.getHeader("Connection"))) {
                    done = true;
                    break;
                }
            }

        } catch (SocketTimeoutException e) {
            System.out.printf("** [%d] timeout **\n",socket.getPort());
//            e.printStackTrace();
        } catch (SocketException e) {
            System.out.printf("** [%d] client closed connection **\n", socket.getPort());
//            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}