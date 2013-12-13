/*
 * Launcher.groovy
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

import static javax.swing.SwingConstants.*
import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL
import java.awt.Desktop
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JTextArea
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


/**
 * The class {@code Launcher} represents a GUI launcher of Tomcat and
 * SpringCRM.
 *
 * @author  Daniel Ellermann
 * @version 1.0
 */
class Launcher {

    //-- Class variables ------------------------

    private static Logger log = LogManager.getLogger(this.class)


    //-- Instance variables ---------------------

    Arguments args
    JButton btnLaunch
    JButton btnStart
    JButton btnStop
    Extractor extractor
    JMenuItem menuItemLaunch
    JMenuItem menuItemStart
    JMenuItem menuItemStop
    GuiOutput output
    JTextArea outputArea
    ResourceBundle rb
    TomcatLauncher tomcatLauncher
    boolean tomcatRunning
    JFrame window


    //-- Constructors ---------------------------

    Launcher(String [] args) {
        this.args = new Arguments(args)
        initResourceBundle Locale.getDefault()
        output = new GuiOutput(resourceBundle: rb)
        this.extractor = new Extractor(this.args)
        this.extractor.extract()
        this.tomcatLauncher = new TomcatLauncher(
            args: this.args, extractor: this.extractor, output: this.output
        )
    }


    //-- Public methods -------------------------

    def generateWindow = {
        window = frame(title: rb.getString('title'), show: true,
                       defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE) {
            menuBar {
                menu(text: rb.getString('menu.file.label'),
                     mnemonic: rb.getString('menu.file.mnemonic')) {
                    menuItemStart = menuItem(
                        text: rb.getString('menu.file.start.label'),
                        mnemonic: rb.getString('menu.file.start.mnemonic'),
                        icon: imageIcon(resource: '/image/menu-start.png'),
                        enabled: !tomcatRunning,
                        actionPerformed: { startTomcat() }
                    )
                    menuItemStop = menuItem(
                        text: rb.getString('menu.file.stop.label'),
                        mnemonic: rb.getString('menu.file.stop.mnemonic'),
                        icon: imageIcon(resource: '/image/menu-stop.png'),
                        enabled: tomcatRunning,
                        actionPerformed: { stopTomcat() }
                    )
                    separator()
                    menuItemLaunch = menuItem(
                        text: rb.getString('menu.file.launch.label'),
                        mnemonic: rb.getString('menu.file.launch.mnemonic'),
                        icon: imageIcon(resource: '/image/menu-springcrm.png'),
                        enabled: tomcatRunning,
                        actionPerformed: { launchSpringcrm() }
                    )
                    separator()
                    menuItem(
                        text: rb.getString('menu.file.quit.label'),
                        mnemonic: rb.getString('menu.file.quit.mnemonic'),
                        icon: imageIcon(resource: '/image/menu-quit.png'),
                        actionPerformed: { dispose() }
                    )
                }
                menu(text: rb.getString('menu.language.label'),
                     mnemonic: rb.getString('menu.language.mnemonic')) {
                    menuItem(
                        text: 'Deutsch', mnemonic: 'D',
                        actionPerformed: { changeLanguage Locale.GERMAN }
                    )
                    menuItem(
                        text: 'English', mnemonic: 'E',
                        actionPerformed: { changeLanguage Locale.ENGLISH }
                    )
                }
                menu(text: rb.getString('menu.info.label'),
                     mnemonic: rb.getString('menu.info.mnemonic')) {
                    menuItem(
                        text: rb.getString('menu.info.website.label'),
                        mnemonic: rb.getString('menu.info.website.mnemonic'),
                        icon: imageIcon(resource: '/image/menu-website.png'),
                        actionPerformed: { browseTo 'http://www.springcrm.de' }
                    )
                    separator()
                    menuItem(
                        text: rb.getString('menu.info.about.label'),
                        mnemonic: rb.getString('menu.info.about.mnemonic'),
                        icon: imageIcon(resource: '/image/menu-about.png'),
                        actionPerformed: { showAboutDlg() }
                    )
                }
            }
            borderLayout()
            output.outputArea = textArea columns: 60, rows: 10, constraints: BL.NORTH
            panel {
                flowLayout()
                btnStart = button(
                    text: rb.getString('button.start.label'),
                    mnemonic: rb.getString('button.start.mnemonic'),
                    icon: imageIcon(resource: '/image/start.png'),
                    horizontalTextPosition: CENTER,
                    verticalTextPosition: BOTTOM,
                    enabled: !tomcatRunning,
                    actionPerformed: { startTomcat() }
                )
                btnLaunch = button(
                    text: rb.getString('button.launch.label'),
                    mnemonic: rb.getString('button.launch.mnemonic'),
                    icon: imageIcon(resource: '/image/springcrm.png'),
                    horizontalTextPosition: CENTER,
                    verticalTextPosition: BOTTOM,
                    enabled: tomcatRunning,
                    actionPerformed: { launchSpringcrm() }
                )
                btnStop = button(
                    text: rb.getString('button.stop.label'),
                    mnemonic: rb.getString('button.stop.mnemonic'),
                    icon: imageIcon(resource: '/image/stop.png'),
                    horizontalTextPosition: CENTER,
                    verticalTextPosition: BOTTOM,
                    enabled: tomcatRunning,
                    actionPerformed: { stopTomcat() }
                )
            }
        }
        window.pack()
    }

    /**
     * Constructs the window and displays it.
     */
    void start() {
        def builder = new SwingBuilder()
        generateWindow.delegate = builder
        builder.edt generateWindow
    }

    static main(args) {
        new Launcher(args).start()
    }


    //-- Non-public methods ---------------------

    /**
     * Opens a browser and displays the given URL.
     *
     * @param url   the URL to display
     */
    protected void browseTo(String url) {
        def desktop = Desktop.desktop
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            output.output 'error.cannotLaunchBrowser'
            return
        }

        desktop.browse new URI(url)
    }

    /**
     * Called if the user has changed the language.  The method reloads the
     * window using the new locale.
     *
     * @param locale    the locale that should be used
     */
    protected void changeLanguage(Locale locale) {
        Locale.setDefault(locale)
        output.resourceBundle = initResourceBundle(locale)

        window.dispose()
        def builder = new SwingBuilder()
        generateWindow.delegate = builder
        builder.build generateWindow
    }

    /**
     * Enables or disables the controls in the window depending on the running
     * status of Tomcat.
     */
    protected void enableControls() {
        btnStart.enabled = !tomcatRunning
        btnLaunch.enabled = tomcatRunning
        btnStop.enabled = tomcatRunning
        menuItemStart.enabled = !tomcatRunning
        menuItemLaunch.enabled = tomcatRunning
        menuItemStop.enabled = tomcatRunning
    }

    /**
     * Initializes the resource bundle for the given locale.
     *
     * @param locale    the given locale
     * @return          the resource bundle
     */
    protected ResourceBundle initResourceBundle(Locale locale) {
        rb = ResourceBundle.getBundle('messages', locale)
        rb
    }

    /**
     * Starts a browser and displays the SpringCRM webpage.
     */
    protected void launchSpringcrm() {
        browseTo 'http://localhost:8080/'
    }

    /**
     * Displays the about dialog.
     */
    protected void showAboutDlg() {
        JOptionPane.showMessageDialog window, rb.getString('about.message')
    }

    /**
     * Starts Tomcat.
     */
    protected void startTomcat() {
        output.clear()
        output.output 'status.tomcatStarting'
        tomcatRunning = true
        enableControls()
        try {
            tomcatLauncher.start()
        } catch (Exception e) {
            log.error 'Error loading Tomcat.', e
            output.output 'error.tomcat.starting'
            tomcatRunning = false
            enableControls()
        }
    }

    /**
     * Stops Tomcat.
     */
    protected void stopTomcat() {
        output.output 'status.tomcatStopping'
        tomcatRunning = false
        enableControls()
        try {
            tomcatLauncher.stop()
        } catch (Exception e) {
            log.error 'Error stopping Tomcat.', e
            output.output 'error.tomcat.stopping'
            tomcatRunning = false
            enableControls()
        }
    }
}
