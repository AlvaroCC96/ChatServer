/*
 * Copyright (c) 2019. This code is purely educational, the rights of use are
 * reserved, the owner of the code is Alvaro Castillo Calabacero,
 * contact alvaro.castillo@alumnos.ucn.cl
 * Do not use in production.
 */

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

/**
 * @author Alvaro Castillo
 */
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
    /**
     * Process of conecction , de actions depends of type of request
     * @param socket
     * @throws IOException
     */
    private void processConnection(Socket socket)  throws IOException{
        // Reading the inputstream
        final List<String> contentSocket = socketRequestContent(socket); //get de content from socket (get or post)
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
            if (addToDB(contentSocket)) {
                pw.println(htmlCode());
                pw.println();
                pw.flush();
            } else {
                pw.println("HTTP/1.1 400 ERROR"); //bad request
                pw.println("Server: DSM v0.0.1");
                pw.println();
                pw.flush();
            }
        } else if (request.contains("GET")){
            //if the type is GET, we only show the chat with the message/s
            pw.println(htmlCode());
            pw.println();
            pw.flush();
        } else {
            log.error("ERROR REQUEST");
            pw.println("HTTP/1.1 400 ERROR");
            pw.println("Server: DSM v0.0.1");
            pw.println();
            pw.flush();
        }
        pw.flush();
        socket.close();
        log.debug("Connection Process Finished.");
    }

    /**
     * get the content of the request and store it in a list
     * @param socket
     * @return
     * @throws IOException
     */
    private List<String>socketRequestContent(Socket socket) throws IOException{
        List<String> content = new ArrayList<String>();
        String record = "";
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        int largeContent =0; //for body of request
        while (true) {
            record = bufferedReader.readLine(); //line of request
            if (record.length() != 0) { //if is not null or void , get content
                log.debug(record.toString());
                if (record.contains("Content-Length:")){ //if content contain this text , save de large of content
                    if(Integer.parseInt(record.substring(16) ) !=0) {
                        largeContent =Integer.parseInt(record.substring(16));
                    } else {
                        break;
                    }
                }
                content.add(record); //save all de lines of content, eexcept the empty lines
            } else { //get de body content whit de largeContent , get all lines includ empty lines
                char[] bodyContent = new char[largeContent];
                StringBuilder stringBuilder = new StringBuilder(largeContent);
                for (int i = 0; i < largeContent; i++) {
                    bodyContent[i] = (char)bufferedReader.read();
                }
                content.add(new String(bodyContent)); //formated body content
                return content;
            }
        }
        return content; //return list whit content of request
    }

    /**
     * function that obtains as a string an html file from the server
     * @return a string that contains html of server whit messages
     * @throws IOException
     */
    private static String htmlCode() throws IOException{
        FileReader fileReader = new FileReader("./src/html/index.html");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ( ( line = bufferedReader.readLine() ) != null ) {
            if (line.contains("chat-content")) { //if detect div for messages , add messages to website
                for (ChatMessage chat : messages) {
                    stringBuilder.append("<p>").append(chat.toString()).append("</p>");
                }
            }
            stringBuilder.append(line); //add content from html , to stringBuilder
        }
        bufferedReader.close();
        return  stringBuilder.toString();
    }
    /**
     * function that validates the message, if valid is added to the database to be displayed on the website
     * @param body , is content of request
     * @return bool , it depends if the message was valid or not
     */
    public boolean addToDB(List<String> body) {
        if (!body.isEmpty()) {
            String line ="";
            for (String s : body) {
                line = s;
                if (line.contains("username") && line.contains("message")) {
                    log.debug(line);
                    break;//get post-request elements , user and message
                }
            }
            log.debug(line);
            String user , msgReceived;
            line = line.replace("username=", "").replace("message=", "");
            log.debug(line); //formated ["usuario"&"mensaje"]
            int pos = line.indexOf("&");
            user = line.substring(0,pos).replace("+"," ");
            msgReceived = line.substring(pos+ 1).replace("+"," ");

            if (validateUserMessage(user,msgReceived)) {
                ChatMessage chat = new ChatMessage(user, msgReceived);
                messages.add(chat); // add message to database
                log.debug("Message Added Successfully");
                return true;
            }
            log.error("Invalid message");
            return false;
        }
        log.error("Body is void");
        return false; //body is void
    }
    /**
     * validate de content of message , user and message from post content
     * @param user
     * @param msg
     * @return : if msg and user is valid return true
     */
    private static boolean validateUserMessage(String user, String msg){
        if (!user.isEmpty()) {
            if (msg.isEmpty()) {
                log.error("Error: Message is void");
                return false; //message is void
            }
            return true;
        }
        log.error("User is void");
        return false;
    }
}