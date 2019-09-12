package cl.ucn.disc.dsm.chat;
import java.time.LocalDateTime;

public class ChatMessage {
    /**
     * atributes message class
     * */
    private final LocalDateTime timestamp;
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
}

