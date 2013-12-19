/*
 * TomcatLifecycleListener.groovy
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

import static org.apache.catalina.Lifecycle.*
import groovy.util.logging.Log4j
import org.apache.catalina.Lifecycle
import org.apache.catalina.LifecycleEvent
import org.apache.catalina.LifecycleListener
import org.apache.catalina.LifecycleState
import org.apache.catalina.Server
import org.apache.catalina.core.StandardServer
import org.apache.catalina.startup.Tomcat


/**
 * The class {@code TomcatLifecycleListener} represents a handle for the
 * various Tomcat lifecycle states.  Mostly, it shows the state in the GUI by
 * enabling or disabling buttons and menu items, printing output messages etc.
 *
 * @author  Daniel Ellermann
 * @version 1.0
 */
@Log4j
class TomcatLifecycleListener implements LifecycleListener {

    //-- Instance variables ---------------------

    boolean autostart
    GuiControls controls
    GuiOutput output
    Tomcat tomcat

    protected long time


    //-- Public methods -------------------------

    @Override
    void lifecycleEvent(LifecycleEvent event) {
        Lifecycle l = event.lifecycle
        if (l.state == LifecycleState.FAILED) {
            Server server = tomcat.server
            if (server instanceof StandardServer) {
                log.error "Context failed in ${l.class.name} lifecycle. Allowing Tomcat to shutdown."
                output.output 'error.tomcat.context'
                server.stopAwait()
            }
            return
        }

        switch (event.type) {
        case BEFORE_START_EVENT:
            log.debug 'Starting Tomcat...'
            time = System.currentTimeMillis()
            output.clear()
            output.output autostart ? 'status.tomcatAutoStarting' : 'status.tomcatStarting'
            output.startIndeterminateProgress()
            controls.enableControls TomcatStatus.starting
            break
        case AFTER_START_EVENT:
            log.debug "Tomcat started successfully (took ${(System.currentTimeMillis() - time) / 1000} s)."
            println "Tomcat started successfully (took ${(System.currentTimeMillis() - time) / 1000} s)."
            output.output 'message.tomcat.running'
            controls.enableControls TomcatStatus.started
            break
        case BEFORE_STOP_EVENT:
            log.debug 'Stopping Tomcat...'
            time = System.currentTimeMillis()
            output.output 'status.tomcatStopping'
            output.startIndeterminateProgress()
            controls.enableControls TomcatStatus.stopping
            break
        case AFTER_STOP_EVENT:
            log.debug 'Tomcat stopped successfully (took ${(System.currentTimeMillis() - time) / 1000} s).'
            println "Tomcat stopped successfully (took ${(System.currentTimeMillis() - time) / 1000} s)."
            output.output 'message.tomcat.stopped'
            controls.enableControls TomcatStatus.initialized
            break
        }
    }
}
