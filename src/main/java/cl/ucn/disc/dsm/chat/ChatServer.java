package cl.ucn.disc.dsm.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatServer {
    /**
     * bd representation
     */
    private static final List<ChatMessage> messages = new ArrayList<ChatMessage>();
    /**
     * Logger and port for socket
     */
    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);
    private final int port;
    /**
     * Constructor
     * @param PORT
     */
    public ChatServer(final int PORT) throws IOException {
        if (PORT < 1024 || PORT > 65535) { //available port restrictions
            throw new IllegalArgumentException("Please use other port");
        }
        this.port = PORT;
    }

    /**
     * Method that starts the server and waits for connections
     */
    public void start() throws IOException {
        log.debug("Starting server ...");
        final ServerSocket serverSocket = new ServerSocket(port);
        log.debug("Server started in port {}, waiting for connections ..", port);
        while (true) { //always running
            try {
                final Socket socket = serverSocket.accept();
                // The remote connection address.
                final InetAddress address = socket.getInetAddress();
                log.debug("========================================================================================");
                log.debug("Connection from {} in port {}.", address.getHostAddress(), socket.getPort());
                processConnection(socket);
            } catch (IOException e) {
                log.error("Error", e);
                throw e;
            }
        }
    }
    public static String chatPage() throws IOException {
        String codePage="";
        String body ="";
        String header =
                "<html lang=\"en\">" +
                        "<head>" +
                        "<meta charset=\"UTF-8\">" +
                        "<title>Messenger DSM</title>" +
                        "</head>" +
                        "<body>" +
                        "<h1> Chat DSM-UCN</h1>"+
                        "<div>";
        for (int i = 0; i < getList().size();i++ ) {
            ChatMessage chat = getList().get(i); // this loop add the messages to chat for view in navegator
            body = body + "<p>" + chat.toString() + "</p>";
        }
        body = body +
                "    </div>\n" + // 2 inputs for POST request user + message
                "        <form action=\"/\" method=\"post\" >\n" +
                "            <input type=\"text\" name=\"username\">\n" +
                "            <input type=\"text\" name=\"message\">\n" +
                "            <input type=\"submit\" value=\"Enviar\">\n" +
                "        </form>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
        codePage = header + body;
        return codePage;
    }

    private static List<ChatMessage> getList() {
        return messages;
    }

    private void processConnection(Socket socket)  throws IOException{
        // Reading the inputstream
        final List<String> lines = contentFromSocket(socket);
        final String request = lines.get(0);
        log.debug("Request: {}", request);
        final PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println("HTTP/1.1 200 OK");
        pw.println("Server: DSM v0.0.1");
        pw.println("Date: " + new Date());
        pw.println("Content-Type: text/html; charset=UTF-8");
        pw.println();

        if (request.contains("POST")) {
            //If the type is POST, we must obtain the request body and store it in the database
            //and then show the updated chat
            if (validateAddMessage(lines)) {
                //.println(chatPage());
                pw.println();
                //pw.flush();
            } else {
                pw.println("HTTP/1.1 400 ERROR"); //bad request
                pw.println("Server: DSM v0.0.1");
                pw.println();
                // pw.flush();
            }
        }
    }

    private boolean validateAddMessage(List<String> body) {

        if (body.isEmpty()) {
            return false;
        }
        else{
            String argument = body.get(body.size() - 1);//get post-request elements , user and message
            argument = argument.replace("username=", "").replace("message=","");
            String user = argument.substring(0, argument.indexOf('&'));
            String sentMessage = argument.substring(argument.indexOf('&') + 1, argument.length());
            sentMessage = sentMessage.replace('+', ' ');

            if (user.isEmpty()|| sentMessage.isEmpty()) {
                return false; //message or user is void
            }
            else {
                messages.add(new ChatMessage(user, sentMessage)); // add message to database
                log.debug("Message Added Successfully");
                return true;
            }
        }
    }

    private List<String> contentFromSocket(Socket socket) throws IOException {
        List<String> lines = new ArrayList<String>();
        String line = "";
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        while (true) {
            line = bufferedReader.readLine();
            boolean stateBuffer = true;
            stateBuffer = bufferedReader.ready(); //buffer is ready when the line is not null

            if (stateBuffer && line.isEmpty()) { //if line is a linebreak of request for part body
                int largeBody = 0;
                for(int i =0;i<lines.size();i++){
                    String chatLine = lines.get(i);
                    if (chatLine.contains("Content-Length:")) {
                        largeBody = Integer.parseInt(chatLine.substring(16));
                    } //get size of content in body
                }
                char[] bodyContent = new char[largeBody];
                StringBuilder stringBuilder = new StringBuilder(largeBody);
                for (int i = 0; i < largeBody; i++) {
                    bodyContent[i] = (char)bufferedReader.read();
                }
                lines.add(new String(bodyContent)); //formated body content
                break;

            } else if (!stateBuffer && (line == null || line.isEmpty()) ) {
                log.debug(line); //EOF
                //this conditional is for line void or last line from buffer
                break;

            } else { //simple line not null of request , only add to lines
                log.debug(line);
                lines.add(line);
            }
        }
        if (lines.isEmpty()) { //if the list whit content is void , it is only error
            lines.add("ERROR");
        }
        return lines;
    }

}

