package jnet;


public interface NetworkingListener {
    /**
     * Invoked when a message is received from a client.
     * By default, this method is not synchronized, so it may be called by more than one thread at a time.
     * To make it synchronous, simply add the {@code synchronized} keyword to your implmentation's header.
     * 
     * @param message the message
     * @param address the IP address and port number of the connected client, separated by a colon
     * @return a response to the client, or null if no response is necessary
     */
    String messageReceived(String message, String address);
}
