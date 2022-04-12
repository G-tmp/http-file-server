package com.alpha.utils;

import com.alpha.server.HttpServer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

public class HTMLMaker {

    private  HTMLMaker(){}


    public static String makeIndex(String path, boolean showHidden) throws UnsupportedEncodingException {
        File dir = new File(HttpServer.HOME, path);
        if (!dir.isDirectory())
            return null;


        StringBuffer html = new StringBuffer();
        html.append("<!DOCTYPE html>");
        html.append("<html>\n<head>\n");
        html.append("<meta name=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
        html.append("<title>").append(path).append("</title>\n");
        html.append("<style type=\"text/css\">\n").append("\tli{margin: 10px 0;}\n").append("</style>\n").append("</head>\n");
        html.append("<body>\n").append("<h1>Directory listing for ").append(path).append("</h1>\n");
        if (showHidden) {
            html.append("<a href=\"?showHidden=false\"><button>Show Hidden Files</button></a> on <p>"); // show
        } else {
            html.append("<a href=\"?showHidden=true\"><button>Show Hidden Files</button></a> off <p>"); // hidden
        }

        // Upload form
        html.append("<form  method=\"post\" enctype=\"multipart/form-data\">\n");
//        html.append("<input type=\"text\" name=\"p1\" required=\"required\"> >>");
        html.append("<input type=\"file\" name=\"file\" required=\"required\"> >>");
        html.append("<button type=\"submit\">Upload</button>\n</form>\n");
        html.append("<hr>\n");

        if ("/".equals(path)) {
            html.append("/");
        } else {
            String parentPath = dir.getParent().replace(HttpServer.HOME, "") + "/";
            html.append("<a href=\"").append(parentPath).append("\">").append("Parent Directory").append("</a>");
        }
        html.append("<ul>\n");

        File[] files = FilesFilter.showHidden(dir, showHidden);
        FilesFilter.sortByFileName(files);
        for (File subfile : files) {
            String displayName = subfile.getName();
            String link = URLEncoder.encode(subfile.getName(), "UTF-8");
            Path toPath = subfile.toPath();
            String element = null;

            if (Files.isDirectory(toPath)) {
                displayName += "/";
                link += "/";
                element = String.format("<a href=\"%s\"><strong>%s</strong></a>", link, displayName);
                html.append("<li style=>").append(element).append("</li>\n");
            } else if (Files.isSymbolicLink(toPath)){
                // symbol link
                element = String.format("<a href=\"%s\">%s</a>@", link, displayName);
                html.append("<li>").append(element).append("</li>\n");
            } else if (Files.isRegularFile(toPath)) {
                element = String.format("<a href=\"%s\">%s</a>", link, displayName);
                String download = String.format("<a href=\"%s\">%s</a>", link + "?download", "DL");
                html.append("<li>").append(element).append("&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;").append(download).append("</li>\n");
            }

        }

        html.append("</ul>\n<hr>\n</body>\n</html>");

        return String.valueOf(html);
    }
}
