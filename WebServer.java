import java.net.*;
import java.nio.file.*;
import java.io.*;

/**
 * Class that will deal with the client HTTP request
 */
public class WebServer {

    final private int TIMEOUT = 2000;

    public static void main(String[] args) throws IOException {
        // dummy value that is overwritten below
        int port = 8080;
        try {
          port = Integer.parseInt(args[0]);
        } catch (Exception e) {
          System.out.println("Usage: java webserver <port> ");
          System.exit(0);
        }

        WebServer serverInstance = new WebServer();
        serverInstance.start(port);
    }

    private void start(int port) throws IOException {
        System.out.println("Starting server on port " + port);
        //New socket
        ServerSocket socket = new ServerSocket(port);
        while (true) {
            handleClientSocket(socket);
        }
    }

    /**
     * Handles requests sent by a client
     * @param  socket Socket that handles the client connection
     */
    private void handleClientSocket(ServerSocket socket) throws IOException{

        Socket client = socket.accept();

        //Reads client request
        InputStreamReader read = new InputStreamReader(client.getInputStream());
        BufferedReader in = new BufferedReader(read);

        //Instantiates new request object with buffered request
        HttpRequest request = new HttpRequest(in);

        //Checks file availability and returns correct response
        File file = new File(request.getFilePath());
        if (file.exists()) {
            sendHttpResponse(client, formHttpResponse(request));
        } else {
            sendHttpResponse(client, form404Response(request));
        }

        //Checks connection type to close socket
        if (request.getConnectionType().equals("HTTP/1.1")) {
            socket.setSoTimeout(TIMEOUT);
        } else {
            socket.close();
        }
    }

    /**
     * Sends a response back to the client
     * @param  client Socket that handles the client connection
     * @param  response the response that should be send to the client
     */
    private void sendHttpResponse(Socket client, byte[] response) throws IOException {
        client.getOutputStream().write(response);
    }

    /**
     * Form a response to an HttpRequest
     * @param  request the HTTP request
     * @return a byte[] that contains the data that should be send to the client
     */
    private byte[] formHttpResponse(HttpRequest request) throws IOException {

        //Loads the requested file into a byte array

        byte[] fileBytes = Files.readAllBytes(Paths.get(request.getFilePath()));

        //Creates the HTTP header
        StringBuilder sb = new StringBuilder();
        sb.append(request.getConnectionType());
        sb.append(" ");
        sb.append("200 OK");
        sb.append("\r\n");
        sb.append("Content-Length:");
        sb.append(" ");
        sb.append(fileBytes.length);
        sb.append("\r\n");
        sb.append("\r\n");

        //Concatenates the header and content
        return concatenate(sb.toString().getBytes(), fileBytes);
    }


    /**
     * Form a 404 response for a HttpRequest
     * @param  request a HTTP request
     * @return a byte[] that contains the data that should be send to the client
     */
    private byte[] form404Response(HttpRequest request) {

        byte[] content = get404Content(request.getFilePath()).getBytes();

        StringBuilder sb = new StringBuilder();
        sb.append(request.getConnectionType());
        sb.append(" ");
        sb.append("404 Not Found");
        sb.append("\r\n");
        sb.append("Content-Length:");
        sb.append(" ");
        sb.append(content.length);
        sb.append("\r\n");
        sb.append("\r\n");

        return concatenate(sb.toString().getBytes(), content);
    }
    

    /**
     * Concatenates 2 byte[] into a single byte[]
     * This is a function provided for your convenience.
     * @param  buffer1 a byte array
     * @param  buffer2 another byte array
     * @return concatenation of the 2 buffers
     */
    private byte[] concatenate(byte[] buffer1, byte[] buffer2) {
        byte[] returnBuffer = new byte[buffer1.length + buffer2.length];
        System.arraycopy(buffer1, 0, returnBuffer, 0, buffer1.length);
        System.arraycopy(buffer2, 0, returnBuffer, buffer1.length, buffer2.length);
        return returnBuffer;
    }

    /**
     * Returns a string that represents a 404 error
     * You should use this string as the return website
     * for 404 errors.
     * @param  filePath path of the file that caused the 404
     * @return a String that represents a 404 error website
     */
    private String get404Content(String filePath) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>");
        sb.append("404 Not Found");
        sb.append("</title>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h1>404 Not Found</h1> ");
        sb.append("<p>The requested URL <i>" + filePath + "</i> was not found on this server</p>");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }
}

/**
 * Class that will be used to represent each client HTTP request
 */
class HttpRequest {
    final private String FILEPATH;
    final private String CONNECTION_TYPE;

    public HttpRequest(BufferedReader request) throws IOException {
        //Split the request into variables for the class
        String line = request.readLine();
        String[] splitRequest = line.split(" ");
        FILEPATH = splitRequest[1].substring(1);
        CONNECTION_TYPE = splitRequest[2];
    }

    /**
     * Getter method that returns a string that represents the requested file path
     * @return a String that represents the file path
     */
    public String getFilePath() {
        return FILEPATH;
    }

    /**
     * Getter method that returns a string that represents the requested connection type
     * @return a String that represents the connection type
     */
    public String getConnectionType() {
        return CONNECTION_TYPE;
    }

}
