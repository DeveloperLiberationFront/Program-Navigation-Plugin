/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.callhierarchy;

/**
 * Class for the real callers.
 * 
 * @since 3.5
 */
	public class RealCallers extends CallerMethodWrapper {

		/**
		 * Sets the parent method wrapper.
		 * 
		 * @param methodWrapper the method wrapper
		 * @param methodCall the method call
		 */
		public RealCallers(MethodWrapper methodWrapper, MethodCall methodCall) {
			super(methodWrapper, methodCall);
	}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper#canHaveChildren()
		 */
		public boolean canHaveChildren() {
			return true;
	}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper#isRecursive()
		 */
		public boolean isRecursive() {
			return false;
		}
	}
