package jnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ServerThread extends Thread {
    private final Server server;
    private final Socket socket;
    private final String clientName;
    private final PrintWriter out;
    private final BufferedReader in;

    ServerThread(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        clientName = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    void send(String message) {
        out.println(message);
    }
    
    void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                String str = in.readLine();
                
                if (str == null) 
                    // End of stream reached, so terminate this thread
                    throw new IOException();
                
                server.handle(clientName, str);
            } catch (IOException e) {
                try {
                    server.disconnect(clientName);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            }
        }
    }
    
    String getClientName() {
        return clientName;
    }
}
