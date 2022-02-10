package com.alpha.httpRequest.method;

import com.alpha.httpRequest.Cookie;
import com.alpha.httpRequest.Request;
import com.alpha.httpResponse.ContentType;
import com.alpha.httpResponse.Response;
import com.alpha.httpResponse.Status;
import com.alpha.server.HttpServer;
import com.alpha.utils.FilesFilter;

import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;


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
                String range = request.getHeader("Range");
                File file = new File(HttpServer.HOME, request.getPath());
                long length = file.length();
//                FileInputStream fis = new FileInputStream(file);

                if (range != null && range.contains("bytes")) {
                    long start;
                    long end;
                    long n = 1000 * 1000 * 5;

                    try {
                        start = Integer.parseInt(range.substring(range.indexOf("=") + 1, range.indexOf("-")));
                    } catch (NumberFormatException e) {
                        start = 0;
                    }

                    try {
                        end = Integer.parseInt(range.substring(range.indexOf("-") + 1));
                    } catch (NumberFormatException e) {
                        end = 0;
                    }

                    if (end == 0) {
                        if (length - start > n) {
                            end = n + start - 1;
                        } else {
                            end = length - 1;
                        }
                    }

                    response.addHeader("Content-Range", String.format("bytes %d-%d/%d", start, end, length));
                    response.setContentLength(end - start + 1);
                    response.setStatusCode(Status._206);
                    response.addHeader("Accept-Ranges", "bytes");
                    response.guessContentType(request.getPath());
                    response.sendHeader();

                    FileInputStream fis = new FileInputStream(file);
                    byte[] b = fis.readAllBytes();
                    response.sendBody(Arrays.copyOfRange(b, (int)start, (int)end+1));

                }
                break;
            case _200:
                File localFile = new File(HttpServer.HOME, request.getPath());
                // html
                if (localFile.isDirectory()) {

                    // check parameter
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
                            // parameter showHidden is null or not a number
                            // do not set cookie
                            //if (showHidden != null)
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
                    response.setContentType(ContentType.HTML);
                    response.addBody(html);
                    response.setContentLength(html.getBytes().length);
                    response.send();
                } else {        // file
                    response.setStatusCode(Status._200);
                    response.addHeader("Accept-Ranges", "bytes");
                    response.guessContentType(request.getPath());
                    response.setContentLength(localFile.length());
                    response.sendHeader();

                    byte[] buffer = new byte[1024 * 8];
                    int count = 0;
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(localFile));
                    while ((count = bis.read(buffer)) != -1) {
                        response.sendBody(buffer, 0, count);
                    }
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
        File file = new File(HttpServer.HOME, path);
        if (!file.isDirectory())
            return null;


        StringBuffer html = new StringBuffer();
        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        html.append("<html>\n<head>\n");
        html.append("<meta name=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
        html.append("<title>").append(path).append("</title>\n");
        html.append("<style type=\"text/css\">\n").append("\tli{margin: 10px 0;}\n").append("</style>\n").append("</head>\n");
        html.append("<body>\n").append("<h1>Directory listing for ").append(path).append("</h1>\n");
        if (showHidden == 1) {
            html.append("<a href=\"?showHidden=0\"><button>Show Hidden Files</button></a>&#10004;<p>"); // show
        } else if (showHidden == 0) {
            html.append("<a href=\"?showHidden=1\"><button>Show Hidden Files</button></a>&#10007;<p>"); // hidden
        }
        html.append("<form  method=\"POST\" enctype=\"multipart/form-data\">\n");
//        html.append("<input type=\"text\" name=\"p1\" required=\"required\"> >>");
        html.append("<input type=\"file\" name=\"file\" required=\"required\"> >>");
        html.append("<button type=\"submit\">Upload</button>\n</form>\n");
        html.append("<hr>\n");

        if ("/".equals(path)) {
            html.append("/");
        } else {
            String parentPath = file.getParent().replace(HttpServer.HOME, "") + "/";
            html.append("<a href=\"").append(parentPath).append("\">").append("Parent Directory").append("</a>");
        }
        html.append("<ul>\n");

        File[] files = FilesFilter.showHidden(file, showHidden);


        FilesFilter.sort(files, FilesFilter.SortBy.NAME, 0);

        for (File subfile : files) {
            String displayName = subfile.getName();
            String link = URLEncoder.encode(subfile.getName(), "UTF-8");

            if (subfile.isDirectory()) {
                displayName += "/";
                link += "/";
                String element = String.format("<a href=\"%s\"><strong>%s</strong></a>", link, displayName);
                html.append("<li style=>").append(element).append("</li>\n");
            } else if (subfile.isFile()) {
                String element = String.format("<a href=\"%s\">%s</a>", link, displayName);
                String download = String.format("<a href=\"%s\">%s</a>", link + "?download=1", "DL");
                html.append("<li>").append(element).append("&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;").append(download).append("</li>\n");
            } else {
                // symbol link
                displayName = displayName + "@";
                String element = String.format("<a href=\"%s\">%s</a>", link, displayName);
                html.append("<li>").append(element).append("</li>\n");
            }

        }
        html.append("</ul>\n<hr>\n</body>\n</html>");

        return String.valueOf(html);
    }

}
