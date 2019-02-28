package net.coderodde.mq;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.logging.Logger;
import static net.coderodde.mq.Utilities.checkPortNumber;

/**
 * This class implements a type instances of which provide means for sending 
 * binary messages to any of the consumer reading from the specified queue.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Feb 29, 2019)
 */
public final class MessageQueueProducer implements AutoCloseable {
    
    /**
     * The string representation of the IP address of the message queue handler
     * server.
     */
    private final String hostName;
    
    /**
     * The port number the message queue handler thread listens to.
     */
    private final int portNumber;
    
    /**
     * The socket connected to the message queue handler.
     */
    private final Socket socket;
    
    /**
     * The logger object used for reporting exceptions.
     */
    private Logger logger;
    
    /**
     * Constructs this message queue producer and connects it to a specified
     * message queue handler.
     * 
     * @param messageQueueName the name of the queue this producer produces.
     * @param hostName         the IP address of the message queue handler.
     * @param portNumber       the port number to use.
     * @throws UnknownHostException if could not reach the message queue 
     *                              handler.
     * @throws IOException if network I/O fails.
     */
    public MessageQueueProducer(String messageQueueName,
                                String hostName, 
                                int portNumber) 
    throws UnknownHostException,
           IOException {
        this.hostName = 
                Objects.requireNonNull(
                        hostName, 
                        "The input host name is null.");
        
        this.portNumber = checkPortNumber(portNumber);
        this.socket = preamble();
        
        try (OutputStream out = socket.getOutputStream()) {
            // Send the role of this producer to the queue handler:
            byte[] roleNameBytes = MagicConstants.PRODUCER_STRING.getBytes();
            out.write(roleNameBytes.length);
            out.write(roleNameBytes);
            
            // Send the name of the queue this producer operates on:
            byte[] messageQueueNameBytes = messageQueueName.getBytes();
            out.write(messageQueueNameBytes.length);
            out.write(messageQueueNameBytes);
        }
    }
    
    /**
     * Sends a binary message {@code bytes} to the message queue handler this
     * producer is connected to.
     * 
     * @param bytes the message data.
     * @throws IOException if the network I/O fails.
     */
    public void produce(byte[] bytes) throws IOException {
        try (OutputStream out = socket.getOutputStream()) {
            out.write(bytes.length);
            out.write(bytes);
        }
    }
    
    /**
     * Closes the I/O facilities this producer relies on.
     * 
     * @throws Exception 
     */
    @Override
    public void close() throws Exception {
        try (OutputStream out = socket.getOutputStream()) {
            out.write(MagicConstants.CLOSE_SENTINEL);
            out.close();
            socket.close();
        }
    }
    
    /**
     * Sets the logger.
     * 
     * @param logger the logger to set, or {@code null} in order to turn off
     * l             logging.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    /**
     * Connects the socket to a specified host.
     * 
     * @return the communication socket.
     */
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
}
