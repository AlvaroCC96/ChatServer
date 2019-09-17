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
    private static String chatPage() throws IOException {
        String codePage="";
        StringBuilder body = new StringBuilder();
        String header =
                "<html lang=\"en\">" +
                        "<head>" +
                        "<meta charset=\"UTF-8\">" +
                        "<title>Messenger DSM</title>" +
                        "</head>" +
                        "<body>" +
                        "<h1> Chat DSM-UCN</h1>"+
                        "<div>";
        // this loop add the messages to chat for view in navegator
        for (ChatMessage chat : messages) {
            body.append("<p>").append(chat.toString()).append("</p>");
        }
        body.append("    </div>\n").append( // 2 inputs for POST request user + message
                "        <form action=\"/\" method=\"post\" >\n").append("       " +
                "     <input type=\"text\" name=\"username\">\n").append("         " +
                "   <input type=\"text\" name=\"message\">\n").append("         " +
                "   <input type=\"submit\" value=\"Enviar\">\n").append("     " +
                "   </form>\n").append("   " +
                " </div>\n").append("</body>\n").append("</html>");

        codePage = header + body.toString();
        return codePage;
    }

    private void processConnection(Socket socket)  throws IOException{
        // Reading the inputstream
        final List<String> contentSocket = socketRequestContent(socket);
        final String request = contentSocket.get(0);
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
            if (validateAddMessage(contentSocket)) {
                pw.println(chatPage());
                pw.println();
                pw.flush();
            } else {
                pw.println("HTTP/1.1 400 ERROR"); //bad request
                pw.println("Server: DSM v0.0.1");
                pw.println();
                pw.flush();
            }
        }
        else if (request.contains("GET")){
            //if the type is GET, we only show the chat with the message/s
            pw.println(chatPage());
            pw.println();
            pw.flush();
        } else {
            log.debug("ERROR REQUEST");
            pw.println("HTTP/1.1 400 ERROR");
            pw.println("Server: DSM v0.0.1");
            pw.println();
            pw.flush();
        }
        pw.flush();
        socket.close();
        log.debug("Connection Process Finished.");
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

    private List<String>socketRequestContent(Socket socket) throws IOException{

        List<String> content = new ArrayList<String>();
        String record = "";
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        int largeContent =0;
        while (true) {
            record = bufferedReader.readLine();
            if (record.length() != 0) {
                log.debug(record.toString());
                if (record.contains("Content-Length:")){
                    if(Integer.parseInt(record.substring(16) ) !=0) {
                        largeContent =Integer.parseInt(record.substring(16) );
                    }
                    else{
                        break;
                    }
                }
                content.add(record);
            }
            else{
                char[] bodyContent = new char[largeContent];
                StringBuilder stringBuilder = new StringBuilder(largeContent);
                for (int i = 0; i < largeContent; i++) {
                    bodyContent[i] = (char)bufferedReader.read();
                }
                content.add(new String(bodyContent)); //formated body content
                break;
            }
            if (content.isEmpty()) {
                content.add("ERROR");
            }
        }
        return content;
    }
}