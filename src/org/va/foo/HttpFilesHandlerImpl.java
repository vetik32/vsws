// © 2003-2011 Adobe Systems Inc. All Rights Reserved.
// This software is proprietary; use is subject to license terms.
package org.va.foo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adobe Systems Inc
 */
public class HttpFilesHandlerImpl implements Runnable {

  private Socket socket = null;

  static final int BUFFER_SIZE = 1024;
  static final String CLRN = "\n\r";

  String base = "webapp";

  public static final int HTTP_OK = 200;

  /**
   * Content types for
   */
  public static Map<String, String> mime_types = new HashMap<String, String>();

  static {
    mime_types.put("", "content/unknown");
    mime_types.put(".html", "text/html");
  }

  public HttpFilesHandlerImpl(Socket socket) {
    this.socket = socket;
  }

  public void run() {
    try {
      handle();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void handle() throws IOException {
    BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
    PrintStream os = new PrintStream(socket.getOutputStream());

    byte[] buffer = new byte[BUFFER_SIZE];

    // Read the initial request line (RFC 2616).
    int end = -1;
    while (true) {
      int read = is.read(buffer, 0, BUFFER_SIZE);
      if (read == -1) {
        break;
      }
      // Look for CRLF, only read the first line;
      for (int i = 0; i < read; i++) {
        if (buffer[i] == (byte) '\n' || buffer[i] == (byte) ('\r')) {
          end = i;
          break;
        }
      }
      if (end != -1) break;
    }

    if (buffer[0] == (byte) 'G' &&
      buffer[1] == (byte) 'E' &&
      buffer[2] == (byte) 'T' &&
      buffer[3] == (byte) ' ') {

      int uri_end = 4;
      while(uri_end < end && buffer[uri_end] != (byte)' ') {
        uri_end ++;
      }

      String fileName = (new String(buffer, 4, uri_end - 4, "UTF-8"));

      if (fileName.equalsIgnoreCase(File.separator)) {
          fileName = "index.html";
      }

      File file = new File(base, fileName);

      if (!file.exists()) {
        fileName = "404.html";
        file = new File(base, fileName);
      }

      if (file.canRead()) {
        try {
          Thread.sleep(3000);  //emulate heavy loads
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        sendFile(os, file);
      }
    }
  }

  /**
   * Send file content.
   * @param printStream
   * @param file
   * @throws IOException
   */
  private void sendFile(PrintStream printStream, File file) throws IOException {
    String contentType = mime_types.get(getExtension(file.getName()));
    printHeaders(printStream, HTTP_OK, contentType, file.length());
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file.getAbsolutePath());
      int bytesRead;
      byte[] buffer = new byte[BUFFER_SIZE];
      while ((bytesRead = inputStream.read(buffer)) > 0) {
        printStream.write(buffer, 0, bytesRead);
      }
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }


    /**
   * Print HTTP status code and headers for content type and content length.
   * @param printStream
   * @param code HTTP status code.
   * @param contentType Content-Type header string.
   * @param contentLength Content-Length header value.
   * @throws IOException
   */
  private void printHeaders(PrintStream printStream,
      int code,
      String contentType,
      long contentLength) throws IOException {

    printStream.print("HTTP/1.1 " + code + CLRN);
    printStream.print("Content-Type: " + contentType + CLRN);
    printStream.print("Content-Length: " + contentLength + CLRN);
    printStream.print(CLRN);
  }

  /**
   * Helper function. Extracts the filename extension.
   * @param filename Filename (name.extension).
   * @return File extension including the dot.
   */
  private String getExtension(String filename) {
    int index = filename.lastIndexOf(".");
    return index >= 0 ? filename.substring(index) : "";
  }

}
