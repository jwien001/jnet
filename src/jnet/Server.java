package jnet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class Server implements Runnable {
    private ServerSocket serverSocket;
    private ServerListener listener;    
    private final Map<String, ServerThread> clients;
    private Thread thread;
    
    /**
     * Creates a new Server.
     */
    public Server() {
        clients = new HashMap<String, ServerThread>();
    }
    
    /**
     * Creates a new Server and begins listening on the specified port.
     * 
     * @param portNumber the port number to listen on
     * @param listener an object that will be notified when a message is received
     * @throws IOException if the server fails to open a socket
     */
    public Server(int portNumber, ServerListener listener) throws IOException {
        this.listener = listener;
        clients = new HashMap<String, ServerThread>();
        open(portNumber);
    }
    
    /**
     * Opens a server socket and begins listening on the specified port.
     * 
     * @param portNumber the port number to listen on
     * @throws IOException if the server fails to open a socket
     */
    public synchronized void open(int portNumber) throws IOException {
        serverSocket = new ServerSocket(portNumber);
        thread = new Thread(this);
        thread.start();
    }
    
    /**
     * Disconnects all clients and closes down the server.
     * 
     * @throws IOException if an error occurs when closing the server
     */
    public synchronized void close() throws IOException {
        for (String clientName : clients.keySet())
            disconnect(clientName);
        
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw e;
        } finally {
            serverSocket = null;
        }
    }
    
    public boolean isOpen() {
        return serverSocket != null && serverSocket.isBound();
    }
    
    public ServerListener getServerListener() {
        return listener;
    }
    
    public void setServerListener(ServerListener listener) {
        this.listener = listener;
    }
    
    /**
     * Sends a message to the specified client.
     * 
     * @param clientName the name of the client to send to (the same name passed into {@link ServerListener#messageReceived(Server, String, String)})
     * @param message the message to send
     * @return true if the clientName was valid and the message sent, false otherwise
     */
    public boolean send(String clientName, String message) {
        ServerThread client = clients.get(clientName);
        if (client == null)
            return false;
        
        client.send(message);
        return true;
    }

    /**
     * Disconnects the specified client.
     * 
     * @param clientName the name of the client to disconnect (the same name passed into {@link ServerListener#messageReceived(Server, String, String)})
     * @returns true if the clientName was valid and the client disconnected, false otherwise
     * @throws IOException if an error occurs when disconnecting the client
     */
    public synchronized boolean disconnect(String clientName) throws IOException {
         ServerThread client = clients.remove(clientName);
         if (client == null)
             return false;
         
         client.close();
         return true;
    }

    void messageReceived(String clientName, String message) {
        if (listener != null)
            listener.messageReceived(clientName, message);
    }
    
    void clientConnected(String clientName) {
        if (listener != null)
            listener.clientConnected(clientName);
    }
    
    void clientDisconnected(String clientName) {
        if (listener != null)
            listener.clientDisconnected(clientName);
    }
    
    @Override
    public void run() {
        while (true) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                try {
                    close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            }
            
            ServerThread newClient;
            try {
                newClient = new ServerThread(this, clientSocket);
                clients.put(newClient.getClientName(), newClient);
                newClient.start();

                clientConnected(newClient.getClientName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            final Server s = new Server();
            s.setServerListener(new ServerListener() {                
                @Override
                public void messageReceived(String clientName, String message) {
                    System.out.println("Received: " + message + " from " + clientName);
                    for (int i=1; i<=5; i++) {
                        System.out.println("Waiting " + i + "...");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    s.send(clientName, message + message);
                }

                @Override
                public synchronized void clientConnected(String clientName) {
                    System.out.println(clientName + " connected");
                }

                @Override
                public synchronized void clientDisconnected(String clientName) {
                    System.out.println(clientName + " disconnected");
                }
            });
            
            s.open(8000);
            
            while (true) {
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
