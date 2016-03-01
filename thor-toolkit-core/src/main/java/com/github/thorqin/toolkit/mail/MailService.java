/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.mail;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.service.IService;
import com.github.thorqin.toolkit.service.TaskService;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.utility.StringUtils;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.validation.Validator;
import com.github.thorqin.toolkit.validation.annotation.ValidateString;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author nuo.qin
 */
public class MailService implements IService {

    public final static String SECURE_STARTTLS = "starttls";
    public final static String SECURE_SSL = "ssl";
    public final static String SECURE_NO = "no";

    public static class MailSetting {
        public boolean trace = false;
        public boolean debug = false;
        public boolean auth = true;
        @ValidateString
        public String host;
        public int port = 25;
        public String user;
        public String password;
        @ValidateString("^(starttls|ssl|no)$")
        public String secure = "no";
        @ValidateString(ValidateString.MAIL_BOX)
        public String from;
    }

	public static class Mail {
		public String from = null;
		public String[] to = null;
		public String subject = null;
		public String htmlBody;
		public String textBody;
        /**
         * Must be serializable.
         */
        public Object extraParam = null;
		private final List<String> attachments = new LinkedList<>();
		public void addAttachment(String filePath) {
			attachments.add(filePath);
		}
		public void clearAttachment() {
			attachments.clear();
		}
	}

    @Service("tracer")
    private Tracer tracer = null;
    @Service("logger")
	private Logger logger = Logger.getLogger(MailService.class.getName());
    private TaskService<Mail> taskService = null;
	private MailSetting setting;
    private String serviceName = null;


    public MailService() {
        setting = null;
        tracer = null;
    }
	
	public MailService(MailSetting mailSetting) throws ValidateException {
        validateSetting(mailSetting);
        setting = mailSetting;
        this.tracer = null;
	}

    public MailService(MailSetting mailSetting, Tracer tracer) throws ValidateException {
        validateSetting(mailSetting);
        setting = mailSetting;
        this.tracer = tracer;
    }

    private void validateSetting(MailSetting mailSetting) throws ValidateException {
        Validator validator = new Validator(Localization.getInstance());
        validator.validateObject(mailSetting, MailService.MailSetting.class, false);
    }

    public synchronized void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }
	
	private void sendMail(Mail mail) {
		long beginTime = System.currentTimeMillis();
        String mailFrom = "";
        boolean success = false;
        try {
            Properties props = new Properties();
            final Session session;
            props.put("mail.smtp.auth", String.valueOf(setting.auth));
            // If want to display SMTP protocol detail then uncomment following statement
            // props.put("mail.debug", "true");
            props.put("mail.smtp.host", setting.host);
            props.put("mail.smtp.port", setting.port);
            if (setting.secure.equals(SECURE_STARTTLS)) {
                props.put("mail.smtp.starttls.enable", "true");

            } else if (setting.secure.equals(SECURE_SSL)) {
                props.put("mail.smtp.socketFactory.port", setting.port);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
            }
            if (!setting.auth)
                session = Session.getInstance(props);
            else
                session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(setting.user, setting.password);
                            }
                        });

            if (setting.debug)
                session.setDebug(true);

            MimeMessage message = new MimeMessage(session);
            StringBuilder mailTo = new StringBuilder();
			if (mail.from != null) {
                message.setFrom(new InternetAddress(mail.from));
            }
			else if (setting.from != null)
				message.setFrom(new InternetAddress(setting.from));
            mailFrom = StringUtils.join(message.getFrom());
            if (mail.to != null) {
				for (String to : mail.to) {
					if (mailTo.length() > 0)
						mailTo.append(",");
					mailTo.append(to);
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
				}
			}
			if (mail.subject != null)
				message.setSubject("=?UTF-8?B?" +
							Base64.encodeBase64String(mail.subject.getBytes("utf-8"))
							+ "?=");
			message.setSentDate(new Date());
			
			BodyPart bodyPart = new MimeBodyPart();
			if (mail.htmlBody != null)
				bodyPart.setContent(mail.htmlBody, "text/html;charset=utf-8");
			else if (mail.textBody != null)
				bodyPart.setText(mail.textBody);
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(bodyPart);
			
			if (mail.attachments != null) {
				for (String attachment : mail.attachments) {
					BodyPart attachedBody = new MimeBodyPart();
					File attachedFile = new File(attachment);
					DataSource source = new FileDataSource(attachedFile);
					attachedBody.setDataHandler(new DataHandler(source));
					attachedBody.setDisposition(MimeBodyPart.ATTACHMENT);
					String filename = attachedFile.getName();
					attachedBody.setFileName("=?UTF-8?B?" +
							Base64.encodeBase64String(filename.getBytes("utf-8"))
							+ "?=");
					multipart.addBodyPart(attachedBody);
				}
			}
			
			message.setContent(multipart);
			message.saveChanges();
			Transport transport = session.getTransport("smtp");
			transport.connect();
				
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
            success = true;
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Send mail failed!", ex);
		} finally {
            if (setting.trace && tracer != null) {
                Tracer.Info info = new Tracer.Info();
                info.catalog = "mail";
                info.name = "send";
                info.put("serviceName", serviceName);
                info.put("success", success);
                info.put("sender", mailFrom);
                info.put("recipients", mail.to);
                info.put("SMTPServer", setting.host);
                info.put("SMTPAccount", setting.user);
                info.put("subject", mail.subject);
                info.put("startTime", beginTime);
                info.put("runningTime", System.currentTimeMillis() - beginTime);
                info.put("extraParam", mail.extraParam);
                tracer.trace(info);
            }
        }
    }

    @Override
    public boolean config(ConfigManager configManager, String serviceName, boolean isReConfig) {
        this.serviceName = serviceName;
        MailService.MailSetting newSetting = configManager.get(serviceName, MailService.MailSetting.class);
        try {
            validateSetting(newSetting);
            setting = newSetting;
        } catch (ValidateException ex) {
            logger.log(Level.SEVERE, "Invalid mail configuration settings: {0}", ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean isStarted() {
        return taskService != null;
    }

    @Override
	public synchronized void start() {
        if (taskService != null)
            return;
        if (setting == null) {
            throw new RuntimeException("Invalid mail service setting.");
        }
        taskService = new TaskService<>(new TaskService.TaskHandler<Mail>() {
            @Override
            public void process(Mail mail) {
                sendMail(mail);
            }
        });
        if (setting.trace && tracer != null) {
            Tracer.Info info = new Tracer.Info();
            info.catalog = "mail";
            info.name = "started";
            tracer.trace(info);
        }
	}

    @Override
	public synchronized void stop() {
        if (taskService == null)
            return;
        try {
            taskService.shutdown();
        } catch (InterruptedException e) {
        }
        taskService = null;
        if (setting.trace && tracer != null) {
            Tracer.Info info = new Tracer.Info();
            info.catalog = "mail";
            info.name = "stopped";
            tracer.trace(info);
        }
	}

    /**
     * Post mail item into send queue
     * @param mail
     */
	public synchronized void post(Mail mail) {
        if (taskService != null)
            taskService.offer(mail);
	}

    public static Mail createMail(InputStream templateStream, Map<String,String> replaced) throws IOException {
        Mail mail = new Mail();
        InputStreamReader reader = new InputStreamReader(templateStream, "utf-8");
        char[] buffer = new char[1024];
        StringBuilder builder = new StringBuilder();
        while (reader.read(buffer) != -1)
            builder.append(buffer);
        String mailBody = builder.toString();
        builder.setLength(0);
        Pattern pattern = Pattern.compile(
                "\\$\\{\\s*(.+?)\\s*\\}",
                Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(mailBody);
        int scanPos = 0;
        while (matcher.find()) {
            builder.append(mailBody.substring(scanPos, matcher.start()));
            scanPos = matcher.end();
            String key = matcher.group(1);
            if (replaced != null) {
                String value = replaced.get(key);
                if (value != null) {
                    builder.append(value);
                }
            }
        }
        builder.append(mailBody.substring(scanPos, mailBody.length()));
        mail.htmlBody = builder.toString();
        pattern = Pattern.compile("<title>(.*)</title>",
                Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(mail.htmlBody);
        if (matcher.find()) {
            mail.subject = matcher.group(1);
        }
        return mail;
    }

    /**
     * Create file from template content
     * @param template Mail template content, for convenient you can use Application.readScript() method
     *                 to easily load content from file or resource.
     * @param replaced Placeholder and value map.
     * @return Newly created mail item instance.
     * @throws IOException Throw on failed.
     */
    public static Mail createMail(String template, Map<String,String> replaced) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(template.getBytes("utf-8"))) {
            return createMail(inputStream, replaced);
        }
    }
}
