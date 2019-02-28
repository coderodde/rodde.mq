package net.coderodde.mq;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Logger;

/**
 * This class groups all miscellaneous utilities.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Feb 26, 2019)
 */
final class Utilities {
    
    /**
     * Reports an {@link java.io.IOException} and rethrows it via a
     * {@link java.lang.RuntimeException}.
     * 
     * @param ioException the exception to report.
     * @param logger      the logger to print to.
     */
    static void reportAndThrowIOException(IOException ioException,
                                          Logger logger) {
        logger.severe("IOException.getMessage(): " + ioException.getMessage());
        throw new RuntimeException(ioException);
    }
    
    /**
     * Reports an {@link java.net.SocketException} and rethrows it via a
     * {@link java.lang.RuntimeException}.
     * 
     * @param ioException the exception to report.
     * @param logger      the logger to print to.
     */
    static void reportAndThrowSocketException(
            SocketException ioException,
            Logger logger) {
        logger.severe("SocketException.getMessage(): " + ioException.getMessage());
        throw new RuntimeException(ioException);
    }
    
    /**
     * Checks that the port number is within range {@code [0, 65535]}.
     * 
     * @param portNumber the port number to check.
     * @return a valid port number value.
     * @throws IllegalArgumentException if the input port number is negative or
     *         greater than 65535.
     */
    static int checkPortNumber(int portNumber) {
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
    
