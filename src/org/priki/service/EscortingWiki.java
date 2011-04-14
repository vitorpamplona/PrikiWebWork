package org.priki.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.priki.bo.User;
import org.priki.bo.Wikiword;

public class EscortingWiki extends Thread {
	
	public static String USER_PATTERN = "___USERNAME___";
	private static EscortingWiki instance;
		
	private Mail mailSender;
	private List<Message> toSend;
	
	private EscortingWiki() {
		mailSender = new Mail(Prevalence.wiki().getAdmin().getMailConfiguration());
		toSend = Collections.synchronizedList(new ArrayList<Message>());
	}
	
	public static EscortingWiki getInstance() {
		if (instance == null) {
			instance = new EscortingWiki();
			instance.start();
		}
		return instance;
	}
	
	public Set<User> getEscortingUsers(User user, Wikiword page) {
		return Prevalence.wiki().getAdmin().getAccessManager().getEscortingUsers(user, page);
	}
	
	public void notifyEscorting(String subject, String bodyMessage, Wikiword page, User actionUser) {
		Message m = new Message();
		m.subject = subject;
		m.body = bodyMessage;
		m.page = page;
		m.actionUser = actionUser;
		toSend.add(m);
	}
	
    public void run() {
        super.run();

        while (true) {
            try {
                Thread.sleep(5000); //5 minutos
                
                while (toSend.size() > 0) { 
                	Message m = toSend.remove(0);

                	Set<User> usersToSend = getEscortingUsers(m.actionUser, m.page);
            		// If is the same user of the action, abort
                	usersToSend.remove(m.actionUser);
                	
                	for (User u : usersToSend) {
               			mailSender.sendMail(u.getEmail(), m.subject, m.body.replaceAll(USER_PATTERN, u.getName()));
                	}
                }
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            } 
        }
    }	
    
    class Message {
    	public String subject;
    	public String body;
    	public Wikiword page;
    	public User actionUser;
    }
}
