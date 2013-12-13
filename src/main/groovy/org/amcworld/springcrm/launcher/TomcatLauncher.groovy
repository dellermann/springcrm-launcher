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

import org.apache.catalina.Context
import org.apache.catalina.LifecycleEvent
import org.apache.catalina.LifecycleException
import org.apache.catalina.LifecycleListener
import org.apache.catalina.LifecycleState
import org.apache.catalina.Server
import org.apache.catalina.connector.Connector
import org.apache.catalina.core.StandardServer
import org.apache.catalina.startup.Tomcat
import org.apache.catalina.valves.CrawlerSessionManagerValve
import org.apache.coyote.http11.Http11NioProtocol
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


/**
 * The class {@code TomcatLauncher} represents ...
 *
 * @author  Daniel Ellermann
 * @version 1.0
 */
class TomcatLauncher {

    //-- Class variables ------------------------

    private static Logger log = LogManager.getLogger(this.class)


    //-- Instance variables ---------------------

    protected Arguments args
    protected Context context
    protected Extractor extractor
    protected GuiOutput output
    protected Tomcat tomcat = new Tomcat()


    //-- Public methods -------------------------

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
        addFailureLifecycleListener()

        tomcat.start()
        output.output 'message.tomcat.running'
    }

    void stop() {
        tomcat.stop()
    }


    //-- Non-public methods ---------------------

    protected void addFailureLifecycleListener() {
        context.addLifecycleListener({ LifecycleEvent event ->
            if (event.lifecycle.state == LifecycleState.FAILED) {
                Server server = tomcat.server
                if (server instanceof StandardServer) {
                    output.output 'error.tomcat.context'
                    server.stopAwait()
                }
            }
        } as LifecycleListener)
    }

    /**
     * Adds the NIO connector to Tomcat.
     *
     * @param port  the port the connector should listen to
     */
    protected void addNioConnector(int port) {
        output.output 'message.nio.enable'
        Connector connector = new Connector(Http11NioProtocol.class.name)
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
        tomcat.enableNaming()

        if (config.useNio) {
            addNioConnector config.port
        }

        tomcat.engine.pipeline.addValve new CrawlerSessionManagerValve()

        Connector connector = tomcat.connector
        if (config.enableCompression) {
            connector.setProperty 'compression', 'on'
            connector.setProperty 'compressableMimeType', config.compressableMimeTypes
        }

        // Only bind to host name if we aren't using the default
        if (config.host != 'localhost') {
            connector.setAttribute 'address', config.host
        }

        connector.URIEncoding = 'UTF-8'
        context.sessionTimeout = config.sessionTimeout

        if (config.httpsPort > 0) {
            initSSL config
            createSSLConnector config
        }
    }

    protected void createSSLConnector(Map config) {
        Connector sslConnector
        try {
            sslConnector = new Connector()
        } catch (Exception e) {
            throw new RuntimeException("Couldn't create HTTPS connector", e)
        }

        sslConnector.scheme = "https"
        sslConnector.secure = true
        sslConnector.port = config.httpsPort
        sslConnector.setProperty "SSLEnabled", "true"
        sslConnector.URIEncoding = "UTF-8"

        sslConnector.setAttribute "keystoreFile", config.keystoreFile.absolutePath
        sslConnector.setAttribute "keystorePass", config.keystorePassword

        if (config.truststorePath) {
            sslConnector.setProperty "sslProtocol", "tls"
            sslConnector.setAttribute "truststoreFile", new File(config.truststorePath).absolutePath
            sslConnector.setAttribute "trustStorePassword", config.trustStorePassword
        }

        if (config.enableClientAuth) {
            sslConnector.setAttribute "clientAuth", true
        }

        if (!config.host == "localhost") {
            sslConnector.setAttribute "address", config.host
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

    protected void initSSL(Map config) {
        if (config.keystoreFile.exists()) {
            return
        }

        if (config.usingUserKeystore) {
            throw new IllegalStateException(
                "Cannot start in HTTPS because used keystore does not exist (value: ${config.keystoreFile})"
            )
        }

        output.output 'message.ssl.createCertificates'

        File keystoreDir = config.keystoreFile.parentFile
        if (!keystoreDir.exists() && !keystoreDir.mkdir()) {
            throw new RuntimeException(
                "Unable to create keystore folder: ${config.keystoreDir.canonicalPath}"
            )
        }

        try {
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
        } catch (Exception e) {
            System.err.println("Unable to create an SSL certificate: " + e.getMessage());
        }
    }
}
