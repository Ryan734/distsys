import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Message class for queue communication
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String messageId;
    private String type;
    private String content;
    private Date timestamp;
    private String sender;
    private int retryCount;
    
    public Message(String type, String content, String sender) {
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.timestamp = new Date();
        this.retryCount = 0;
    }
    
    // Getters and setters
    public String getMessageId() { return messageId; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public Date getTimestamp() { return timestamp; }
    public String getSender() { return sender; }
    public int getRetryCount() { return retryCount; }
    public void incrementRetryCount() { this.retryCount++; }
}
