package com.alpha.request.method;

import com.alpha.request.Cookie;
import com.alpha.request.HttpRequest;
import com.alpha.response.ContentType;
import com.alpha.response.HttpResponse;
import com.alpha.response.Status;
import com.alpha.server.HttpServer;
import com.alpha.utils.HTMLMaker;

import java.io.*;


public class Get implements Method {
    private HttpRequest request;
    private HttpResponse response;


    public Get(HttpRequest request, HttpResponse response) {
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
            case _307:
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

                    byte[] b = new byte[HttpServer.BUFFER_SIZE];
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
                        if (showHidden.equals("true")) {
                            Cookie cookie = new Cookie("showHidden", "true");
                            cookie.setMaxAge(60 * 60 * 2);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                            response.redirect(request.getPath());
                        } else {
                            Cookie cookie = new Cookie("showHidden", "false");
                            cookie.setMaxAge(60 * 60 * 2);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                            response.redirect(request.getPath());
                        }
                    }


                    String html = mappingLocal(request.getPath());

                    // check cookie
                    for (Cookie cookie : request.getCookies()) {
                        if (cookie.getName().equals("showHidden")) {
                            String show = cookie.getValue();

                            if (show.equals("true")) {
                                html = mappingLocal(request.getPath(), true);
                            } else {
                                html = mappingLocal(request.getPath(), false);
                            }
                        }
                    }

                    response.setStatusCode(Status._200);
                    response.enableChunked();
                    response.setContentType(ContentType.HTML);
                    response.sendHeader();

                    response.sendChunkedFin(html.getBytes());
                } else {   // file

                    // check parameter download
                    String download = request.getParameter("download");
                    if (download != null) {
                        response.addHeader("Content-Disposition", "attachment");
                    }

                    response.setStatusCode(Status._200);
                    response.guessContentType(request.getPath());
                    response.setContentLength(localFile.length());
                    response.sendHeader();

                    byte[] buffer = new byte[HttpServer.BUFFER_SIZE];
                    int n = 0;
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(localFile));

                    while ((n = bis.read(buffer)) != -1) {
                        response.sendBody(buffer, 0, n);
                    }

                    bis.close();
                }
                break;
            default:
                throw new IOException("unhandled status code");
        }
    }


    private static String mappingLocal(String path) throws UnsupportedEncodingException {
        return mappingLocal(path, false);
    }


    private static String mappingLocal(String path, boolean showHidden) throws UnsupportedEncodingException {
        return HTMLMaker.makeIndex(path, showHidden);
    }

}
