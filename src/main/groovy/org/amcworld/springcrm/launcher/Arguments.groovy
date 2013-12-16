/*
 * Arguments.groovy
 *
 * Copyright (c) 2011-${year}, Daniel Ellermann
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

import groovy.util.logging.Log4j


/**
 * The class {@code Arguments} represents a container for the command line
 * arguments.
 *
 * @author  Daniel Ellermann
 * @version 1.0
 */
@Log4j
class Arguments {

    //-- Constants ------------------------------

    protected static final List<String> SUPPORTED_ARGS = [
        'context', 'host', 'port', 'httpsPort', 'keystorePath',
        'javax.net.ssl.keyStore', 'keystorePassword',
        'javax.net.ssl.keyStorePassword', 'trustStorePath',
        'javax.net.ssl.trustStore', 'trustStorePassword',
        'javax.net.ssl.trustStorePassword', 'enableClientAuth', 'workDir',
        'enableCompression', 'compressableMimeTypes', 'sessionTimeout', 'nio',
        'tomcat.nio', 'war'
    ].asImmutable()


    //-- Instance variables ---------------------

    Map<String, String> args = [: ]


    //-- Constructors ---------------------------

    Arguments(String [] args) {
        convertToMap args
    }


    //-- Properties -----------------------------

    /**
     * Gets the context path.
     *
     * @return  the context path
     */
    String getContextPath() {
        String path = get('context', '').trim()
        if (path && !path.startsWith('/')) path = '/' + path
        path
    }

    /**
     * Gets the working directory used to expand archives.
     *
     * @return  the working directory
     */
    File getWorkDir() {
        new File(get('workDir', get('java.io.tmpdir')))
    }


    //-- Public methods -------------------------

    /**
     * Gets the value of the command line argument with the given name.
     *
     * @param name  the given name of the argument
     * @return      the value
     */
    String get(String name) {
        get name, null
    }

    /**
     * Gets the value of the command line argument with the given name.  The
     * method uses the given default value if the argument value is
     * {@code null}.
     *
     * @param name          the given name of the argument
     * @param defaultValue  the value that should be used if the command line
     *                      argument is unset
     * @return              the value
     */
    String get(String name, String defaultValue) {
        String value = args[name]
        if (value == null) {
            if (System.properties.containsKey(name)) {
                value = System.getProperty(name)
            }
        }
        if (value == null) {
            value = defaultValue
        } else if (System.properties.containsKey(value)) {
            value = System.getProperty(value)
        }

        value
    }

    /**
     * Gets the value of the command line argument with the given name.  If no
     * argument with the given name was specified an empty string is used.
     *
     * @param name  the given name of the argument
     * @return      the value
     */
    String getAt(String name) {
        get name, ''
    }

    /**
     * Gets the value of the command line argument with the given name as
     * boolean.  The method uses the given default value if the argument value
     * is {@code null}.
     *
     * @param name          the given name of the argument
     * @param defaultValue  the value that should be used if the command line
     *                      argument is unset
     * @return              the value
     */
    boolean getBoolean(String name, boolean defaultValue) {
        'true'.equalsIgnoreCase get(name, String.valueOf(defaultValue))
    }

    /**
     * Gets the value of the command line argument with the given name as
     * integer.  The method uses the given default value if the argument value
     * is {@code null}.
     *
     * @param name          the given name of the argument
     * @param defaultValue  the value that should be used if the command line
     *                      argument is unset
     * @return              the value
     */
    int getInt(String name, int defaultValue) {
        int res = defaultValue
        String value = args[name]
        if (value) {
            try {
                res = Integer.parseInt(value)
            } catch (NumberFormatException e) { /* ignored */ }
        }

        res
    }


    //-- Non-public methods ---------------------

    /**
     * Converts the given command line arguments in the form {@code name=value}
     * to a map.
     *
     * @param args  the arguments that should be converted
     */
    protected void convertToMap(String [] args) {
        for (String arg in args) {
            int index = arg.indexOf('=')
            if (index == -1) {
                throw new ArgumentException(
                    "Warning, arguments must be specified in name=value format, ignoring argument '${arg}'"
                )
            }

            String name = arg.substring(0, index).trim()
            if (!(name in SUPPORTED_ARGS)) {
                throw new ArgumentException(
                    "Warning, the specified argument '${name}' is not supported, ignoring"
                )
            }

            this.args[name] = arg.substring(index + 1).trim()
        }
    }
}
