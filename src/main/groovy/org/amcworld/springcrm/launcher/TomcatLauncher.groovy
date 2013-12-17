/*
 * TomcatLauncher.groovy
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

import groovy.util.logging.Log4j
import org.apache.catalina.Context
import org.apache.catalina.LifecycleException
import org.apache.catalina.connector.Connector
import org.apache.catalina.startup.Tomcat
import org.apache.coyote.http11.Http11NioProtocol


/**
 * The class {@code TomcatLauncher} represents a launcher of Tomcat.  It offers
 * methods to start and stop a Tomcat instance.
 *
 * @author  Daniel Ellermann
 * @version 1.0
 */
@Log4j
class TomcatLauncher {

    //-- Instance variables ---------------------

    protected Arguments args
    protected boolean autostart
    protected Connector connector
    protected Context context
    protected GuiControls controls
    protected Extractor extractor
    protected GuiOutput output
    protected Tomcat tomcat = new Tomcat()


    //-- Public methods -------------------------

    /**
     * Configures Tomcat and starts it.
     */
    void start() {
        File workDir = args.workDir

        String keystorePath = args.get(
            'keystorePath', args['javax.net.ssl.keyStore']
        )
        String keystorePassword = args.get(
            'keystorePassword', args['javax.net.ssl.keyStorePassword']
        )
        File keystoreFile
        if (keystorePath) {
            keystoreFile = new File(keystorePath)
        } else {
            keystoreFile = new File(workDir, 'ssl/keystore')
            keystorePassword = '123456'
        }

        File tomcatDir = new File(workDir, 'grails-standalone-tomcat')
        tomcatDir.deleteDir()

        String nio = args.get('nio', args['tomcat.nio'])

        configureTomcat(
            compressableMimeTypes: args['compressableMimeTypes'],
            contextPath: args.contextPath,
            destDir: extractor.destDir,
            enableClientAuth: args.getBoolean('enableClientAuth', false),
            enableCompression: args.getBoolean('enableCompression', true),
            host: args.get('host', 'localhost'),
            httpsPort: args.getInt('httpsPort', 0),
            keystoreFile: keystoreFile,
            keystorePassword: keystorePassword,
            port: args.getInt('port', 8080),
            sessionTimeout: args.getInt('sessionTimeout', 30),
            tomcatDir: tomcatDir,
            trustStorePassword: args.get(
                'trustStorePassword', args['javax.net.ssl.trustStorePassword']
            ),
            trustStorePath: args.get(
                'trustStorePath', args['javax.net.ssl.trustStore']
            ),
            usingUserKeystore: !!keystorePath,
            useNio: !nio || nio.equalsIgnoreCase('true')
        )

        addShutdownHook()

        tomcat.start()
    }

    /**
     * Stops Tomcat.
     */
    void stop() {
        tomcat.stop()
        if (connector) {
            connector.stop()
            connector.destroy()
        }
    }


    //-- Non-public methods ---------------------

    /**
     * Adds the NIO connector to Tomcat.
     *
     * @param port  the port the connector should listen to
     */
    protected void addNioConnector(int port) {
        output.output 'message.nio.enable'
        connector = new Connector(Http11NioProtocol.class.name)
        connector.port = port
        tomcat.connector = connector
        tomcat.service.addConnector tomcat.connector
    }

    /**
     * Stops the embedded Tomcat server if the application goes down.
     */
    protected void addShutdownHook() {
        Runtime.runtime.addShutdownHook({
            try {
                if (tomcat) {
                    tomcat.server.stop()
                }
            } catch (LifecycleException e) {
                log.error 'Cannot stop Tomcat.', e
                output.output 'error.tomcat.cannotStop'
            }
        } as Thread)
    }

    /**
     * Configures Tomcat using the given settings.
     *
     * @param config    the given configuration settings
     */
    protected void configureTomcat(Map config) {
        tomcat.port = config.port

        tomcat.baseDir = config.tomcatDir.path
        context = tomcat.addWebapp(
            config.contextPath, config.destDir.absolutePath
        )
        context.addLifecycleListener(new TomcatLifecycleListener(
            tomcat: tomcat, output: output, controls: controls,
            autostart: autostart
        ))
        tomcat.enableNaming()

        if (config.useNio) {
            addNioConnector config.port
        }

        Connector connector = tomcat.connector
        if (config.enableCompression) {
            connector.setProperty 'compression', 'on'
            connector.setProperty 'compressableMimeType', config.compressableMimeTypes
        }

        /* only bind to host name if we aren't using the default */
        if (config.host != 'localhost') {
            connector.setAttribute 'address', config.host
        }

        connector.URIEncoding = 'UTF-8'
        context.sessionTimeout = config.sessionTimeout

        if (config.httpsPort > 0) {
            if (initSSL(config)) {
                createSSLConnector config
            }
        }
    }

    /**
     * Creates an SSL connector using the given configuration data.
     *
     * @param config    the given configuration data
     */
    protected void createSSLConnector(Map config) {
        Connector sslConnector
        try {
            sslConnector = new Connector()
        } catch (Exception e) {
            log.warn 'Couldn\'t create HTTPS connector. Use HTTP instead.', e
            return
        }

        sslConnector.scheme = 'https'
        sslConnector.secure = true
        sslConnector.port = config.httpsPort
        sslConnector.setProperty 'SSLEnabled', 'true'
        sslConnector.URIEncoding = 'UTF-8'

        sslConnector.setAttribute 'keystoreFile', config.keystoreFile.absolutePath
        sslConnector.setAttribute 'keystorePass', config.keystorePassword

        if (config.truststorePath) {
            sslConnector.setProperty 'sslProtocol', 'tls'
            sslConnector.setAttribute 'truststoreFile', new File(config.truststorePath).absolutePath
            sslConnector.setAttribute 'trustStorePassword', config.trustStorePassword
        }

        if (config.enableClientAuth) {
            sslConnector.setAttribute 'clientAuth', true
        }

        if (!config.host == 'localhost') {
            sslConnector.setAttribute 'address', config.host
        }

        tomcat.service.addConnector sslConnector
    }

    protected Class<?> getKeyToolClass() throws ClassNotFoundException {
        try {
            Class.forName 'sun.security.tools.KeyTool'
        } catch (ClassNotFoundException e) {
            Class.forName 'com.ibm.crypto.tools.KeyTool'
        }
    }

    /**
     * Initializes SSL for Tomcat using the given configuration data.
     *
     * @param config    the given configuration data
     * @return          {@code true} if the initialization was successful;
     *                  {@code false} otherwise
     */
    protected boolean initSSL(Map config) {
        if (config.keystoreFile.exists()) {
            return true
        }

        if (config.usingUserKeystore) {
            log.warn "Cannot start in HTTPS because used keystore does not exist (value: ${config.keystoreFile}). Use HTTP instead."
            return false
        }

        File keystoreDir = config.keystoreFile.parentFile
        if (!keystoreDir.exists() && !keystoreDir.mkdir()) {
            log.warn "Unable to create keystore folder: ${config.keystoreDir.canonicalPath}. Use HTTP instead."
            return false
        }

        try {
            output.output 'message.ssl.createCertificates'
            keyToolClass.main([
                '-genkey',
                '-alias', 'localhost',
                '-dname', 'CN=localhost,OU=Test,O=Test,C=US',
                '-keyalg', 'RSA',
                '-validity', '365',
                '-storepass', 'key',
                '-keystore', config.keystoreFile.absolutePath,
                '-storepass', config.keystorePassword,
                '-keypass', config.keystorePassword
            ] as String[])
            output.output 'message.ssl.createdCertificates'
            return true
        } catch (Exception e) {
            log.warn 'Unable to create an SSL certificate.', e
            output.output 'error.ssl.certificates'
            return false
        }
    }
}
