package com.alpha.utils;

import com.alpha.server.HttpServer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

public class HTMLMaker {

    private  HTMLMaker(){}


    public static String makeIndex(String path, int showHidden) throws UnsupportedEncodingException {
        File dir = new File(HttpServer.HOME, path);
        if (!dir.isDirectory())
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

        // Upload form
        html.append("<form  method=\"POST\" enctype=\"multipart/form-data\">\n");
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
        FilesFilter.sortByName(files);
        for (File subfile : files) {
            String displayName = subfile.getName();
            String link = URLEncoder.encode(subfile.getName(), "UTF-8");
            Path toPath = subfile.toPath();

            if (Files.isDirectory(toPath)) {
                displayName += "/";
                link += "/";
                String element = String.format("<a href=\"%s\"><strong>%s</strong></a>", link, displayName);
                html.append("<li style=>").append(element).append("</li>\n");
            } else if (Files.isSymbolicLink(toPath)){
                // symbol link
                displayName = displayName + "@";
                String element = String.format("<a href=\"%s\">%s</a>", link, displayName);
                html.append("<li>").append(element).append("</li>\n");
            } else if (Files.isRegularFile(toPath)) {
                String element = String.format("<a href=\"%s\">%s</a>", link, displayName);
                String download = String.format("<a href=\"%s\">%s</a>", link + "?download", "DL");
                html.append("<li>").append(element).append("&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;").append(download).append("</li>\n");
            }

        }

        html.append("</ul>\n<hr>\n</body>\n</html>");

        return String.valueOf(html);
    }
}
