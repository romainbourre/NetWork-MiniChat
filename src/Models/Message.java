package Models;

/**
 * Message
 */
public class Message {

    /**
     * Client of message
     */
    private Client client = null;

    /**
     * Content of message
     */
    private String message = null;

    /**
     * Create new message
     * @param client client of message
     * @param message message content
     */
    Message(Client client, String message) {
        this.client = client;
        this.message = message;
    }

    /**
     * Get client of message
     * @return Client
     */
    Client getClient() {
        return client;
    }

    /**
     * Get content of message
     * @return String
     */
    String getMessage() {
        return message;
    }

    /**
     * Get string message
     * @return String
     */
    @Override
    public String toString() {
        if(client != null) return client + " : " + message; else return message;
    }
}
