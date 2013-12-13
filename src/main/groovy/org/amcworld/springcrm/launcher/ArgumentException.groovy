/*
 * ArgumentException.groovy
 *
 * Copyright (c) 2011-2013, Daniel Ellermann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.amcworld.springcrm.launcher


/**
 * The class {@code ArgumentException} represents an exception concerning the
 * command line arguments.
 *
 * @author  Daniel Ellermann
 * @version 1.0
 */
class ArgumentException extends Exception {

    //-- Constructors ---------------------------

    ArgumentException() {
        super()
    }

    ArgumentException(String message, Throwable cause) {
        super(message, cause)
    }

    ArgumentException(String message) {
        super(message)
    }

    ArgumentException(Throwable cause) {
        super(cause)
    }
}
