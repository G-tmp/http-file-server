package com.alpha.httpRequest.method;

import com.alpha.httpRequest.Cookie;
import com.alpha.httpRequest.Request;
import com.alpha.httpResponse.ContentType;
import com.alpha.httpResponse.Response;
import com.alpha.httpResponse.Status;
import com.alpha.server.HttpServer;
import com.alpha.utils.HTMLMaker;

import java.io.*;


public class Get implements Method {
    private Request request;
    private Response response;


    public Get(Request request, Response response) {
        this.request = request;
        this.response = response;
    }


    @Override
    public void execute() throws IOException {
        Status statusCode = Status.getStatusCode(request);

        switch (statusCode) {
            case _404:
                response.setStatusCode(Status._404);
                response.setContentLength(0);
                response.sendHeader();
                break;
            case _403:
                response.setStatusCode(Status._403);
                response.setContentLength(0);
                response.sendHeader();
                break;
            case _301:
            case _302:
                response.redirect(request.getPath() + "/");
                break;
            case _206:
                // for seeking audio and video
                String range = request.getHeader("Range");
                File file = new File(HttpServer.HOME, request.getPath());
                long fsize = file.length();
                long len = 0;

                if (range != null && range.contains("bytes")) {
                    long start;
                    long end;
                    long sendSize = 1000 * 1000 * 5;

                    try {
                        start = Long.parseLong(range.substring(range.indexOf("=") + 1, range.indexOf("-")));
                    } catch (NumberFormatException e) {
                        start = 0;
                    }

                    try {
                        end = Long.parseLong(range.substring(range.indexOf("-") + 1));
                    } catch (NumberFormatException e) {
                        end = 0;
                    }

                    if (start < 0 || start > fsize || end < 0 || (end < start && end > 0)) {
                        response.setContentLength(0);
                        response.setStatusCode(Status._416);
                        response.addHeader("Content-Range", String.format("bytes */%d", fsize));
                        response.sendHeader();
                        break;
                    }

                    if (end == 0) {
                        if (fsize > start + sendSize) {
                            end = start + sendSize - 1;
                        } else {
                            end = fsize - 1;
                        }
                    } else if (end > fsize) {
                        end = fsize - 1;
                    }

                    len = end - start + 1;
                    response.setStatusCode(Status._206);
                    response.addHeader("Content-Range", String.format("bytes %d-%d/%d", start, end, fsize));
                    response.guessContentType(request.getPath());
                    response.setContentLength(len);
                    response.sendHeader();

                    byte[] b = new byte[HttpServer.RESPONSE_SIZE];
                    int c = 0;
                    long read = 0;
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                    long a = bis.skip(start);

                    while (read < len) {
                        c = bis.read(b, 0, read + b.length > len ? (int) (len - read) : b.length);
                        if (c == -1)
                            break;

                        read += c;
                        response.sendBody(b, 0, c);
                    }

                    bis.close();
                }
                break;
            case _200:
                File localFile = new File(HttpServer.HOME, request.getPath());
                // html
                if (localFile.isDirectory()) {

                    // check parameter showHidden, then redirect
                    String showHidden = request.getParameter("showHidden");
                    if (showHidden != null) {
                        try {
                            int i = Integer.parseInt(showHidden);

                            if (i == 0) {
                                Cookie cookie = new Cookie("showHidden", String.valueOf(0));
                                cookie.setMaxAge(60 * 60);
                                cookie.setPath("/");
                                response.addCookie(cookie);
                                response.redirect(request.getPath());
                            } else {
                                Cookie cookie = new Cookie("showHidden", String.valueOf(1));
                                cookie.setMaxAge(60 * 60);
                                cookie.setPath("/");
                                response.addCookie(cookie);
                                response.redirect(request.getPath());
                            }

                        } catch (NumberFormatException e) {
                            // Parameter showHidden is null or not a number
                            // Do not set cookie
                            Cookie cookie = new Cookie("showHidden", String.valueOf(0));
                            cookie.setMaxAge(60 * 60);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                            response.redirect(request.getPath());
                        }
                    }


                    // check cookie
                    String html = null;
                    String cookie = request.getCookie("showHidden");
                    if (cookie != null) {
                        try {
                            int i = Integer.parseInt(cookie);
                            if (i == 0) {
                                html = mappingLocal(request.getPath(), 0);
                            } else {
                                html = mappingLocal(request.getPath(), 1);
                            }
                        } catch (NumberFormatException e) {
                            html = mappingLocal(request.getPath(), 0);
                        }
                    } else {
                        html = mappingLocal(request.getPath(), 0);
                    }


                    response.setStatusCode(Status._200);
                    response.enableChunked();
                    response.setContentType(ContentType.HTML);
//                    response.setContentLength(html.getBytes().length);
                    response.sendHeader();
                    response.sendChunkedFin(html.getBytes());

                } else {   // file

                    // check parameter download
                    String download = request.getParameter("download");
                    if (download != null){
                        response.addHeader("Content-Disposition","attachment;");
                    }

                    response.setStatusCode(Status._200);
                    response.enableChunked();
                    response.guessContentType(request.getPath());
                    response.sendHeader();

                    byte[] buffer = new byte[HttpServer.RESPONSE_SIZE];
                    int count = 0;
                    BufferedInputStream  bis = new BufferedInputStream(new FileInputStream(localFile));

                    while ((count = bis.read(buffer)) != -1) {
                        response.sendChunked(buffer, 0, count);
                    }
                    response.sendChunkedTrailer();

                    bis.close();
                }
                break;
            default:
                throw new IOException("unhandled status code");
        }
    }


    private static String mappingLocal(String path) throws UnsupportedEncodingException {
        return mappingLocal(path, 0);
    }


    private static String mappingLocal(String path, int showHidden) throws UnsupportedEncodingException {
        return HTMLMaker.makeIndex(path, showHidden);
    }

}
