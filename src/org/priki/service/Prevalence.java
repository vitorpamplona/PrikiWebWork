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
import java.io.InputStream;
import java.util.Properties;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.priki.bo.Wiki;
import org.priki.prevalence.PrikiTransaction;

/** The prevalence layer */
public class Prevalence {

    protected static String DATABASE_PATH = "database.path";
    protected static String DATABASE_MIRRORING_TYPE = "database.mirroringType";
    protected static String DATABASE_MIRRORING_PORT = "database.mirroringPort";
    protected static String DATABASE_SERVER_IP = "database.serverIP";
    
    protected static final String REPLICATION_NONE = "none";
    protected static final String REPLICATION_SERVER = "server";
    protected static final String REPLICATION_CLIENT = "client";
    
    private static Prevalence singleton;
    
    private Properties config;
    private Prevayler layer;
    private Wiki wiki;
    
    public static synchronized Prevalence getInstance() {
        if (singleton == null) {
        	System.out.println("Criando Prevalence");
            singleton = new Prevalence();
        }
        return singleton;
    }
    
    public static Wiki wiki() {
        return getInstance().getWiki();
    }
    
    private Prevalence() {
        loadConfigurations();
        
        try {
        	PrevaylerFactory factory = new PrevaylerFactory();
        	factory.configurePrevalenceDirectory(getConfig(DATABASE_PATH));
        	factory.configurePrevalentSystem(new Wiki());
        	
        	if (getConfig(DATABASE_MIRRORING_TYPE).equalsIgnoreCase(REPLICATION_SERVER)) {
        		factory.configureReplicationServer(Integer.parseInt(getConfig(DATABASE_MIRRORING_PORT)));	
        	}
        	if (getConfig(DATABASE_MIRRORING_TYPE).equalsIgnoreCase(REPLICATION_CLIENT)) {
        		factory.configureReplicationClient(getConfig(DATABASE_SERVER_IP), Integer.parseInt(getConfig(DATABASE_MIRRORING_PORT)));	
        	}        	
        	
        	layer = factory.create();
            wiki = (Wiki)layer.prevalentSystem();
            
            new SnapshotTimer(layer).start();
        } catch (IOException e) {
        	e.printStackTrace();
            new RuntimeException("Prevayler load database IO error. ", e);
        } catch (ClassNotFoundException e) {
        	e.printStackTrace();
        	new RuntimeException("Prevayler load database error: ClassNotFoundException. ", e);
		} catch (StackOverflowError e) {
			e.printStackTrace();
			throw e;
		}
    }
    
    public Wiki getWiki() {
        return wiki;
    }

    public void execute(PrikiTransaction trans) {
        layer.execute(trans);
    }
    
    protected String getConfig(String key) {
        return config.getProperty(key);
    }
    
    private void loadConfigurations() {
        config = new Properties();
        InputStream in = getConfigurationFile();
        
        try {
            config.load(in);
        } catch (IOException e) {
            new RuntimeException("Failed to load configurations", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {}
        }
    }

    protected InputStream getConfigurationFile() {
        return Prevalence.class.getResourceAsStream("../../../prevayler.properties");
    }

	public void snapshot() throws IOException {
		layer.takeSnapshot();
	}

}
