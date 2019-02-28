package net.coderodde.mq;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * This class implements a type instances of which provide means for receiving
 * binary messages from any producers writing to the specified queue.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Feb 27, 2019)
 */
public final class MessageQueueConsumer implements AutoCloseable {
    
    /**
     * The string representing the IP address of the message queue handler host.
     */
    private final String hostName;
    
    /**
     * The port number the message queue handler thread listens to.
     */
    private final int portNumber;
    
    /**
     * The socket connected to the message queue handler host.
     */
    private final Socket socket;
    
    /**
     * The logger object used for reporting exceptions.
     */
    private Logger logger;
    
    public MessageQueueConsumer(String messageQueueName,
                                String hostName,
                                int portNumber) throws IOException {
        this.hostName = 
                Objects.requireNonNull(
                        hostName, 
                        "The input host name is null.");
        
        this.portNumber = checkPortNumber(portNumber);
        this.socket = preamble();
        
        try (OutputStream out = socket.getOutputStream()) {
            
            // Send the role of this consumer to the queue handler:
            byte[] roleNameBytes = MagicConstants.CONSUMER_STRING.getBytes();
            out.write(roleNameBytes.length);
            out.write(roleNameBytes);
            
            // Send the name of the queue this consumer listens to:
            byte[] messageQueueNameBytes = messageQueueName.getBytes();
            out.write(messageQueueNameBytes.length);
            out.write(messageQueueNameBytes);
        }
    }
    
    public byte[] consume() throws IOException {
        try (InputStream in = socket.getInputStream()) {
            int bytesLength = in.read();
            byte[] message = new byte[bytesLength];
            in.read(message);
            return message;
        }
    }
    
    @Override
    public void close() throws Exception {
        try (OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {
            out.write(MagicConstants.CLOSE_SENTINEL);
            out.close();
            in.close();
            socket.close();
        }
    }
    
    private Socket preamble() {
        Socket socket = null;
        
        try {
            socket = new Socket(hostName, portNumber);
        } catch (UnknownHostException ex) {
            if (logger != null) {
                logger.severe("Failed upon socket creation: " +
                              ex.getMessage());
            }
        } catch (IOException ex) {
            if (logger != null) {
                logger.severe("Failed upon socket creation: " + 
                              ex.getMessage());
            }
        }
        
        return socket;
    }
 
    private int checkPortNumber(int portNumber) {
        if (portNumber < 0) {
            throw new IllegalArgumentException(
                    "The input port number is negative: " + portNumber);
        }
        
        if (portNumber >= 256 * 256) {
            throw new IllegalArgumentException(
                    "The input port number is too large: " + portNumber);
        }
        
        return portNumber;
    }
}
