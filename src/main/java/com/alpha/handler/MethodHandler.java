package com.alpha.handler;

import com.alpha.request.Cookie;
import com.alpha.request.Request;
import com.alpha.response.ContentType;
import com.alpha.response.Response;
import com.alpha.response.Status;
import com.alpha.utils.FileListUtil;
import com.alpha.utils.Reader;

import java.io.*;
import java.net.URLEncoder;



public class MethodHandler {

    private final static String HOME = System.getProperty("user.home");


    private MethodHandler() {
    }


    public static void doPost(Request request, Response response) throws IOException {
        SingleFile singleFile = request.parsePost();

        File path = singleFile.save(new File(HOME, request.getPath()).getPath(), singleFile.getFilename());
        System.out.println(path);

        String body = "success";
        response.setStatusCode(Status._200);
        response.setContentType(ContentType.HTML);
        response.addBody(body);
        response.setContentLength(body.getBytes().length);
        response.send();
    }


    public static void doGet(Request request, Response response) throws IOException {
        Status statusCode = statusCode(request);

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
                File file = new File(HOME, request.getPath());
                byte[] data = com.alpha.utils.Reader.readFile(file);
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
                File localFile = new File(HOME, request.getPath());
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

                    byte[] d = Reader.readFile(localFile);
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


    private static Status statusCode(Request request) {
        String path = request.getPath();
        File file = new File(HOME, path);
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
        File file = new File(HOME, path);
        if (! file.isDirectory())
            return null;


        StringBuffer html = new StringBuffer();
        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        html.append("<html>\n<head>\n");
        html.append("<meta name=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
        html.append("<title>").append(path).append("</title>\n</head>\n");
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
            String parentPath = file.getParent().replace(HOME, "") + "/";
            html.append("<a href=\"").append(parentPath).append("\">").append("Parent Directory").append("</a>");
        }
        html.append("<ul>\n");

        File[] files = FileListUtil.showHidden(file, showHidden);


        FileListUtil.sort(files, FileListUtil.SortBy.NAME,0);

        for (File subfile : files) {
            String displayName = subfile.getName();
            String link = URLEncoder.encode(subfile.getName(), "UTF-8");

            if (subfile.isDirectory()) {
                displayName += "/";
                link += "/";
                String element = String.format("<a href=\"%s\">%s</a>", link, displayName);
                html.append("<li>").append(element).append("</li>\n");
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
