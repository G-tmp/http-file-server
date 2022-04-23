package com.alpha.server;

public interface Constants {
    final static String PROTOCOL_VERSION = "HTTP/1.1";

    final static String SERVER_NAME = "XD";

    final static String HOME = System.getProperty("user.home");

    final static int TIMEOUT = 10;

    final static int Kb = 1024 ;

    final static int Mb = 1024 * 1024;

    final static int BUFFER_SIZE = Kb * 8;
}
