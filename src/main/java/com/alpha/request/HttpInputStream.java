package com.alpha.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

public class HttpInputStream extends InputStream {
    private Reader source;
    private int bytesRemain;
    private boolean chunked;


    public HttpInputStream(Reader source, Map<String, String> headers) throws IOException {
        this.chunked = false;
        this.source = source;

        String declaredContentLength = headers.get("Content-Length");
        if (declaredContentLength != null) {
            try {
                bytesRemain = Integer.parseInt(headers.get("Content-Length"));
            } catch (NumberFormatException e) {
                throw new IOException("Malformed or missing Content-Length header");
            }
        } else if ("chunked".equalsIgnoreCase(headers.get("Transfer-Encoding"))) {
            chunked = true;
            bytesRemain = parseChunkSize();
        }
    }
    

    private int parseChunkSize() throws IOException {
        int b = 0;
        int chunkSize = 0;

        while ((b = source.read()) != '\r') {
            chunkSize = (chunkSize << 4) |
                    ((b > '9') ?
                            (b > 'F') ?
                                    (b - 'a' + 10) :
                                    (b - 'A' + 10) :
                            (b - '0'));

            // Consume the trailing '\n'
            if (source.read() != '\n') {
                throw new IOException("Malformed chunked encoding");
            }
        }

        return chunkSize;
    }

    @Override
    public int read() throws IOException {
        if (bytesRemain == 0) {
            if (!chunked) {
                return -1;
            } else {
                // Read next chunk size; return -1 if 0 indicating end of stream
                // Read and discard extraneous \r\n
                if (source.read() != ('\n' | '\r')) {
                    throw new IOException("Malformed chunked encoding");
                }

                bytesRemain = parseChunkSize();
                if (bytesRemain == 0) {
                    return -1;
                }
            }

        }

        bytesRemain = -1;
        // not work here
        return source.read();
    }
}
