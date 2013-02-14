package jnet;


public interface ServerListener {
    /**
     * Invoked when a message is received from a client.
     * By default, this method is not synchronized, so it may be called by more than one thread at a time.
     * To make it synchronous, simply add the {@code synchronized} keyword to your implmentation's header.
     * 
     * @param server the server that received the message
     * @param clientName the unique name of the client, which is its IP address and port number separated by a colon
     * @param message the message
     */
    void messageReceived(Server server, String clientName, String message);
}