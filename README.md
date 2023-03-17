# HTTP file server



A simple HTTP server supports display local files

* multi-threading

* HTTP/1.1

* GET and POST only






## Launch

```java -jar HttpServer.jar 8888```







## Implementation

### chunked transfer encoding 

* response header ```Transfer-Encoding: chunked```

* response body 
```
4\r\n
HTTP\r\n
0\r\n
\r\n
```


### redirect

* response code 302 and contain header ```Location: url```

* pay attention to Redirect loops


### connection keep alive

* response header ```Connection: keep-alive```

* set socket timeout, reuse socket connection


### parse HTTP request

* read from byte stream, request headers and body divided by  ```\r\n\r\n``` 4 ascii characters, read 2 bytes each time until detect ```\r\n\r``` 

* if it is a post request, read header ```Content-Length``` and read corresponding number of bytes in body, then parse body


### playback media

* browser request a media file, response code 200 and header ```Accept-Ranges: bytes```

* then browser request headers contain ```Range: bytes=start-end```, response code 206 and header```Content-Range: bytes start-end/length``` 


### download file

* HTTP response header ```Content-Disposition: attachment; filename=?```


### cookie

* HTTP response header ```Set-Cookie: key1=value1; key2=value2; ...```





## About client

* working well on Chrome and Firefox, but Firefox is more stricter

* IOS Safari sucks, does not support webm, and ignore ```Content-Disposition``` header cause can not download file directly, and upload compressed image rather than original





## TODO

* test on Windows 

* response correct Mime‑Type, maybe by file signature maybe

* sort files by name, size  or last modified time

* sometimes the socket connection is disconnected for unknown reasons

* refactor and use design patterns

* I/O Multiplexing

* HTTP pipelining

* HTTP/2





## References

* [A Simple HTTP Server in Java](https://commandlinefanatic.com/cgi-bin/showarticle.cgi?article=art076)

* [HTTP Server with POST and SSL support](https://commandlinefanatic.com/cgi-bin/showarticle.cgi?article=art077)

* [An HTTP server in Java, part 3](https://commandlinefanatic.com/cgi-bin/showarticle.cgi?article=art078)

* [Configuring web servers for HTML5 Ogg video and audio](https://blog.pearce.org.nz/2009/08/configuring-web-servers-for-html5-ogg.html)

* [dasanjos/java-WebServer](https://github.com/dasanjos/java-WebServer)
