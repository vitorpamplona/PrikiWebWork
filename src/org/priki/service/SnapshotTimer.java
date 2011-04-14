/*
 * Priki - Prevalent Wiki
 * Copyright (c) 2005 Priki 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 *
 * @author Vitor Fernando Pamplona - vitor@babaxp.org
 *
 */
package org.priki.service;

import java.io.IOException;

import org.prevayler.Prevayler;

/**
 * Take a system snapshot each minute
 * 
 * @author <a href="mailto:vitor@babaxp.org">Vitor Fernando Pamplona</a>
 * 
 * @since 27/04/2004
 * @version $Id: SnapshotTimer.java,v 1.1 2006/06/11 15:26:31 vfpamp Exp $
 */
public class SnapshotTimer extends Thread {

    /**
     * Controls persistence.
     */
    Prevayler prevayler;

    /**
     * Creates a new class for Snapshot Timer control.
     * This class is a daemon
     * 
     * @param prevayler Controls Persistence
     */
    public SnapshotTimer(Prevayler prevayler) {
        this.prevayler = prevayler;
        this.setDaemon(true);
    }

    /**
     * Execute snapshots
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        super.run();

        while (true) {
            try {
                Thread.sleep(1000 * 60 * 60 * 6); //6 horas
                prevayler.takeSnapshot();
                System.out.println("Snapshot disparado as " + new java.util.Date() + "...");    
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}