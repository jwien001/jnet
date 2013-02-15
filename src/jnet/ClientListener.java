package jnet;


public interface ClientListener {
    /**
     * Invoked when a message is received from the server.
     * 
     * @param message the message
     */
    void messageReceived(String message);
    
    /**
     * Invoked when the client connects to the server.
     */
    void connected();
    
    /**
     * Invoked when the client is disconnected from the server.
     */
    void disconnected();
}
