/*
 * GuiOutput.groovy
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

import javax.swing.JTextArea
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


/**
 * The class {@code GuiOutput} represents an uniform access to the output area
 * of the main window.
 *
 * @author  Daniel Ellermann
 * @version 1.0
 */
class GuiOutput {

    //-- Class variables ------------------------

    private static Logger log = LogManager.getLogger(this.class)


    //-- Instance variables ---------------------

    JTextArea outputArea
    ResourceBundle resourceBundle


    //-- Public methods -------------------------

    /**
     * Clears the output area.
     */
    void clear() {
        outputArea.text = ''
    }

    /**
     * Obtains a localized string for the given key and appends it to the
     * content of the output area.
     *
     * @param key   the given l10n key
     */
    void output(String key) {
        outputArea.append resourceBundle.getString(key) + '\n'
    }
}
