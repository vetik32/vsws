package org.va.foo;

public class WebServer {

  public static void main(String[] args) {
    ThreadPooledServer server = new ThreadPooledServer(9000);
    new Thread(server).start();
  }
}
