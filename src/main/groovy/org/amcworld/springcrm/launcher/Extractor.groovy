/*
 * Extractor.groovy
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

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


/**
 * The class {@code Extractor} extracts an embedded or specified WAR file to
 * a temporary folder.
 *
 * @author  Daniel Ellermann
 * @version 1.0
 */
class Extractor {

    //-- Class variables ------------------------

    private static Logger log = LogManager.getLogger(this.class)


    //-- Instance variables ---------------------

    File destDir
    protected Arguments args


    //-- Constructor ----------------------------

    Extractor(Arguments args) {
        this.args = args
    }


    //-- Public methods -------------------------

    /**
     * Extracts either an embedded WAR file or one specified on the command
     * line.  After that, property {@code destDir} is populated with the path
     * to the directory containing the unpacked WAR file.
     */
    void extract() {
        destDir = extractWar()
        if (destDir) {
            log.debug "Extracted WAR file to ${destDir}"
            Runtime.runtime.addShutdownHook { destDir.deleteDir() } as Thread
        }
    }


    //-- Non-public methods ---------------------

    /**
     * Unpacks the given WAR file to a directory.
     *
     * @param war   the given WAR file that should be unpacked
     * @return      the directory containing the unpacked WAR file
     */
    protected File explode(File war) {
        String basename = war.name
        int index = basename.lastIndexOf('.')
        if (index > -1) {
            basename = basename.substring(0, index)
        }
        File destDir = new File(
            war.parentFile,
            "${basename}-exploded-${System.currentTimeMillis()}"
        )

        ZipFile zipfile = new ZipFile(war)
        Enumeration<? extends ZipEntry> e = zipfile.entries()
        while (e) unzip e.nextElement(), zipfile, destDir
        zipfile.close()

        destDir
    }

    /**
     * Extracts the WAR file to the working directory.
     *
     * @return  the directory containing the unpacked WAR file
     */
    protected File extractWar() {
        File dir = new File(args.workDir, 'standalone-war')
        dir.deleteDir()
        dir.mkdirs()

        InputStream is
        String war = args['war']
        if (war) {
            def file = new File(war)
            if (!file.exists()) {
                throw new ArgumentException("WAR file '${war}' doesn't exist.")
            }

            is = file.newInputStream()
        } else {
            is = getClass().classLoader.getResourceAsStream('embedded.war')
        }

        File destFile =
            File.createTempFile('embedded-', '.war', dir).absoluteFile
        destFile.parentFile.mkdirs()
        destFile.deleteOnExit()
        destFile << is
        explode destFile
    }

    /**
     * Unpacks the given archive entry to the destination directory.
     *
     * @param entry         the archive entry that should be unpacked
     * @param zipFile       the whole archive
     * @param explodedDir   the directory where the entry should be unpacked
     */
    protected void unzip(ZipEntry entry, ZipFile zipFile, File explodedDir) {
        File outputFile = new File(explodedDir, entry.name)
        if (entry.directory) {
            outputFile.mkdirs()
            return
        }

        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }

        BufferedInputStream inputStream =
            new BufferedInputStream(zipFile.getInputStream(entry))
        BufferedOutputStream outputStream =
            new BufferedOutputStream(new FileOutputStream(outputFile))

        try {
            outputStream << inputStream
        } finally {
            outputStream.close()
            inputStream.close()
        }
    }
}
