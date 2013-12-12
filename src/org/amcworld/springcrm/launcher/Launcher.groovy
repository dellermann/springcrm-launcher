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
import javax.swing.JFrame
import javax.swing.JTextArea


/**
 * The class {@code Launcher} represents ...
 *
 * @author  Daniel Ellermann
 * @version 1.0
 */
class Launcher {

    //-- Instance variables ---------------------

    JTextArea outputArea
    ResourceBundle rb
    JFrame window


    //-- Constructors ---------------------------

    Launcher() {
        initResourceBundle Locale.getDefault()
    }


    //-- Public methods -------------------------

    def generateWindow = {
        window = frame(title: 'SpringCRM', show: true,
                       defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE) {
            menuBar {
                menu text: 'Datei', {
                    menuItem(
                        text: 'SpringCRM starten',
                        icon: imageIcon('res/image/menu-start.png')
                    )
                    menuItem(
                        text: 'SpringCRM beenden',
                        icon: imageIcon('res/image/menu-stop.png')
                    )
                    separator()
                    menuItem(
                        text: 'SpringCRM aufrufen',
                        icon: imageIcon('res/image/menu-springcrm.png')
                    )
                    separator()
                    menuItem(
                        text: 'Beenden',
                        icon: imageIcon('res/image/menu-quit.png'),
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
                menu text: '?', {
                    menuItem(text: 'Über SpringCRM')
                }
            }
            borderLayout()
            outputArea = textArea rows: 10, constraints: BL.NORTH
            panel {
                flowLayout()
                button(
                    text: rb.getString('button.start.label'),
                    mnemonic: rb.getString('button.start.mnemonic'),
                    icon: imageIcon('res/image/start.png'),
                    horizontalTextPosition: CENTER,
                    verticalTextPosition: BOTTOM
                )
                button(
                    text: 'Aufrufen',
                    icon: imageIcon('res/image/springcrm.png'),
                    horizontalTextPosition: CENTER,
                    verticalTextPosition: BOTTOM,
                    enabled: false
                )
                button(
                    text: rb.getString('button.stop.label'),
                    mnemonic: rb.getString('button.stop.mnemonic'),
                    icon: imageIcon('res/image/stop.png'),
                    horizontalTextPosition: CENTER,
                    verticalTextPosition: BOTTOM,
                    enabled: false
                )
            }
        }
        window.pack()
    }

    static main(args) {
        def launcher = new Launcher()
        launcher.run()
    }

    void run() {
        def builder = new SwingBuilder()
        generateWindow.delegate = builder
        builder.edt generateWindow
        outputArea.text = 'Hallo Welt!'
    }


    //-- Non-public methods ---------------------

    /**
     * Called if the user has changed the language.  The method reloads the
     * window using the new locale.
     *
     * @param locale    the locale that should be used
     */
    protected void changeLanguage(Locale locale) {
        Locale.setDefault(locale)
        initResourceBundle locale

        window.dispose()
        def builder = new SwingBuilder()
        generateWindow.delegate = builder
        builder.build generateWindow
    }

    /**
     * Initializes the resource bundle for the given locale.
     *
     * @param locale    the given locale
     * @return          the resource bundle
     */
    protected ResourceBundle initResourceBundle(Locale locale) {
        rb = ResourceBundle.getBundle(
            'org.amcworld.springcrm.launcher.messages', locale
        )
        rb
    }
}
