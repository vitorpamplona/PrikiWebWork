package org.priki.service;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.priki.bo.MailConfiguration;
import org.priki.utils.StringUtils;

public class Mail {

	MailConfiguration config;
	

	public  Mail(MailConfiguration config) {
		this.config = config;
	}

	/**
	 * @param mailFrom - Mail from
	 * @param mailTo - Use comma to multiple receivers
	 * @param subject - Subject of mail
	 * @param bodyMessage - Mail message
	 * @param htmlMail - Use true to send an HTML mail
	 */
	public void sendMail(String mailTo, String subject,
			String bodyMessage) {
		
		if (StringUtils.isNullAndBlank(config.getFromMail())) {
			System.out.println("Fill E-mail Information on Admin Page");
			return;
		}
		
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		if (!StringUtils.isNullAndBlank(config.getSmtpPort())) {
			props.setProperty("mail.smtp.port", config.getSmtpPort());
		}
		props.setProperty("mail.smtp.host", config.getSmtpHost());
		
		Authenticator auth = null;
		
		if (!StringUtils.isNullAndBlank(config.getSmtpUser())) {
			props.setProperty("mail.smtp.auth", "true");
			props.setProperty("mail.smtp.starttls.enable","true");
			props.setProperty("mail.user", config.getSmtpUser());
        	props.setProperty("mail.password", config.getSmtpPassword());
        	
        	auth = new MyPasswordAuthenticator(config.getSmtpUser(), config.getSmtpPassword());
		}
		    
	    props.put("mail.from", config.getFromMail());
	    
	    Session session = Session.getInstance(props, auth);
	    //session.setDebug(true);

	    try {
	        // Create an "Alternative" Multipart message
	    	Multipart mp = new MimeMultipart("alternative");
	    	
	    	MimeBodyPart p1 = new MimeBodyPart();
	        p1.setText(bodyMessage);
	        
	    	MimeBodyPart p2 = new MimeBodyPart();
	        p2.setContent(bodyMessage, "text/html");
	        
	        mp.addBodyPart(p1);
	        mp.addBodyPart(p2);
	    	
	        MimeMessage msg = new MimeMessage(session);
	        msg.setFrom();
	        msg.setRecipients(Message.RecipientType.TO, mailTo);
	        msg.setSubject(subject);
	        msg.setSentDate(new Date());
	        msg.setContent(mp);
	        Transport.send(msg);
	    } catch (MessagingException mex) {
	        System.out.println("send failed, exception: " + mex);
	        mex.printStackTrace();
	    }
        
	}
	
    class MyPasswordAuthenticator extends Authenticator  {
       String user;
       String pw;

       public MyPasswordAuthenticator (String username, String password)
       {
          super();
          this.user = username;
          this.pw = password;
       }
       public PasswordAuthentication getPasswordAuthentication()
       {
          return new PasswordAuthentication(user, pw);
       }
    }

}
