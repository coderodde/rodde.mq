package net.coderodde.mq;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Feb 26, 2019)
 */
public final class MessageQueueHandler extends Thread {
    
    
    /**
     * Used for logging status information in a console or another listener.
     */
    private static final Logger LOGGER =
            Logger.getLogger(MagicConstants.LOGGER_BADGE);
    
    /**
     * Specifies a flag for halting the inner service loop.
     */
    private boolean haltRequested; // Default value is false.
    
    /**
     * Maps queue names to their actual queues.
     */
    private final Map<String, Queue<byte[]>>
            queueNameToLinkedQeueueMap = new ConcurrentHashMap<>();
    
    /**
     * Maps each socket to the name of the queue.
     */
    private final Map<Socket, String> socketToQueueNameMap =
            new ConcurrentHashMap<>();
    
    /**
     * The listening server socket.
     */
    private final ServerSocket serverSocket;
    
    /**
     * The port number this handler listens to.
     */
    private final int portNumber;
    
    /**
     * Constructs a message queue handler using a particular port.
     * 
     * @param portNumber the number of the port to listen for.
     * @throws IOException if network I/O fails.
     */
    public MessageQueueHandler(int portNumber) throws IOException {
        this.portNumber = checkPortNumber(portNumber);
        this.serverSocket = new ServerSocket(checkPortNumber(portNumber));
    }
    
    public MessageQueueHandler() throws IOException {
        this(MagicConstants.DEFAULT_PORT_NUMBER);
    }
    
    @Override
    public void run() {
        ServerSocket serverSocket = null;
        
        try {
            serverSocket = new ServerSocket(portNumber);                    
        } catch (IOException ex) {
            Utilities.reportAndThrowIOException(ex, LOGGER);
        }
        
        try {
            serverSocket.setSoTimeout(
                    MagicConstants.DEFAULT_SERVER_SOCKET_ACCEPT_TIMEOUT);
        } catch (SocketException ex) {
            Utilities.reportAndThrowSocketException(ex, LOGGER);
        }
        
        while (!haltRequested) {
            Socket socket;
            
            try {
                // New client connected:
                socket = serverSocket.accept();
                
                // Read the type name:
                int typeNameBytesLength = socket.getInputStream().read();
                byte[] typeNameBytes = new byte[typeNameBytesLength];
                socket.getInputStream().read(typeNameBytes);
                String typeName = new String(typeNameBytes);
                System.out.println("Type name: " + typeName);
                
                // Read the message queue name:
                int messageQueueNameLength = socket.getInputStream().read();
                byte[] messageQueueNameBytes = new byte[messageQueueNameLength];
                socket.getInputStream().read(messageQueueNameBytes);
                String messageQueueName = new String(messageQueueNameBytes);
                System.out.println("Queue name: " + messageQueueName);
                
                switch (typeName) {
                    case MagicConstants.PRODUCER_STRING:
                        socketToQueueNameMap.put(socket, messageQueueName);
                        
                        if (!queueNameToLinkedQeueueMap
                                .containsKey(messageQueueName)) {
                            queueNameToLinkedQeueueMap.put(
                                        messageQueueName, 
                                        new ConcurrentLinkedQueue<>());
                        }
                        
                        handleProducerThread(
                                socket,
                                queueNameToLinkedQeueueMap.get(messageQueueName));
                        break;
                        
                    case MagicConstants.CONSUMER_STRING:
                        handleConsumerThread(socket);
                        break;
                        
                    default:
                        throw new RuntimeException(
                                "Unknown client type name: " + typeName);
                }
            } catch (IOException ex) {
                Utilities.reportAndThrowIOException(ex, LOGGER);
            }
        }
    }
    
    private void setTimeout(int milliseconds) throws SocketException {
        serverSocket.setSoTimeout(milliseconds);
    }
    
    private void handleProducerThread(Socket socket, Queue<byte[]> queue) {
        ProducerThread pt = new ProducerThread(socket, queue);
        pt.start();
        
        try {
            pt.join();
        } catch (InterruptedException ex) {
            // Do nothing since we do not expect any InterruptedException.
        }
    }
    
    private void handleConsumerThread(Socket socket) {
        
    }
    
    /**
     * This inner class implements a server thread listening to a producer 
     * client. 
     */
    private static final class ProducerThread extends Thread {
        
        /**
         * The socket connected to a producer client.
         */
        private final Socket socket;
        
        /**
         * The actual queue storing the messages in FIFO-manner.
         */
        private final Queue<byte[]> queue;
        
        /**
         * Is used to halt the execution of this thread's queue in FIFO-order.
         */
        private boolean haltRequested; // Default value is 'false'.
        
        ProducerThread(Socket socket, Queue<byte[]> queue) {
            this.socket = socket;
            this.queue = queue;
        }
        
        public void requestHalt() {
            this.haltRequested = true;
        }
        
        @Override
        public void run() {
            while (!haltRequested) {
                try (InputStream in = socket.getInputStream()) {
                    int messageBytesLength = in.read();
                    byte[] messageBytes = new byte[messageBytesLength];
                    in.read(messageBytes);
                    queue.add(messageBytes);
                } catch (IOException ex) {
                    Logger.getLogger(MessageQueueHandler.class.getName())
                          .log(Level.SEVERE, 
                               "Could not produce a message.", 
                               ex);
                }
            }
        }
    }
    
    private static final class ConsumerThread extends Thread {
        
        /**
         * The default number of milliseconds for waiting upon an empty queue.
         */
        private static final int DEFAULT_EMPTY_QUEUE_SLEEP_DURATION = 1_000;
        
        /**
         * The number of milliseconds to wait for an empty queue..
         */
        private int emptyQueueSleepDuration =
                DEFAULT_EMPTY_QUEUE_SLEEP_DURATION;
        
        private final Socket socket;
        
        private final Queue<byte[]> queue;
        
        private boolean haltRequested;
        
        ConsumerThread(Socket socket, Queue<byte[]> queue) {
            this.socket = socket;
            this.queue = queue;
        }
        
        public void requestHalt() {
            this.haltRequested = true;
        }
        
        @Override
        public void run() {
            while (!haltRequested) {
                if (queue.isEmpty()) {
                    try {
                        Thread.sleep(emptyQueueSleepDuration);
                    } catch (InterruptedException ex) {
                        
                    }
                }
            }
        }
        
        void setEmptyQueueWaitMilliseconds(int milliseconds) {
            
        }
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
