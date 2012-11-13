package org.va.foo;

public class WebServer {

  public final static int WEB_SERVER_PORT = 9000;

  public static void main(String[] args) {
    ThreadPooledServer server = new ThreadPooledServer(WEB_SERVER_PORT);
    new Thread(server).start();
  }
}
