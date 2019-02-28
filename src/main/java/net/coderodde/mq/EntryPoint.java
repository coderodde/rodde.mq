package net.coderodde.mq;

import java.io.IOException;
import java.util.logging.Logger;
import static net.coderodde.mq.MagicConstants.TextResources.ENTRY_SUBSYSTEM_NAME;
import static net.coderodde.mq.Utilities.checkPortNumber;

/**
 * This class defines the entry point for the message queue handler.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6
 */
public final class EntryPoint {
    
    private static final Logger LOGGER = Logger.getLogger(ENTRY_SUBSYSTEM_NAME);
    
    public static void main(String[] args) {
        int portNumber = MagicConstants.DEFAULT_PORT_NUMBER;
        
        switch (args.length) {
            case 0:
                break;
                
            case 1:
                portNumber = checkPortNumber(portNumber);
                break;
                
            default:
                LOGGER.severe(
                        String.format(
                                MagicConstants
                                .TextResources
                                .INVALID_ARGUMENT_COUNT_ERROR_MESSAGE_FORMAT, 
                                args.length));
                
                System.out.println(MagicConstants.TextResources.HELP_MESSAGE);
                System.exit(1);
        }
        LOGGER.info("Using port number " + portNumber + ".");
        MessageQueueHandler messageQueueHandler = null;

        try {
            messageQueueHandler = new MessageQueueHandler(portNumber);
        } catch (IOException ex) {
            LOGGER.severe("IOException: " + ex.getMessage());
            System.exit(1);
        }

        messageQueueHandler.run();
        MessageQueueConsumer messageQueueConsumer;
        
        try {
            messageQueueConsumer = 
                    new MessageQueueConsumer("my-queue", 
                                             "127.0.0.1", 
                                             portNumber);
        } catch (IOException ex) {
            System.out.println("oh shit");
        }
        
    }       
}

