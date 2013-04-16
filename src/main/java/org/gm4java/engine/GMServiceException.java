/*
 * Copyright (c) 2011 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gm4java.engine;

/**
 * 
 * Indicates error communicating with the GraphicsMagick process.
 * 
 * @author Kenneth Xu
 * 
 */
public class GMServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with given message.
     * 
     * @param message
     *            the detailed message of the exception.
     */
    public GMServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with given message and cause.
     * 
     * @param message
     *            the detailed message of the exception.
     * @param cause
     *            the cause of the error.
     */
    public GMServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
