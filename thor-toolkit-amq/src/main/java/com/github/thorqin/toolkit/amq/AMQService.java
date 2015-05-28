package com.github.thorqin.toolkit.amq;

import com.github.thorqin.toolkit.db.DBProxy;
import com.github.thorqin.toolkit.service.ISettingComparable;
import com.github.thorqin.toolkit.service.IStoppable;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Serializer;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import com.google.common.base.Strings;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ConfigurationException;

public class AMQService implements IStoppable, ISettingComparable {
    public static class AMQSetting {
        public String uri;
        public String user;
        public String password;
        public String address = "default";
        public boolean broadcast = false;
        public boolean trace = false;
    }

	private AMQSetting setting;
	private Connection connection = null;
    private Tracer tracer = null;
	private final ThreadLocal<ProducerHolder> localProducer = new ThreadLocal<>();
	
	private class ProducerHolder implements AutoCloseable {
		public Session session = null;
		public MessageProducer producer = null;
		public Destination replyQueue = null;
		public MessageConsumer replyConsumer = null;
		public ProducerHolder() throws JMSException {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(null);
			replyQueue = session.createTemporaryQueue();
			replyConsumer = session.createConsumer(replyQueue);
		}

        @Override
		public void close() {
			closeResource(producer);
			closeResource(replyConsumer);
			closeResource(session);
		}

		@Override
		protected void finalize() throws Throwable {
			close();
			super.finalize();
		}
	}
	
	private ProducerHolder createHolder() throws JMSException {
		ProducerHolder obj = localProducer.get();
		if (obj == null) {
			obj = new ProducerHolder();
			localProducer.set(obj);
		}
		return obj;
	}
	
	public class IncomingMessage {
		private final String address;
		private final String subject;
		private final String contentType;
		private final int replyCode;
		private final boolean isJsonEncoding;
		private final byte[] body;
		private final Destination replyDestination;
		private final String correlationID;

		public IncomingMessage(
				String address, 
				String subject,
				String contentType,
				boolean isJsonEncoding,
				int replyCode,
				byte[] body,
				Destination replyDestination,
				String correlationID) {
			this.address = address;
			this.subject = subject;
			this.contentType = contentType;
			this.isJsonEncoding = isJsonEncoding;
			this.body = body;
			this.replyDestination = replyDestination;
			this.correlationID = correlationID;
			this.replyCode = replyCode;
		}
		public String getAddress() {
			return address;
		}
		public String getSubject() {
			return subject;
		}
		public String getContentType() {
			return contentType;
		}
		public boolean isJsonEncoding() {
			return isJsonEncoding;
		}
		public int getReplyCode() {
			return replyCode;
		}
		public byte[] getBodyBytes() {
			return body;
		}
		public <T> T getBody() throws IOException {
			if (isJsonEncoding) {
				return Serializer.fromJson(body);
			} else {
				return Serializer.fromKryo(body);
			}
		}
		public <T> T getBody(Type type) throws IOException {
			if (isJsonEncoding) {
				return Serializer.fromJson(body, type);
			} else {
				return Serializer.fromKryo(body);
			}
		}
		public <T> T getBody(Class<T> type) throws IOException {
			if (isJsonEncoding) {
				return Serializer.fromJson(body, type);
			} else {
				return Serializer.fromKryo(body);
			}
		}
		public String getCorrelationID() {
			return correlationID;
		}
		public boolean needReply() {
			return replyDestination != null;
		}
		public String getReplyAddress() throws JMSException {
			if (replyDestination == null)
				return null;
			else {
				if (replyDestination instanceof javax.jms.Queue) {
					javax.jms.Queue queue = (javax.jms.Queue)replyDestination;
					return queue.getQueueName();
				} else
					return null;
			}
		}
		public void reply(byte[] replyMessage, String contentType, boolean useJsonEncoding)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyMessage, contentType, useJsonEncoding, replyDestination, correlationID);
		}
		public void reply(int replyCode, byte[] replyMessage, String contentType, boolean useJsonEncoding)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyCode, replyMessage, contentType, useJsonEncoding, replyDestination, correlationID);
		}
		public void reply(byte[] replyMessage, String contentType, boolean useJsonEncoding, long timeToLive)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, 0, replyMessage, contentType, useJsonEncoding, replyDestination, correlationID, timeToLive);
		}
		public void reply(int replyCode, byte[] replyMessage, String contentType, boolean useJsonEncoding, long timeToLive)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyCode, replyMessage, contentType, useJsonEncoding, replyDestination, correlationID, timeToLive);
		}
		public <T> void reply(T replyMessage) 
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyMessage, replyDestination, correlationID);
		}
		public <T> void reply(int replyCode, T replyMessage) 
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyCode, replyMessage, replyDestination, correlationID);
		}
		public <T> void reply(T replyMessage, long timeToLive) 
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyMessage, replyDestination, correlationID, timeToLive);
		}
		public <T> void reply(int replyCode, T replyMessage, long timeToLive) 
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyCode, replyMessage, replyDestination, correlationID, timeToLive);
		}
		public <T> void reply(T replyMessage, boolean useJsonEncoding) 
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyMessage, useJsonEncoding, replyDestination, correlationID);
		}
		public <T> void reply(int replyCode, T replyMessage, boolean useJsonEncoding) 
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyCode, replyMessage, useJsonEncoding, replyDestination, correlationID);
		}
		public <T> void reply(T replyMessage, boolean useJsonEncoding, long timeToLive) 
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyMessage, useJsonEncoding, replyDestination, correlationID, timeToLive);
		}
		public <T> void reply(int replyCode, T replyMessage, boolean useJsonEncoding, long timeToLive) 
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyCode, replyMessage, useJsonEncoding, replyDestination, correlationID, timeToLive);
		}
	}

	public interface MessageHandler {
		void onMessage(IncomingMessage message);
	}
	
	public class Replier {
		private ProducerHolder holder = null;
		private final int deliveryMode = DeliveryMode.NON_PERSISTENT;
		private final long defaultTimeToLive = 30000l;
		protected Replier() throws JMSException {
			if (connection == null)
				throw new JMSException("Connection not allocate.");
			holder = createHolder();
			holder.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		}
		public <T> void reply(String subject, T replyMessage, Destination destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, 0, replyMessage, destination, correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, T replyMessage, String destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, 0, replyMessage, holder.session.createQueue(destination), correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, int replyCode, T replyMessage, Destination destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, destination, correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, int replyCode, T replyMessage, String destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, holder.session.createQueue(destination), correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, T replyMessage, Destination destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, 0, replyMessage, destination, correlationID, timeToLive);
		}
		public <T> void reply(String subject, T replyMessage, String destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, 0, replyMessage, holder.session.createQueue(destination), correlationID, timeToLive);
		}
		public <T> void reply(String subject, int replyCode, T replyMessage, String destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, holder.session.createQueue(destination), correlationID, timeToLive);
		}
		public <T> void reply(String subject, int replyCode, T replyMessage, Destination destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			byte[] bytes = Serializer.toKryo(replyMessage);
			reply(subject, replyCode, bytes, Serializer.getTypeName(replyMessage), false, destination, correlationID, timeToLive);
		}
		public <T> void reply(String subject, T replyMessage, boolean useJsonEncoding, Destination destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, 0, replyMessage, useJsonEncoding, destination, correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, T replyMessage, boolean useJsonEncoding, String destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, 0, replyMessage, useJsonEncoding, holder.session.createQueue(destination), correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, int replyCode, T replyMessage, boolean useJsonEncoding, Destination destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, useJsonEncoding, destination, correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, int replyCode, T replyMessage, boolean useJsonEncoding, String destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, useJsonEncoding, holder.session.createQueue(destination), correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, T replyMessage, boolean useJsonEncoding, Destination destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, 0, replyMessage, useJsonEncoding, destination, correlationID, timeToLive);
		}
		public <T> void reply(String subject, T replyMessage, boolean useJsonEncoding, String destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, 0, replyMessage, useJsonEncoding, holder.session.createQueue(destination), correlationID, timeToLive);
		}
		public <T> void reply(String subject, int replyCode, T replyMessage, boolean useJsonEncoding, String destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, useJsonEncoding, 
					holder.session.createQueue(destination), correlationID, timeToLive);
		}
		
		public <T> void reply(String subject, int replyCode, T replyMessage, boolean useJsonEncoding, Destination destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			byte[] bytes;
			if (useJsonEncoding)
				bytes = Serializer.toJsonBytes(replyMessage);
			else
				bytes = Serializer.toKryo(replyMessage);
			reply(subject, replyCode, bytes, Serializer.getTypeName(replyMessage), useJsonEncoding, destination, correlationID, timeToLive);
		}
		public void reply(String subject, byte[] replyMessage, String contentType, boolean useJsonEncoding, Destination destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, 0, replyMessage, contentType, useJsonEncoding, destination, correlationID, defaultTimeToLive);
		}
		public void reply(String subject, byte[] replyMessage, String contentType, boolean useJsonEncoding, String destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, 0, replyMessage, contentType, useJsonEncoding, holder.session.createQueue(destination), correlationID, defaultTimeToLive);
		}
		public void reply(String subject, int replyCode, byte[] replyMessage, String contentType, boolean useJsonEncoding, Destination destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, contentType, useJsonEncoding, destination, correlationID, defaultTimeToLive);
		}
		public void reply(String subject, int replyCode, byte[] replyMessage, String contentType, boolean useJsonEncoding, String destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, contentType, useJsonEncoding, holder.session.createQueue(destination), correlationID, defaultTimeToLive);
		}
		public void reply(String subject, int replyCode, byte[] replyMessage, String contentType, boolean useJsonEncoding, Destination destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
            long beginTime = System.currentTimeMillis();
            boolean success = true;
            try {
                BytesMessage bytesMessage = holder.session.createBytesMessage();
                bytesMessage.writeBytes(replyMessage);
                bytesMessage.setJMSCorrelationID(correlationID);
                bytesMessage.setStringProperty("subject", subject);
                bytesMessage.setStringProperty("contentType", contentType);
                bytesMessage.setBooleanProperty("jsonEncoding", useJsonEncoding);
                bytesMessage.setIntProperty("replyCode", replyCode);
                holder.producer.send(destination, bytesMessage, deliveryMode, 5, timeToLive);
            } catch (Exception ex) {
                success = false;
                throw ex;
            } finally {
                if (setting.trace && tracer != null) {
                    Tracer.Info info = new Tracer.Info();
                    info.catalog = "amq";
                    info.name = "reply";
                    info.put("success", success);
                    info.put("subject", subject);
                    info.put("contentType", contentType);
                    info.put("startTime", beginTime);
                    info.put("runningTime", System.currentTimeMillis() - beginTime);
                    tracer.trace(info);
                }
            }
        }
		public void reply(String subject, int replyCode, byte[] replyMessage, String contentType, boolean useJsonEncoding, String destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, contentType, useJsonEncoding, 
					holder.session.createQueue(destination),correlationID, timeToLive);
		}
	}
	
	public class Sender {
		private ProducerHolder holder = null;
		private String address = null;
		private Destination dest = null;
		private int deliveryMode = DeliveryMode.NON_PERSISTENT;
		private final long defaultTimeout = 30000l;
		protected Sender(String address, boolean broadcast, boolean persistent) throws JMSException {
			if (connection == null)
				throw new JMSException("Connection not allocate.");
			this.address = address;
			holder = createHolder();
			if (broadcast)
				dest = holder.session.createTopic(address);
			else
				dest = holder.session.createQueue(address);
			deliveryMode = persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
		}
		public String getAddress() {
			return address;
		}
		public <T> void send(String subject, T message) throws IOException, JMSException {
			byte[] bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), false, 0);
		}
		public <T> void send(String subject, T message, String replyAddress, String correlationID) throws IOException, JMSException {
			byte[] bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), replyAddress, correlationID, false, 0);
		}
		public <T> void send(String subject, T message, boolean useJsonEncoding) throws IOException, JMSException {
			byte[] bytes;
			if (useJsonEncoding)
				bytes = Serializer.toJsonBytes(message);
			else
				bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), useJsonEncoding, 0);
		}
		public <T> void send(String subject, T message, String replyAddress, String correlationID, boolean useJsonEncoding) throws IOException, JMSException {
			byte[] bytes;
			if (useJsonEncoding)
				bytes = Serializer.toJsonBytes(message);
			else
				bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), replyAddress, correlationID, useJsonEncoding, 0);
		}
		public <T> void send(String subject, T message, long timeToLive) throws IOException, JMSException {
			byte[] bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), false, timeToLive);
		}
		public <T> void send(String subject, T message, String replyAddress, String correlationID, long timeToLive) throws IOException, JMSException {
			byte[] bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), replyAddress, correlationID, false, timeToLive);
		}
		public <T> void send(String subject, T message, boolean useJsonEncoding, long timeToLive) throws IOException, JMSException {
			byte[] bytes;
			if (useJsonEncoding)
				bytes = Serializer.toJsonBytes(message);
			else
				bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), useJsonEncoding, timeToLive);
		}
		public <T> void send(String subject, T message, String replyAddress, String correlationID, boolean useJsonEncoding, long timeToLive) throws IOException, JMSException {
			byte[] bytes;
			if (useJsonEncoding)
				bytes = Serializer.toJsonBytes(message);
			else
				bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), replyAddress, correlationID, useJsonEncoding, timeToLive);
		}
		public void send(String subject, byte[] message, String contentType, boolean useJsonEncoding, long timeToLive) 
				throws IOException, JMSException {
            long beginTime = System.currentTimeMillis();
            boolean success = true;
            try {
                BytesMessage bytesMessage = holder.session.createBytesMessage();
                bytesMessage.writeBytes(message);
                bytesMessage.setStringProperty("subject", subject);
                bytesMessage.setStringProperty("contentType", contentType);
                bytesMessage.setBooleanProperty("jsonEncoding", useJsonEncoding);
                holder.producer.send(dest, bytesMessage, deliveryMode, 4, timeToLive);
            } catch (Exception ex) {
                success = false;
                throw ex;
            } finally {
                if (setting.trace && tracer != null) {
                    Tracer.Info info = new Tracer.Info();
                    info.catalog = "amq";
                    info.name = "send";
                    info.put("success", success);
                    info.put("subject", subject);
                    info.put("contentType", contentType);
                    info.put("startTime", beginTime);
                    info.put("runningTime", System.currentTimeMillis() - beginTime);
                    tracer.trace(info);
                }
            }
		}
		public void send(String subject, byte[] message, String contentType, String replyAddress, String correlationID, boolean useJsonEncoding, long timeToLive) 
				throws IOException, JMSException {
            long beginTime = System.currentTimeMillis();
            boolean success = true;
            try {
                BytesMessage bytesMessage = holder.session.createBytesMessage();
                bytesMessage.writeBytes(message);
                bytesMessage.setStringProperty("subject", subject);
                bytesMessage.setStringProperty("contentType", contentType);
                bytesMessage.setBooleanProperty("jsonEncoding", useJsonEncoding);
                if (replyAddress != null)
                    bytesMessage.setJMSReplyTo(holder.session.createQueue(replyAddress));
                if (correlationID != null)
                    bytesMessage.setJMSCorrelationID(correlationID);
                holder.producer.send(dest, bytesMessage, deliveryMode, 4, timeToLive);
            } catch (Exception ex) {
                success = false;
                throw ex;
            } finally {
                if (setting.trace && tracer != null) {
                    Tracer.Info info = new Tracer.Info();
                    info.catalog = "amq";
                    info.name = "send";
                    info.put("success", success);
                    info.put("subject", subject);
                    info.put("contentType", contentType);
                    info.put("startTime", beginTime);
                    info.put("runningTime", System.currentTimeMillis() - beginTime);
                    tracer.trace(info);
                }
            }
		}
		
		public <T> IncomingMessage sendAndWaitForReply(String subject, T message) 
				throws IOException, JMSException, TimeoutException {
			return sendAndWaitForReply(subject, message, defaultTimeout);
		}
		
		public <T> IncomingMessage sendAndWaitForReply(String subject, T message, long timeout)  
				throws IOException, JMSException, TimeoutException {
			byte[] bytes = Serializer.toKryo(message);
			String contentType = Serializer.getTypeName(message);
			return sendAndWaitForReply(subject, bytes, contentType, false, timeout);
		}
		public <T> IncomingMessage sendAndWaitForReply(String subject, T message, boolean useJsonEncoding, long timeout)  
				throws IOException, JMSException, TimeoutException {
			byte[] bytes;
			if (useJsonEncoding)
				bytes = Serializer.toJsonBytes(message);
			else
				bytes = Serializer.toKryo(message);
			String contentType = Serializer.getTypeName(message);
			return sendAndWaitForReply(subject, bytes, contentType, useJsonEncoding, timeout);
		}

		public IncomingMessage sendAndWaitForReply(String subject, byte[] message, String contentType, 
				boolean useJsonEncoding, long timeout) throws IOException, 
				JMSException, TimeoutException {
            long beginTime = System.currentTimeMillis();
            boolean success = true;
            Message replyMessage = null;
            int replyCode = 0;
            try {
                String correlationID = UUID.randomUUID().toString();

                BytesMessage bytesMessage = holder.session.createBytesMessage();
                bytesMessage.writeBytes(message);
                bytesMessage.setStringProperty("subject", subject);
                bytesMessage.setStringProperty("contentType", contentType);
                bytesMessage.setBooleanProperty("jsonEncoding", useJsonEncoding);
                bytesMessage.setJMSReplyTo(holder.replyQueue);
                bytesMessage.setJMSCorrelationID(correlationID);
                holder.producer.send(dest, bytesMessage, deliveryMode, 4, timeout);
                for (; ; ) { // Ignore other message
                    replyMessage = holder.replyConsumer.receive(timeout);
                    if (replyMessage == null) {
                        break;
                    }
                    if (replyMessage.getJMSCorrelationID().equals(correlationID)) {
                        break;
                    }
                }
                if (replyMessage == null) {
                    throw new TimeoutException("Reply timeout: " + correlationID);
                }
                byte[] replyBytes = getBytes(replyMessage);
                replyCode = getReplyCode(replyMessage);
                return new IncomingMessage(
                        address,
                        replyMessage.getStringProperty("subject"),
                        replyMessage.getStringProperty("contentType"),
                        replyMessage.getBooleanProperty("jsonEncoding"),
                        replyCode,
                        replyBytes, null, null);
            } catch (Exception ex) {
                success = false;
                throw ex;
            } finally {
                if (setting.trace && tracer != null) {
                    Tracer.Info info = new Tracer.Info();
                    info.catalog = "amq";
                    info.name = "sendAndWaitForReply";
                    info.put("subject", subject);
                    info.put("contentType", contentType);
                    info.put("success", success);
                    if (success) {
                        info.put("replyCode", replyCode);
                    }
                    info.put("startTime", beginTime);
                    info.put("runningTime", System.currentTimeMillis() - beginTime);
                    tracer.trace(info);
                }
            }
		}
	}
	
	private static int getReplyCode(Message message) throws JMSException {
		if (message.propertyExists("replyCode")) {
			return message.getIntProperty("replyCode");
		} else
			return 0;
	}
	
	public class Receiver implements AutoCloseable {
		private Session session = null;
		private MessageConsumer consumer = null;
		private String address = null;

		protected Receiver(String address, boolean broadcast, String filter) throws JMSException {
			if (connection == null)
				throw new JMSException("Connection not allocate.");
			this.address = address;
			session = connection.createSession(false, 
					Session.AUTO_ACKNOWLEDGE);
			Destination dest;
			if (broadcast)
				dest = session.createTopic(address);
			else
				dest = session.createQueue(address);
			if (filter != null)
				consumer = session.createConsumer(dest, filter);
			else
				consumer = session.createConsumer(dest);
		}
		
		public String getAddress() {
			return address;
		}
		
		public IncomingMessage receive() throws IOException,
				JMSException {
            boolean success = true;
            IncomingMessage inMessage = null;
            try {
                Message message = consumer.receive();
                inMessage = new IncomingMessage(
                        address,
                        message.getStringProperty("subject"),
                        message.getStringProperty("contentType"),
                        message.getBooleanProperty("jsonEncoding"),
                        getReplyCode(message),
                        getBytes(message),
                        message.getJMSReplyTo(),
                        message.getJMSCorrelationID());
                return inMessage;
            } catch (Exception ex) {
                success = false;
                throw ex;
            } finally {
                if (setting.trace && tracer != null) {
                    Tracer.Info info = new Tracer.Info();
                    info.catalog = "amq";
                    info.name = "receive";
                    info.put("success", success);
                    if (success) {
                        info.put("subject", inMessage.getSubject());
                        info.put("contentType", inMessage.getContentType());
                        info.put("replyCode", inMessage.getReplyCode());
                    }
                    tracer.trace(info);
                }
            }
        }

		public IncomingMessage receive(long timout)
				throws IOException, JMSException {
            boolean success = true;
            IncomingMessage inMessage = null;
            try {
                Message message = consumer.receive(timout);
                inMessage = new IncomingMessage(
                        address,
                        message.getStringProperty("subject"),
                        message.getStringProperty("contentType"),
                        message.getBooleanProperty("jsonEncoding"),
                        getReplyCode(message),
                        getBytes(message),
                        message.getJMSReplyTo(),
                        message.getJMSCorrelationID());
                return inMessage;
            } catch (Exception ex) {
                success = false;
                throw ex;
            } finally {
                if (setting.trace && tracer != null) {
                    Tracer.Info info = new Tracer.Info();
                    info.catalog = "amq";
                    info.name = "receive";
                    info.put("success", success);
                    if (success) {
                        info.put("subject", inMessage.getSubject());
                        info.put("contentType", inMessage.getContentType());
                        info.put("replyCode", inMessage.getReplyCode());
                    }
                    tracer.trace(info);
                }
            }
		}

		public IncomingMessage receiveNoWait()
				throws IOException, JMSException {
            boolean success = true;
            IncomingMessage inMessage = null;
            boolean noMessage = false;
            try {
                Message message = consumer.receiveNoWait();
                if (message == null) {
                    noMessage = true;
                    success = false;
                    return null;
                } else {
                    inMessage = new IncomingMessage(
                            address,
                            message.getStringProperty("subject"),
                            message.getStringProperty("contentType"),
                            message.getBooleanProperty("jsonEncoding"),
                            getReplyCode(message),
                            getBytes(message),
                            message.getJMSReplyTo(),
                            message.getJMSCorrelationID());
                    return inMessage;
                }
            } catch (Exception ex) {
                success = false;
                throw ex;
            } finally {
                if (setting.trace && tracer != null && !noMessage) {
                    Tracer.Info info = new Tracer.Info();
                    info.catalog = "amq";
                    info.name = "receiveNoWait";
                    info.put("success", success);
                    if (success) {
                        info.put("subject", inMessage.getSubject());
                        info.put("contentType", inMessage.getContentType());
                        info.put("replyCode", inMessage.getReplyCode());
                    }
                    tracer.trace(info);
                }
            }
		}

        @Override
		public void close() {
			closeResource(consumer);
			closeResource(session);
		}

		@Override
		protected void finalize() throws Throwable {
			close();
			super.finalize();
		}
	}
	
	public class AsyncReceiver implements AutoCloseable {
		private Session session = null;
		private MessageConsumer consumer = null;
		private MessageHandler handler = null;
		private String address = null;
		
		public String getAddress() {
			return address;
		}
		
		private final MessageListener listener = new MessageListener() {
			@Override
			public void onMessage(Message message) {
                boolean success = true;
                IncomingMessage inMessage = null;
				try {
					if (handler != null) {
						inMessage = new IncomingMessage(
								address,
								message.getStringProperty("subject"), 
								message.getStringProperty("contentType"), 
								message.getBooleanProperty("jsonEncoding"),
								getReplyCode(message),
								getBytes(message), 
								message.getJMSReplyTo(), 
								message.getJMSCorrelationID());
						handler.onMessage(inMessage);
					}
				} catch (Exception e) {
                    success = false;
					e.printStackTrace();
				} finally {
                    if (setting.trace && tracer != null) {
                        Tracer.Info info = new Tracer.Info();
                        info.catalog = "amq";
                        info.name = "onReceiveMessage";
                        info.put("success", success);
                        if (success) {
                            if (handler != null) {
                                info.put("subject", inMessage.getSubject());
                                info.put("contentType", inMessage.getContentType());
                                info.put("replyCode", inMessage.getReplyCode());
                            } else {
                                info.put("handler", "null");
                            }
                        }
                        tracer.trace(info);
                    }
                }
			}
		};

		protected AsyncReceiver(String address, boolean broadcast, String filter, 
				MessageHandler handler) throws JMSException {
			if (connection == null)
				throw new JMSException("Connection not allocate.");
			this.address = address;
			this.handler = handler; 
			session = connection.createSession(false, 
					Session.AUTO_ACKNOWLEDGE);
			Destination dest;
			if (broadcast)
				dest = session.createTopic(address);
			else
				dest = session.createQueue(address);
			if (filter != null)
				consumer = session.createConsumer(dest, filter);
			else
				consumer = session.createConsumer(dest);
			consumer.setMessageListener(listener);
		}

        @Override
		public void close() {
			closeResource(consumer);
			closeResource(session);
		}
		@Override
		protected void finalize() throws Throwable {
			close();
			super.finalize();
		}
	}

    @Override
    public boolean isSettingChanged(ConfigManager configManager, String configName) {
        AMQService.AMQSetting newSetting = configManager.get(configName, AMQService.AMQSetting.class);
        return !Serializer.equals(newSetting, setting);
    }

    public AMQService(ConfigManager configManager, String configName, Tracer tracer) throws JMSException {
        this(configManager.get(configName, AMQService.AMQSetting.class), tracer);
    }

	public AMQService(AMQSetting setting) throws JMSException {
		this(setting, null);
	}

    public AMQService(AMQSetting setting, Tracer tracer) throws JMSException {
        this.setting = setting;
        this.tracer = tracer;
        ActiveMQConnectionFactory connectionFactory;
        if (Strings.isNullOrEmpty(setting.uri))
            throw new ConfigurationException("Must provide the ActiveMQ URI info.");
        if (!Strings.isNullOrEmpty(setting.user))
            connectionFactory = new ActiveMQConnectionFactory(setting.user, setting.password, setting.uri);
        else
            connectionFactory = new ActiveMQConnectionFactory(setting.uri);
        connection = connectionFactory.createConnection();
        connection.start();
    }
	
	private static void closeResource(MessageConsumer resource) {
		try {
			if (resource != null)
				resource.close();
		} catch (Exception e) {
		}
	}
	private static void closeResource(MessageProducer resource) {
		try {
			if (resource != null)
				resource.close();
		} catch (Exception e) {
		}
	}
	private static void closeResource(Session resource) {
		try {
			if (resource != null)
				resource.close();
		} catch (Exception e) {
		}
	}
	private static void closeResource(Connection resource) {
		try {
			if (resource != null) {
				resource.stop();
				resource.close();
			}
		} catch (Exception e) {
		}
	}

    @Override
	public void stop() {
		closeResource(connection);
		connection = null;
	}
	
	private byte[] getBytes(Message message) throws IOException, JMSException {
		if (message == null)
			return null;
		else {
			if (message instanceof BytesMessage) {
				BytesMessage bytesMessage = (BytesMessage)message;
				byte[] content = new byte[(int) bytesMessage.getBodyLength()];
				bytesMessage.readBytes(content);
				return content;
			} else {
				throw new JMSException("Invalid message: wrong type.");
			}
		}
	}
	
	public Replier createReplier() throws JMSException {
		return new Replier();
	}
	public Sender createSender(boolean persistent) throws JMSException {
		return new Sender(setting.address, setting.broadcast, persistent);
	}
	public Sender createSender(String address, boolean broadcast, boolean persistent) throws JMSException {
		return new Sender(address, broadcast, persistent);
	}
	public Sender createSender() throws JMSException {
		return new Sender(setting.address, setting.broadcast, false);
	}
	public Sender createSender(String address, boolean broadcast) throws JMSException {
		return new Sender(address, broadcast, false);
	}
	public Sender createSender(String address) throws JMSException {
		return new Sender(address, false, false);
	}
	public Receiver createReceiver() throws JMSException {
		return new Receiver(setting.address, setting.broadcast, null);
	}
	public Receiver createReceiver(String address,
			boolean broadcast) throws JMSException {
		return new Receiver(address, broadcast, null);
	}
	public Receiver createReceiver(String address) throws JMSException {
		return new Receiver(address, false, null);
	}
	public Receiver createReceiver(String address, String filter) 
			throws JMSException {
		return new Receiver(address, false, filter);
	}
	public Receiver createReceiver(String address,
			boolean broadcast, String filter) throws JMSException {
		return new Receiver(address, broadcast, filter);
	}
	public AsyncReceiver createAsyncReceiver(MessageHandler handler) throws JMSException {
		return new AsyncReceiver(setting.address, setting.broadcast, null, handler);
	}
	public AsyncReceiver createAsyncReceiver(String address, boolean broadcast, 
			MessageHandler handler) throws JMSException {
		return new AsyncReceiver(address, broadcast, null, handler);
	}
	public AsyncReceiver createAsyncReceiver(String address,
			MessageHandler handler) throws JMSException {
		return new AsyncReceiver(address, false, null, handler);
	}
	public AsyncReceiver createAsyncReceiver(String address, String filter,
			MessageHandler handler) throws JMSException {
		return new AsyncReceiver(address, false, filter, handler);
	}
	public AsyncReceiver createAsyncReceiver(String address, boolean broadcast, 
			String filter, MessageHandler handler) throws JMSException {
		return new AsyncReceiver(address, broadcast, filter, handler);
	}

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> interfaceType) {
        Object instance = Proxy.newProxyInstance(
                AMQProxy.class.getClassLoader(),
                new Class<?>[]{interfaceType},
                new AMQProxy(this));
        return (T)instance;
    }
}
