package com.alpha.server;

public interface Constants {
    final static String HOME = System.getProperty("user.home");

    final static int TIMEOUT = 10;

    final static int Kb = 1 << 10;

    final static int Mb = 1 << 20;

    final static int BUFFER_SIZE = 16 * Kb;
}
