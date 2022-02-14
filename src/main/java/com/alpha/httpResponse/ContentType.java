package com.alpha.httpResponse;


/**
 * ContentType enum uses the file extension to loosely map the
 * available content type based on common media types:
 *
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

    private final String extension;


    ContentType(String extension) {
        this.extension = extension;
    }


    @Override
    public String toString() {
        switch (this) {
            case HTM:
            case HTML:
                return "text/html; charset=utf-8";
            case CSS:
                return "text/css";
            case ICO:
                return "image/gif";
            case JPG:
            case JPEG:
                return "image/jpeg";
            case PNG:
                return "image/png";
            case GIF:
                return "image/gif";
            case TXT:
                return "text/plain; charset=UTF-8";
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
                return "text/markdown; charset=UTF-8";
            case PDF:
                return "application/pdf";
            default:
                return null;
        }
    }


}
