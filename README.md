## VerySimpleWebServer - walkthrough
====

Inspired code:

http://dailyjavatips.com/2011/10/18/build-a-web-server/

build  multi thread web server

    user -->  uri  --> webserver
     ^                    |
     +------- data -------+

    webserver
       +--socket
            ^ +--> BufferedInputStream --> fileName ---------+
            |                                                V
            +--< PrintStream <-- FileInputStream <-- File(nameName)

Let's start from the end.

## Find out what does user want

To find out we shall read inputStream

    BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
which should look like

    GET /uri http/1.1 \r\n
    host: serverName:port \r\n
    ...

We are shall check if this is get request

read 1st line until CLRN buffer[i] = (byte) ('\n' || '\r')

is this is end uri starts with 4th char

    GET /index.html h...
    01234567890123456...

so the fileName is a substring from 4 to 15 chars, starting with 4th char, read next 15-4=9 chars

    String fileName = (new String(buffer, 4, uri_end - 4, "UTF-8"));

now we have the fileName user requested.


## Read the file and sent it to the "user"

#

# File

<<  The API says that the class File is "An abstract representation of file
and directory path names." The File class isn't used to actually read or write
data; it's used to work at a higher level, making new empty files, searching for
files, deleting files, making directories, and working with paths. >> SCJP 6

In our case we use File to get the path to the requested file e.g.

    File file = new File(base, fileName);  // file.getAbsolutePath()

# FileInputStream

to read from file will use FileInputStream and to "write" to outputStream will use PrintStream

	InputStream inputStream = new FileInputStream(file.getAbsolutePath());

define buffer

    byte[] buffer = new byte[1024];

reads block with size of buffer while there is data to read

    while (( bytesRead = inputStream.read(buffer)) > 0 ) {
      printStream.write(buffer, 0, bytesRead); // write the whole block
    }

don't forget to close the inputStream if we had one created successfully

    inputStream.close()


The part with dealing when a request is coming is done. now let's review server side:

the communication is done through sockets:

Socket on client side and ServerSocket on serverSide

when a request appears it gets "unique" socket - to communicate with specific client;

    Socket clientSocket = serverSocket.accept()
