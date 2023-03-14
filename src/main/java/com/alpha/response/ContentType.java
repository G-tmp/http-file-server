package com.alpha.response;


/**
 * ContentType enum uses the file extension to loosely map the
 * available content type based on common media types:
 * <p>
 * http://en.wikipedia.org/wiki/Internet_media_type
 */
public enum ContentType {

    HTM("HTM"),
    HTML("HTML"),
    CSS("CSS"),
    ICO("ICO"),
    JPG("JPG"),
    JPEG("JPEG"),
    PNG("PNG"),
    GIF("GIF"),
    TXT("TXT"),
    FLAC("FLAC"),
    MP3("MP3"),
    MP4("MP4"),
    WEBM("WEBM"),
    WEBP("WEBP"),
    XML("XML"),
    MD("md"),
    PDF("pdf");


    ContentType(String extension) {
    }


    @Override
    public String toString() {
        switch (this) {
            case HTM:
            case HTML:
                return "text/html; charset=utf-8";
            case CSS:
                return "text/css";
            case JPG:
            case JPEG:
                return "image/jpeg";
            case PNG:
                return "image/png";
            case GIF:
            case ICO:
                return "image/gif";
            case TXT:
                return "text/plain; charset=utf-8";
            case XML:
                return "text/xml";
            case FLAC:
                return "audio/flac";
            case MP3:
                return "audio/mpeg";
            case MP4:
                return "video/mp4";
            case WEBM:
                return "video/webm";
            case WEBP:
                return "image/webp";
            case MD:
                return "text/markdown; charset=utf-8";
            case PDF:
                return "application/pdf";
            default:
                return null;
        }
    }


}
