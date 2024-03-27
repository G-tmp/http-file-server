package com.alpha.utils;

/**
 *  Parse post request contain multiple files
 *  
 * POST / HTTP/1.1
 * Content-Type: multipart/form-data; boundary=----WebKitFormBoundarydGnETrh9DhBD8Hlf
 * Content-Length: 7982380
 * \r\n
 * ------WebKitFormBoundarydGnETrh9DhBD8Hlf
 * Content-Disposition: form-data; name="file"; filename="1234.png"
 * Content-Type: image/png
 * \r\n
 * [data]
 * ------WebKitFormBoundarydGnETrh9DhBD8Hlf
 * Content-Disposition: form-data; name="file"; filename="987.jpeg"
 * Content-Type: image/jpeg
 * \r\n
 * [data]
 * ------WebKitFormBoundarydGnETrh9DhBD8Hlf--
 */

public class MultiFiles {

}
