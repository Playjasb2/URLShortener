import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.net.*;

public class ProxyServer {
  static int count = 0;
  static int[] ports= new int[3];

  static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "index.html";
	static final String FILE_NOT_FOUND = "404.html";
	static final String METHOD_NOT_SUPPORTED = "not_supported.html";
	static final String REDIRECT_RECORDED = "redirect_recorded.html";
	static final String REDIRECT = "redirect.html";
	static final String NOT_FOUND = "notfound.html";
  static final String DATABASE = "database.txt";

  public static volatile int DIVERTED_SERVER = -1;

  public static void main(String[] args) throws IOException {
    
    try {
      String[] hosts = {"dh2026pc13.utm.utoronto.ca","dh2026pc14.utm.utoronto.ca","dh2026pc15.utm.utoronto.ca"};
      //String[] hosts = {"127.0.0.1","127.0.0.1","127.0.0.1"};
      ports[0] = 12345; //skulls
      ports[1] = 12346; //green
      ports[2] = 12347; //white
      int localport = 1235;
      // Print a start-up message
      /*System.out.println("Starting proxy for " + hosts + ":" + ports[count%3]
          + " on port " + localport);*/
      // And start running the server

      ServerSocket serverMonitor = new ServerSocket(localport+3);

      Thread monitorThread = new Thread() {
        public void run() {
          handleMonitor(serverMonitor);
        }
      };

      monitorThread.start();


      runServer(hosts, localport); // never returns
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  /**
   * runs a single-threaded proxy server on
   * the specified local port. It never returns.
   */
  public static void runServer(String[] hosts, int localport)
      throws IOException {
    // Create a ServerSocket to listen for connections with
    ServerSocket ss = new ServerSocket(localport);

    final byte[] request = new byte[1024];
    byte[] reply = new byte[4096];

    Boolean madeNewThread = false;

    while (true) {
      //Socket client = null;
      try {
        // Wait for a connection on the local port
        System.out.println("Opening socket");
        final Socket client = ss.accept();

        System.out.println("Got here 1!");

        final InputStream streamFromClient = client.getInputStream();
        final OutputStream streamToClient = client.getOutputStream();

        final int bytesRead = streamFromClient.read(request);

        if(bytesRead == -1) {
          continue;
        }

        String line = ((new String(request, "UTF-8")).split("\n")[0]).trim();
        System.out.println("Got: " + line);
        if(line.isEmpty()){
          System.out.println("Got empty line.");
        }
        else if(line.equals("GET / HTTP/1.1"))
        {
           // Send the main page back to client
          ProxyServer.sendMainPage(streamToClient);
          System.out.println("Done sending page to client");
        }
        else if(line.equals("GET /favicon.ico HTTP/1.1")) {
          System.out.println("Getting favicon request");
          ProxyServer.send404(streamToClient);
        }
        else {
          Thread t = new Thread(new Runnable() {
            public void run() {
              System.out.println("Made a new thread");
              Socket server = null;
              int socketIndex = ProxyServer.getServerIndex(line);
              System.out.println("My server is: " + socketIndex);

              if(socketIndex == -1) {
                return;
              }
              
              InputStream streamFromServer = null;
              OutputStream streamToServer = null;

              if(socketIndex == DIVERTED_SERVER) {
                socketIndex = (socketIndex + 1)%3;
              }

              System.out.println("My current server now is: " + socketIndex);

              // Get connection to server

              try {
                server = new Socket(hosts[socketIndex], ports[socketIndex]);
                streamFromServer = server.getInputStream();
                streamToServer = server.getOutputStream();
              } catch (IOException e) {
                PrintWriter out = new PrintWriter(streamToClient);
                System.out.println("Cannot connect to server " + hosts[socketIndex]);
		/*
                out.print("Proxy server cannot connect to " + hosts[socketIndex] + ":"
                    + socketIndex + ":\n" + e + "\n");
                out.flush();
		*/

                try {
		  
                  socketIndex = (socketIndex + 1)%3;
                  server = new Socket(hosts[socketIndex], ports[socketIndex]);
                  streamFromServer = server.getInputStream();
                  streamToServer = server.getOutputStream();
                } catch (IOException e2) {
                  System.out.println("Cannot connect to server " + hosts[socketIndex]);
                  out.print("Proxy server cannot connect to " + hosts[socketIndex] + ":"
                      + socketIndex + ":\n" + e2 + "\n");
                  out.flush();
                  System.exit(-1);
                }
              }

              // Write to the server
              try {
                System.out.println("The number of bytes is: " + bytesRead);
                System.out.println("The size of the request is: " + request.length);
                streamToServer.write(request, 0, bytesRead);
                streamToServer.flush();
              } catch (IOException e) {
                System.out.println("Unable to send data to server " + hosts[socketIndex]);
                System.exit(1);
              }

              int serverBytesRead;

              System.out.println("I am now here!~");

              // Read from the server
              try {
                while ((serverBytesRead = streamFromServer.read(reply)) != -1) {
                  streamToClient.write(reply, 0, serverBytesRead);
                  streamToClient.flush();
                }
              } catch (IOException e) {
                System.err.println("Can't send data to client because: " + e);
                System.out.println("Unable to send data to client");
              }
              
              try {
                server.close();
                client.close();
              } catch (IOException e) {
                System.out.println("Can't close server");
                
              }
            }
          });
          t.start();
          madeNewThread = true;
        }
        
        System.out.println("Made it here!~");

        // The server closed its connection to us, so we close our
        // connection to our client.
        //streamToClient.close();

        if(!madeNewThread) {
          try {
            if (client != null)
            {
              client.close();
              System.out.println("Closed client");
            }
          } catch (IOException e) {
          }
        }
        madeNewThread = false;
      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }

  private static void sendMainPage(OutputStream streamToClient) {
		PrintWriter out = new PrintWriter(streamToClient);
		BufferedOutputStream dataOut = new BufferedOutputStream(streamToClient);

    File file = new File(WEB_ROOT, DEFAULT_FILE);
    int fileLength = (int) file.length();
    String contentMimeType = "text/html";

    //read content to return to client
    try{
      byte[] fileData = readFileData(file, fileLength);
      
      // out.println("HTTP/1.1 301 Moved Permanently");
      out.println("HTTP/1.1 200 Success");
      out.println("Server: Java HTTP Server/Shortner : 1.0");
      out.println("Date: " + new Date());
      out.println("Content-type: " + contentMimeType);
      out.println("Content-length: " + fileLength);
      out.println(); 
      out.flush(); 

      dataOut.write(fileData, 0, fileLength);
      dataOut.flush();
    } catch(IOException e) {
      System.out.println("Cannot send the main page to the client.");
    }
  }

  private static void send404(OutputStream streamToClient) {
    PrintWriter out = new PrintWriter(streamToClient);
    BufferedOutputStream dataOut = new BufferedOutputStream(streamToClient);
    
    File file = new File(WEB_ROOT, FILE_NOT_FOUND);
    int fileLength = (int) file.length();
    String content = "text/html";

    try{
      byte[] fileData = readFileData(file, fileLength);
      
      out.println("HTTP/1.1 404 File Not Found");
      out.println("Server: Java HTTP Server/Shortner : 1.0");
      out.println("Date: " + new Date());
      out.println("Content-type: " + content);
      out.println("Content-length: " + fileLength);
      out.println(); 
      out.flush(); 
      
      dataOut.write(fileData, 0, fileLength);
      dataOut.flush();
    } catch(IOException e) {
      System.out.println("Cannot send 404 page to the client.");
    }
  }

  private static byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
  }
  
  private static int getServerIndex(String line) {

    String shortResource = null;
    String longResource = null;
    System.out.println("My line is: " + line);
    Pattern pput1 = Pattern.compile("^GET\\s+/(\\S+)\\s+(\\S+)$");
    Pattern pput2 = Pattern.compile("^PUT\\s+/\\?short=(\\S+)&long=(\\S+)\\s+(\\S+)$");
    Matcher mput1 = pput1.matcher(line);
    Matcher mput2 = pput2.matcher(line);
    
    if(mput1.matches()) {
      shortResource = mput1.group(1);
    }
    else if(mput2.matches()) {
      shortResource = mput2.group(1);
      longResource = mput2.group(2);
    }
    else {
      return -1;
    }

    if(shortResource.length() > 24 || (longResource != null && longResource.length() > 120)) {
      return -1;
    }

    // Hashing
    int myNum = -1;

    try{
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] theDigest = md.digest(shortResource.getBytes());
      System.out.println("The digest is: " + theDigest.toString());
      myNum = Math.abs(ByteBuffer.wrap(theDigest).getInt())%3;
      System.out.println("The number I am getting is: " + myNum);
    } catch(NoSuchAlgorithmException e) {
      System.err.println(e);
    }

    return myNum;
  }

  public static void handleMonitor(ServerSocket serverMonitor) {
    BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
    Socket connect = null;
		while(true){
			try {
        //System.out.println("Listening for monitor signal");
				connect = serverMonitor.accept();

				in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
				out = new PrintWriter(connect.getOutputStream());
				dataOut = new BufferedOutputStream(connect.getOutputStream());
				
				String input = in.readLine();

				//System.out.println("Input: " + input);
				
				// signals from monitor
				if(input.equals("PING")){
					out.println("on");
					out.flush();
					dataOut.flush();
        }
        else if (input.contains("DIVERT:")) {
          System.out.println("Diverting");
          Pattern pput = Pattern.compile("^DIVERT:(\\S+)$");
          Matcher mput = pput.matcher(input);
          if(mput.matches()) {
            DIVERTED_SERVER = Integer.parseInt(mput.group(1))-1;
            out.println("TRAFFIC HAS BEEN DIVERTED");
            out.flush();
            dataOut.flush();
          }
        }else if(input.equals("ALLOW")){
		
		System.out.println("Diverting back");
		DIVERTED_SERVER = -1;
		out.println("TRAFFIC HAS REVERTED");
		out.flush();
            	dataOut.flush();
	}

			} catch (IOException e) {
				System.err.println("Server error:" + e.getMessage());
				System.err.println(e.getStackTrace());

			} finally {
				try {
					in.close();
					out.close();
					connect.close(); // we close socket connection
				} catch (Exception e) {
					System.err.println("Error closing stream : " + e.getMessage());
				} 
				
			}
		}
	}
}
