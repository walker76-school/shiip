/************************************************
 * Author: Andrew Walker
 * Assignment: Prog1
 * Class: CSI 4321
 ************************************************/

package shiip.serialization;

import java.io.Serializable;

/**
 * Thrown if problem with attribute
 * @author Andrew Walker
 */
public class BadAttributeException extends Exception implements Serializable {

    // Unique ID for BadAttributeException
    private static final long serialVersionUID = 16L;

    // Bad attribute
    private String attribute;

    /**
     * Constructs a BadAttributeException with given message, attribute, and cause
     * @param message detail message (null permitted)
     * @param attribute attribute related to problem (null permitted)
     * @param cause underlying cause (null is permitted and indicates no or
     *              unknown cause)
     */
    public BadAttributeException(String message, String attribute, Throwable cause){
        super(message, cause);
        this.attribute = attribute;
    }

    /**
     * Constructs a BadAttributeException with given message and attribute with
     * no given cause
     * @param message detail message
     * @param attribute attribute related to problem
     */
    public BadAttributeException(String message, String attribute){
        this(message, attribute, null);
    }

    /**
     * Return attribute related to problem
     * @return attribute name
     */
    public String getAttribute(){
        return attribute;
    }
}
