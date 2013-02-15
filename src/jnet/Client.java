package jnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;


public class Client implements Runnable {
    private String ipAddress;
    private int portNumber;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ClientListener listener;
    private Thread thread;
    
    /**
     * Creates a new Client networking object without opening a connection.
     * The IP address and port number are set to default values.
     */
    public Client() {
        this("127.0.0.1", 8000);
    }
    
    /**
     * Creates a new Client networking object without opening a connection.
     * The IP address and port number are set to default values.
     * 
     * @param listener an object that will be notified when a message is received
     */
    public Client(ClientListener listener) {
        this("127.0.0.1", 8000, listener);
    }
    
    /**
     * Creates a new Client networking object without opening a connection.
     * 
     * @param ipAddress the IP address of the server to connect to
     * @param portNumber the port number on the server to connect on
     */
    public Client(String ipAddress, int portNumber) {
        try {
            init(ipAddress, portNumber, null, false);
        } catch (IOException e) {
            // This should never happen, since reconnect() will not be called
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a new Client networking object without opening a connection.
     * 
     * @param ipAddress the IP address of the server to connect to
     * @param portNumber the port number on the server to connect on
     * @param listener an object that will be notified when a message is received
     */
    public Client(String ipAddress, int portNumber, ClientListener listener) {
        try {
            init(ipAddress, portNumber, listener, false);
        } catch (IOException e) {
            // This should never happen, since reconnect() will not be called
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a new Client networking object. 
     * If connectNow is true, this will also open a connection to the server.
     * 
     * @param ipAddress the IP address of the server to connect to
     * @param portNumber the port number on the server to connect on
     * @param listener an object that will be notified when a message is received
     * @param connectNow true to connect to the server now; false otherwise
     * @throws IOException if the client fails to connect to the server
     */
    public Client(String ipAddress, int portNumber, ClientListener listener, boolean connectNow) throws IOException {
        init(ipAddress, portNumber, listener, connectNow);
    }
    
    private void init(String ipAddress, int portNumber, ClientListener listener, boolean connectNow) throws IOException {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        if (listener != null)
            this.listener = listener;
        
        if (connectNow)
            reconnect();
    }

    /**
     * Connects to the server at the specified IP address on the specified port number.
     * If a connection is currently open, it will close it first.
     * 
     * @param ipAddress the IP address of the server to connect to
     * @param portNumber the port number on the server to connect on
     * @throws IOException if the client fails to connect to the server
     */
    public void connect(String ipAddress, int portNumber) throws IOException {
        init(ipAddress, portNumber, null, true);
    }
    
    /**
     * Connects to the last server connected to. 
     * If a connection is currently open, it will close it first.
     * 
     * @throws IOException if the client fails to connect to the server
     */
    public void reconnect() throws IOException {
        if (socket != null) {
            close();
            while (thread.isAlive()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddress, portNumber), 4000);
            out = new PrintWriter(socket.getOutputStream(), true);        
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            thread = new Thread(this);
            thread.start();
            connected();
        } catch (IOException e) {
            socket = null;
            out = null;
            in = null;
            throw e;
        }
    }
    
    /**
     * Sends a message to the server if the connection is open.
     * The message should not contain any newline characters.
     * 
     * @param message the message to send to the server
     */
    public void send(String message) {
        if (out == null)
            return;
        
        out.println(message);
    }
    
    /**
     * Closes down the client's connection to the server.
     * 
     * @throws IOException if an error occurs when closing the connection
     */
    public synchronized void close() throws IOException {
        if (socket == null)
            return;
        
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            throw e;
        } finally {
            socket = null;
            out = null;
            in = null;
        }
    }
    
    /**
     * Checks if the client is currently connected to a server.
     * 
     * @return true if the client is connected; false otherwise
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }
    
    public ClientListener getClientListener() {
        return listener;
    }
    
    public void setClientListener(ClientListener listener) {
        this.listener = listener;
    }
    
    synchronized void messageReceived(String message) {
        if (listener != null)
            listener.messageReceived(message);
    }
    
    synchronized void connected() {
        if (listener != null)
            listener.connected();
    }
    
    synchronized void disconnected() {
        if (listener != null)
            listener.disconnected();
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (in == null)
                    // terminate this thread
                    throw new IOException();
                
                String str = in.readLine();
                
                if (str == null) 
                    // End of stream reached, so terminate this thread
                    throw new IOException();
                
                messageReceived(str);
            } catch (IOException e) {
                try {
                    close();
                    disconnected();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        Client c = new Client(new ClientListener() {
            @Override
            public void messageReceived(String message) {
                System.out.println(message);
            }
            
            @Override
            public void disconnected() {
                System.out.println("Disconnected");
            }
            
            @Override
            public void connected() {
            }
        });
        Scanner scan = new Scanner(System.in);

        try {
            c.reconnect();
            String str = scan.nextLine();
            while (c.isConnected()) {
                if (str.equalsIgnoreCase("exit"))
                    break;
                
                c.send(str);
                str = scan.nextLine();
            }

            c.reconnect();
            Thread.sleep(5000);
            c.close();
            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
