# http-file-server

A simple multi-threading http server supports displaying local files


## development environment

OpenJDK11 Ubuntu18


## Features

### redirect

contains two requests and responses, be used to refresh page and address bar here

response code 302 and contain header ```Location: url```

pay attention to **Redirect loops**


### connection keep alive

add response header ```Connection: keep-alive```

socket set timeout, request and response reuse socket connection


### parse http request

read from byte stream rather than character stream, request headers and body divided by  ```\r\n\r\n``` 4 ascii characters, read 2 bytes each time until detect ```\r\n\r``` 

if it is a post request, read header ```Content-Length``` and read corresponding number of bytes, then parse body


### playback media

browser request a media file, response code 200 and add header ```Accept-Ranges: bytes```;
second request contains ```Range: bytes=0-```, response code 206 and header```Content-Range: bytes start-end/length``` 


### download file

add http response header ```Content-Disposition: attachment; filename=?```


### cookie

add http response header ```Set-Cookie: key1=value1; key2=value2; ...```



## TODO

* response correct Mime‑Type, by file signature maybe

* post request only supports upload one file, it is tedious to parse post body

* sort files by name, size  or last modified time

* chunked transfer encoding

* http pipelining

* sometimes the socket connection is disconnected for unknown reasons



## Reference

* [A Simple HTTP Server in Java](https://commandlinefanatic.com/cgi-bin/showarticle.cgi?article=art076)

* [HTTP Server with POST and SSL support](https://commandlinefanatic.com/cgi-bin/showarticle.cgi?article=art077)

* [An HTTP server in Java, part 3](https://commandlinefanatic.com/cgi-bin/showarticle.cgi?article=art078)

* [Configuring web servers for HTML5 Ogg video and audio](https://blog.pearce.org.nz/2009/08/configuring-web-servers-for-html5-ogg.html)

* [dasanjos/java-WebServer](https://github.com/dasanjos/java-WebServer)