package com.alpha.httpRequest.method;

import com.alpha.httpRequest.Cookie;
import com.alpha.httpRequest.Request;
import com.alpha.httpResponse.ContentType;
import com.alpha.httpResponse.Response;
import com.alpha.httpResponse.Status;
import com.alpha.server.HttpServer;
import com.alpha.utils.FileReader;
import com.alpha.utils.FilesFilter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Get implements Method {
    private Request request;
    private Response response;


    public Get(Request request, Response response){
        this.request = request;
        this.response = response;
    }


    @Override
    public void execute() throws IOException {
        Status statusCode = getStatusCode(request);

        switch (statusCode) {
            case _404:
                response.setStatusCode(Status._404);
                response.addBody("");
                response.setContentLength(0);
                response.send();
                break;
            case _401:
                response.setStatusCode(Status._401);
                response.addBody("");
                response.setContentLength(0);
                response.send();
                break;
            case _302:
                response.redirect(request.getPath() + "/");
                break;
            case _206:
                String range = request.getHeader("Range");
                File file = new File(HttpServer.HOME, request.getPath());
                byte[] data = FileReader.readFile(file);
                int length = data.length;

                if (range != null && range.contains("bytes")) {
                    int start = Integer.parseInt(range.substring(range.indexOf("=") + 1, range.indexOf("-")));
                    response.setStatusCode(Status._206);
                    // TODO - return partial bytes not all
                    response.addHeader("Content-Range", String.format("bytes 0-%d/%d", length - 1, length));
                    response.guessContentType(request.getPath());
                    response.addBody(data);
                    response.setContentLength(length);
                    response.send();
                }
                break;
            case _200:
                File localFile = new File(HttpServer.HOME, request.getPath());
                // html
                if (localFile.isDirectory()) {

                    // check parameter
                    String showHidden = request.getParameter("showHidden");
                    try {
                        int i = Integer.parseInt(showHidden);

                        if (i == 1) {
                            Cookie cookie = new Cookie("showHidden", String.valueOf(1));
                            cookie.setMaxAge(60 * 60);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                            response.redirect(request.getPath());
                        } else {
                            Cookie cookie = new Cookie("showHidden", String.valueOf(0));
                            cookie.setMaxAge(60 * 60);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                            response.redirect(request.getPath());
                        }

                    } catch (NumberFormatException e) {
                        // parameter showHidden is null or not a number
                        // do not set cookie
                        if (showHidden != null)
                            response.redirect(request.getPath());
                    }


                    // check cookie
                    String c = request.getCookie("showHidden");
                    String html = null;
                    try {
                        int i = Integer.parseInt(c);
                        if (i == 1) {
                            html = mappingLocal(request.getPath(), 1);
                        } else {
                            html = mappingLocal(request.getPath(), 0);
                        }
                    } catch (NumberFormatException e) {
                        html = mappingLocal(request.getPath(), 0);
                    }


                    response.setStatusCode(Status._200);
                    response.setContentType(ContentType.HTML);
                    response.addBody(html);
                    response.setContentLength(html.getBytes().length);
                    response.send();
                } else {

                    byte[] d = FileReader.readFile(localFile);
                    int l = d.length;

                    String isDownload = request.getParameter("download");
                    if (isDownload != null && "1".equals(isDownload)) {
                        response.addHeader("Content-Disposition", "attachment; filename=\"" + localFile.getName() + "\"");
                    }

                    response.setStatusCode(Status._200);
                    response.addHeader("Accept-Ranges", "bytes");
                    response.guessContentType(request.getPath());
                    response.addBody(d);
                    response.setContentLength(l);
                    response.send();

                }
                break;
            default:
                throw new IOException("unhandled status code");
        }
    }


    private static Status getStatusCode(Request request) {
        String path = request.getPath();
        File file = new File(HttpServer.HOME, path);
        System.out.println(file);

        String range = request.getHeader("Range");
        if (range != null && range.contains("bytes"))
            return Status._206;

        if (!file.exists()) {
            return Status._404;
        }

        if (!file.canRead()) {
            return Status._401;
        }

        if (file.isDirectory()) {
            if (!path.endsWith("/")) {
                return Status._302;
            } else

                return Status._200;
        } else {
            // is file
            return Status._200;
        }
    }



    private static String mappingLocal(String path) throws UnsupportedEncodingException {
        return mappingLocal(path, 0);
    }


    private static String mappingLocal(String path, int showHidden) throws UnsupportedEncodingException {
        File file = new File(HttpServer.HOME, path);
        if (! file.isDirectory())
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


        FilesFilter.sort(files, FilesFilter.SortBy.NAME,0);

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
