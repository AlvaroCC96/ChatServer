/*
 * Copyright (c) 2019. This code is purely educational, the rights of use are
 * reserved, the owner of the code is Alvaro Castillo Calabacero,
 * contact alvaro.castillo@alumnos.ucn.cl
 * Do not use in production.
 */

package cl.ucn.disc.dsm.chat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Alvaro Castillo
 */
public class ChatMessage {
    /**
     * atributes message class
     */
    private final LocalDateTime timestamp; // for record message
    private final String username;
    private final String message;

    /**
     * Constructor of class ChatMessage
     * @param user
     * @param contentMessage
     */
    public ChatMessage(String user,String contentMessage){
        timestamp = LocalDateTime.now(); //datetime from server clock
        username= user;
        message=contentMessage;
    }

    /**
     * Function that returns a message with user time date and message
     * @return message chat formated
     */
    public String toString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder chat = new StringBuilder();
        chat.append(timestamp.format(formatter)).append(" ").append(username).append(":  ").append(message);//formated
        return chat.toString(); //create string
    }
}