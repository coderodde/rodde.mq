package net.coderodde.mq;

/**
 * This class groups all the magic numbers and constants used in rodde.mq.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Feb 26, 2019)
 */
final class MagicConstants {
    
    /**
     * Is written to a socket's output stream in order to communicate that the
     * sender is closing.
     */
    static final int CLOSE_SENTINEL = -1;
    
    /**
     * The string specifying that a new connection comes from a consumer.
     */
    static final String CONSUMER_STRING = "consumer";
    
    /**
     * Specifies that by default the server accepting socket does not time out.
     */
    static final int DEFAULT_SERVER_SOCKET_ACCEPT_TIMEOUT = 0;
    
    /**
     * Used for identifying which log lines belong to rodde.mq.
     */
    static final String LOGGER_BADGE = "[rodde.mq]";
    
    /**
     * The port number to use in the communication.
     */
    static final int DEFAULT_PORT_NUMBER = 18273;
    
    /**
     * The string specifying that a new connection comes from a producer.
     */
    static final String PRODUCER_STRING = "producer";
    
    /**
     * Groups all the string constants.
     */
    static final class TextResources {
        
        /**
         * Used by the {@link net.coderodde.mq.EntryPoint} logging.
         */
        static final String ENTRY_SUBSYSTEM_NAME = "[EntryPoint]";
        
        /**
         * Defines the help message.
         */
        static final String HELP_MESSAGE = 
                "Usage: java -jar rodde.mq.1.6.jar [PORT]";
        
        /**
         * The message format for logging the wrong number of arguments.
         */
        static final String INVALID_ARGUMENT_COUNT_ERROR_MESSAGE_FORMAT = 
                "At most one argument (port number) is allowed. %d received.";
    }
}
