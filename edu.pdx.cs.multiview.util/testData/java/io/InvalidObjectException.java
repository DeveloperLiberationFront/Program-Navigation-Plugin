/*
 * @(#)InvalidObjectException.java	1.16 04/01/12
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.io;

/**
 * Indicates that one or more deserialized objects failed validation
 * tests.  The argument should provide the reason for the failure.
 *
 * @see ObjectInputValidation
 * @since JDK1.1
 *
 * @author  unascribed
 * @version 1.16, 01/12/04
 * @since   JDK1.1
 */
public class InvalidObjectException extends ObjectStreamException {
    /**
     * Constructs an <code>InvalidObjectException</code>.
     * @param reason Detailed message explaining the reason for the failure.
     *
     * @see ObjectInputValidation
     */
    public  InvalidObjectException(String reason) {
	super(reason);
    }
}
