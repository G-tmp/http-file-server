package com.alpha.server;

import com.alpha.request.HttpRequest;
import com.alpha.request.method.Get;
import com.alpha.request.method.HttpMethod;
import com.alpha.request.method.Post;
import com.alpha.response.HttpResponse;
import com.alpha.response.Status;
import com.alpha.utils.HTMLMaker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;


public class SocketThread implements Runnable, Constants {
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
        System.out.println("++ Received connection from " + socket.getRemoteSocketAddress().toString());
        boolean done = false;

        try {
            socket.setSoTimeout(TIMEOUT * 1000);

            while (!done) {
                HttpRequest request = new HttpRequest(in);
                HttpResponse response = new HttpResponse(out);

                if (!request.parse()) {
                    response.setStatusCode(Status._500);
                    String body = HTMLMaker._500();
                    response.setContentLength(body.getBytes().length);
                    response.setBody(body);
                    response.send();
                    return;
                }

                HttpMethod method = null;
                if ("GET".equals(request.getMethod())) {
                    method = new Get(request, response);
                } else if ("POST".equals(request.getMethod())) {
                    method = new Post(request, response);
                } else {
                    throw new RuntimeException("Unsupported HTTP method");
                }

                method.execute();

                if ("close".equals(request.getHeader("Connection"))) {
                    done = true;
                }

            }

        } catch (SocketTimeoutException e) {
            System.out.printf("-- [%d] timeout **\n", socket.getPort());
//            e.printStackTrace();
        } catch (SocketException e) {
            System.out.printf("-- [%d] client closed connection **\n", socket.getPort());
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