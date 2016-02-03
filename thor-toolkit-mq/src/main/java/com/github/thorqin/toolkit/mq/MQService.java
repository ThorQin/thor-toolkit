package com.github.thorqin.toolkit.mq;

import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.service.IService;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.utility.Serializer;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;

import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.validation.Validator;
import com.github.thorqin.toolkit.validation.annotation.ValidateString;
import com.google.common.base.Strings;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.qpid.jms.JmsConnectionFactory;

public class MQService implements IService {

    public interface ConnectionListener {
        void onConnect();
        void onDisconnect();
    }

    private ExceptionListener exceptionListener = new ExceptionListener() {
        @Override
        public void onException(JMSException e) {
            logger.log(Level.WARNING, "MQService connection exception", e);
            connection = null;
            synchronized (connectionListeners) {
                for (ConnectionListener listener : connectionListeners) {
                    listener.onDisconnect();
                }
            }
            tryConnect();
        }
    };

    public static class MQSetting {
        @ValidateString
        public String uri;
        public String user;
        public String password;
        @ValidateString("^(activemq|amqp)$")
        public String broker = "activemq";
        @ValidateString
        public String address = "default";
        public boolean broadcast = false;
        public boolean trace = false;
    }

	@Service("logger")
    private Logger logger =
            Logger.getLogger(MQService.class.getName());
    private ConnectionFactory connectionFactory;
	private MQSetting setting;
	private Connection connection = null;
    @Service("tracer")
    private Tracer tracer = null;
	private final ThreadLocal<ProducerHolder> localProducer = new ThreadLocal<>();
    private Set<ConnectionListener> connectionListeners = new HashSet<>();
    private boolean isRunning = false;
    private String serviceName = null;

    public void addConnectionListener(ConnectionListener listener) {
        synchronized (connectionListeners) {
            if (!connectionListeners.contains(listener))
                connectionListeners.add(listener);
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        synchronized (connectionListeners) {
            if (!connectionListeners.contains(listener))
                connectionListeners.remove(listener);
        }
    }

    public void clearConnectionListener() {
        synchronized (connectionListeners) {
            connectionListeners.clear();
        }
    }
	
	private class ProducerHolder implements AutoCloseable {
		public Session session = null;
		public MessageProducer producer = null;
		public Destination replyQueue = null;
		public MessageConsumer replyConsumer = null;
		public ProducerHolder() throws JMSException {
            if (connection == null) {
                throw new JMSException("Connection isn't established!");
            }
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

    private void clearHolder() {
        localProducer.remove();
    }

    public enum MessageEncoding {
        JSON,
        KRYO,
        RAW
    }

	public class IncomingMessage {
		private final String address;
		private final String subject;
		private final String contentType;
		private final int replyCode;
		private final MessageEncoding encoding;
		private final byte[] body;
		private final Destination replyDestination;
		private final String correlationID;

		public IncomingMessage(
				String address, 
				String subject,
				String contentType,
                MessageEncoding encoding,
				int replyCode,
				byte[] body,
				Destination replyDestination,
				String correlationID) {
			this.address = address;
			this.subject = subject;
			this.contentType = contentType;
			this.encoding = encoding;
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
		public MessageEncoding getEncoding() {
			return encoding;
		}
		public int getReplyCode() {
			return replyCode;
		}
		public byte[] getBodyBytes() {
			return body;
		}
		public <T> T getBody() throws IOException {
			if (encoding == MessageEncoding.JSON) {
				return Serializer.fromJson(body);
			} else if (encoding == MessageEncoding.KRYO) {
				return Serializer.fromKryo(body);
			} else
                throw new IOException("Raw message cannot be convert to specified type.");
		}
		public <T> T getBody(Type type) throws IOException {
            if (encoding == MessageEncoding.JSON) {
				return Serializer.fromJson(body, type);
			} else if (encoding == MessageEncoding.KRYO) {
				return Serializer.fromKryo(body);
			} else
                throw new IOException("Raw message cannot be convert to specified type.");
		}
		public <T> T getBody(Class<T> type) throws IOException {
            if (encoding == MessageEncoding.JSON) {
				return Serializer.fromJson(body, type);
            } else if (encoding == MessageEncoding.KRYO) {
				return Serializer.fromKryo(body);
			} else
                throw new IOException("Raw message cannot be convert to specified type.");
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
		public void reply(byte[] replyMessage, String contentType, MessageEncoding encoding)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyMessage, contentType, encoding, replyDestination, correlationID);
		}
		public void reply(int replyCode, byte[] replyMessage, String contentType, MessageEncoding encoding)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyCode, replyMessage, contentType, encoding, replyDestination, correlationID);
		}
		public void reply(byte[] replyMessage, String contentType, MessageEncoding encoding, long timeToLive)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, 0, replyMessage, contentType, encoding, replyDestination, correlationID, timeToLive);
		}
		public void reply(int replyCode, byte[] replyMessage, String contentType, MessageEncoding encoding, long timeToLive)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyCode, replyMessage, contentType, encoding, replyDestination, correlationID, timeToLive);
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
		public <T> void reply(T replyMessage, MessageEncoding encoding)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyMessage, encoding, replyDestination, correlationID);
		}
		public <T> void reply(int replyCode, T replyMessage, MessageEncoding encoding)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyCode, replyMessage, encoding, replyDestination, correlationID);
		}
		public <T> void reply(T replyMessage, MessageEncoding encoding, long timeToLive)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyMessage, encoding, replyDestination, correlationID, timeToLive);
		}
		public <T> void reply(int replyCode, T replyMessage, MessageEncoding encoding, long timeToLive)
				throws JMSException, IOException {
			if (replyDestination == null)
				return;
			Replier replier = createReplier();
			replier.reply(subject, replyCode, replyMessage, encoding, replyDestination, correlationID, timeToLive);
		}

        public void reply(int replyCode, byte[] replyMessage, long timeToLive)
                throws JMSException, IOException {
            if (replyDestination == null)
                return;
            Replier replier = createReplier();
            replier.reply(subject, replyCode, replyMessage, MessageEncoding.RAW, replyDestination, correlationID, timeToLive);
        }

        public void reply(int replyCode, String replyMessage, long timeToLive)
                throws JMSException, IOException {
            if (replyDestination == null)
                return;
            Replier replier = createReplier();
            replier.reply(subject, replyCode, replyMessage == null ? null: replyMessage.getBytes("utf-8"),
                    MessageEncoding.RAW, replyDestination, correlationID, timeToLive);
        }

        public void reply(int replyCode, byte[] replyMessage)
                throws JMSException, IOException {
            if (replyDestination == null)
                return;
            Replier replier = createReplier();
            replier.reply(subject, replyCode, replyMessage, MessageEncoding.RAW, replyDestination, correlationID);
        }

        public void reply(int replyCode, String replyMessage)
                throws JMSException, IOException {
            if (replyDestination == null)
                return;
            Replier replier = createReplier();
            replier.reply(subject, replyCode, replyMessage == null ? null: replyMessage.getBytes("utf-8"),
                    MessageEncoding.RAW, replyDestination, correlationID);
        }

        public void reply(byte[] replyMessage)
                throws JMSException, IOException {
            if (replyDestination == null)
                return;
            Replier replier = createReplier();
            replier.reply(subject, replyMessage, MessageEncoding.RAW, replyDestination, correlationID);
        }

        public void reply(String replyMessage)
                throws JMSException, IOException {
            if (replyDestination == null)
                return;
            Replier replier = createReplier();
            replier.reply(subject, replyMessage == null ? null: replyMessage.getBytes("utf-8"),
                    MessageEncoding.RAW, replyDestination, correlationID);
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
            if (!isRunning)
                throw new JMSException("MQ Service not started!");
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
			reply(subject, replyCode, bytes, Serializer.getTypeName(replyMessage), MessageEncoding.KRYO, destination, correlationID, timeToLive);
		}
		public <T> void reply(String subject, T replyMessage, MessageEncoding encoding, Destination destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, 0, replyMessage, encoding, destination, correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, T replyMessage, MessageEncoding encoding, String destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, 0, replyMessage, encoding, holder.session.createQueue(destination), correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, int replyCode, T replyMessage, MessageEncoding encoding, Destination destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, encoding, destination, correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, int replyCode, T replyMessage, MessageEncoding encoding, String destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, encoding, holder.session.createQueue(destination), correlationID, defaultTimeToLive);
		}
		public <T> void reply(String subject, T replyMessage, MessageEncoding encoding, Destination destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, 0, replyMessage, encoding, destination, correlationID, timeToLive);
		}
		public <T> void reply(String subject, T replyMessage, MessageEncoding encoding, String destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, 0, replyMessage, encoding, holder.session.createQueue(destination), correlationID, timeToLive);
		}
		public <T> void reply(String subject, int replyCode, T replyMessage, MessageEncoding encoding, String destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, encoding,
					holder.session.createQueue(destination), correlationID, timeToLive);
		}
		
		public <T> void reply(String subject, int replyCode, T replyMessage, MessageEncoding encoding, Destination destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			byte[] bytes;
			if (encoding == MessageEncoding.JSON)
				bytes = Serializer.toJsonBytes(replyMessage);
			else if (encoding == MessageEncoding.KRYO)
				bytes = Serializer.toKryo(replyMessage);
            else
                throw new IOException("Cannot specify RAW encoding");
			reply(subject, replyCode, bytes, Serializer.getTypeName(replyMessage), encoding, destination, correlationID, timeToLive);
		}
		public void reply(String subject, byte[] replyMessage, String contentType, MessageEncoding encoding, Destination destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, 0, replyMessage, contentType, encoding, destination, correlationID, defaultTimeToLive);
		}
		public void reply(String subject, byte[] replyMessage, String contentType, MessageEncoding encoding, String destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, 0, replyMessage, contentType, encoding, holder.session.createQueue(destination), correlationID, defaultTimeToLive);
		}
		public void reply(String subject, int replyCode, byte[] replyMessage, String contentType, MessageEncoding encoding, Destination destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, contentType, encoding, destination, correlationID, defaultTimeToLive);
		}
		public void reply(String subject, int replyCode, byte[] replyMessage, String contentType, MessageEncoding encoding, String destination,
				String correlationID) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, contentType, encoding, holder.session.createQueue(destination), correlationID, defaultTimeToLive);
		}
		public void reply(String subject, int replyCode, byte[] replyMessage, String contentType, MessageEncoding encoding, Destination destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
            long beginTime = System.currentTimeMillis();
            boolean success = true;
            try {
                BytesMessage bytesMessage = holder.session.createBytesMessage();
                bytesMessage.writeBytes(replyMessage);
                bytesMessage.setJMSCorrelationID(correlationID);
                bytesMessage.setStringProperty("subject", subject);
                bytesMessage.setStringProperty("contentType", contentType);
                bytesMessage.setStringProperty("encoding", encoding.toString());
                bytesMessage.setIntProperty("replyCode", replyCode);
                holder.producer.send(destination, bytesMessage, deliveryMode, 5, timeToLive);
            } catch (Exception ex) {
                success = false;
                throw ex;
            } finally {
                if (setting.trace && tracer != null) {
                    Tracer.Info info = new Tracer.Info();
                    info.catalog = "mq";
                    info.name = "reply";
                    info.put("success", success);
                    info.put("serviceName", serviceName);
                    info.put("subject", subject);
                    info.put("contentType", contentType);
                    info.put("startTime", beginTime);
                    info.put("runningTime", System.currentTimeMillis() - beginTime);
                    tracer.trace(info);
                }
            }
        }
		public void reply(String subject, int replyCode, byte[] replyMessage, String contentType,
                          MessageEncoding encoding, String destination,
				String correlationID, long timeToLive) throws IOException, JMSException {
			reply(subject, replyCode, replyMessage, contentType, encoding,
					holder.session.createQueue(destination),correlationID, timeToLive);
		}

        public void reply(String subject, int replyCode, byte[] replyMessage, String destination,
                          String correlationID, long timeToLive) throws IOException, JMSException {
            reply(subject, replyCode, replyMessage, Serializer.getTypeName(replyMessage),
                    MessageEncoding.RAW, destination, correlationID, timeToLive);
        }

        public void reply(String subject, int replyCode, String replyMessage, String destination,
                          String correlationID, long timeToLive) throws IOException, JMSException {
            reply(subject, replyCode, replyMessage == null ? null : replyMessage.getBytes("utf-8"),
                    Serializer.getTypeName(replyMessage),
                    MessageEncoding.RAW, destination, correlationID, timeToLive);
        }

        public void reply(String subject, int replyCode, byte[] replyMessage, String destination,
                          String correlationID) throws IOException, JMSException {
            reply(subject, replyCode, replyMessage, Serializer.getTypeName(replyMessage),
                    MessageEncoding.RAW, destination, correlationID, defaultTimeToLive);
        }

        public void reply(String subject, int replyCode, String replyMessage, String destination,
                          String correlationID) throws IOException, JMSException {
            reply(subject, replyCode, replyMessage == null ? null : replyMessage.getBytes("utf-8"),
                    Serializer.getTypeName(replyMessage),
                    MessageEncoding.RAW, destination, correlationID, defaultTimeToLive);
        }

        public void reply(String subject, byte[] replyMessage, String destination,
                          String correlationID) throws IOException, JMSException {
            reply(subject, 0, replyMessage, Serializer.getTypeName(replyMessage),
                    MessageEncoding.RAW, destination, correlationID, defaultTimeToLive);
        }

        public void reply(String subject, String replyMessage, String destination,
                          String correlationID) throws IOException, JMSException {
            reply(subject, 0, replyMessage == null ? null : replyMessage.getBytes("utf-8"),
                    Serializer.getTypeName(replyMessage),
                    MessageEncoding.RAW, destination, correlationID, defaultTimeToLive);
        }
	}
	
	public class Sender {
		private ProducerHolder holder = null;
		private String address = null;
		private Destination dest = null;
		private int deliveryMode = DeliveryMode.NON_PERSISTENT;
		private final long defaultTimeout = 30000l;
		protected Sender(String address, boolean broadcast, boolean persistent) throws JMSException {
            if (!isRunning)
                throw new JMSException("AMQ Service not started!");
			if (connection == null)
				throw new JMSException("Connection not allocate.");
			this.address = address;
			holder = createHolder();
            try {
                if (broadcast)
                    dest = holder.session.createTopic(address);
                else
                    dest = holder.session.createQueue(address);
            } catch (javax.jms.IllegalStateException ex) {
                clearHolder();
                holder = createHolder();
                if (broadcast)
                    dest = holder.session.createTopic(address);
                else
                    dest = holder.session.createQueue(address);
            }
			deliveryMode = persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
		}
		public String getAddress() {
			return address;
		}
		public <T> void send(String subject, T message) throws IOException, JMSException {
			byte[] bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), MessageEncoding.KRYO, 0);
		}
		public <T> void send(String subject, T message, String replyAddress, String correlationID) throws IOException, JMSException {
			byte[] bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), replyAddress, correlationID, MessageEncoding.KRYO, 0);
		}
		public <T> void send(String subject, T message, MessageEncoding encoding) throws IOException, JMSException {
			byte[] bytes;
			if (encoding == MessageEncoding.JSON)
				bytes = Serializer.toJsonBytes(message);
            else if (encoding == MessageEncoding.KRYO)
				bytes = Serializer.toKryo(message);
            else
                throw new IOException("Cannot specify RAW encoding");
			send(subject, bytes, Serializer.getTypeName(message), encoding, 0);
		}
		public <T> void send(String subject, T message, String replyAddress, String correlationID,
                             MessageEncoding encoding) throws IOException, JMSException {
			byte[] bytes;
            if (encoding == MessageEncoding.JSON)
                bytes = Serializer.toJsonBytes(message);
            else if (encoding == MessageEncoding.KRYO)
                bytes = Serializer.toKryo(message);
            else
                throw new IOException("Cannot specify RAW encoding");
			send(subject, bytes, Serializer.getTypeName(message), replyAddress, correlationID, encoding, 0);
		}
		public <T> void send(String subject, T message, long timeToLive) throws IOException, JMSException {
			byte[] bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), MessageEncoding.KRYO, timeToLive);
		}
		public <T> void send(String subject, T message, String replyAddress,
                             String correlationID, long timeToLive) throws IOException, JMSException {
			byte[] bytes = Serializer.toKryo(message);
			send(subject, bytes, Serializer.getTypeName(message), replyAddress,
                    correlationID, MessageEncoding.KRYO, timeToLive);
		}
		public <T> void send(String subject, T message, MessageEncoding encoding,
                             long timeToLive) throws IOException, JMSException {
			byte[] bytes;
            if (encoding == MessageEncoding.JSON)
                bytes = Serializer.toJsonBytes(message);
            else if (encoding == MessageEncoding.KRYO)
                bytes = Serializer.toKryo(message);
            else
                throw new IOException("Cannot specify RAW encoding");
			send(subject, bytes, Serializer.getTypeName(message), encoding, timeToLive);
		}
		public <T> void send(String subject, T message, String replyAddress, String correlationID,
                             MessageEncoding encoding, long timeToLive) throws IOException, JMSException {
			byte[] bytes;
            if (encoding == MessageEncoding.JSON)
                bytes = Serializer.toJsonBytes(message);
            else if (encoding == MessageEncoding.KRYO)
                bytes = Serializer.toKryo(message);
            else
                throw new IOException("Cannot specify RAW encoding");
			send(subject, bytes, Serializer.getTypeName(message), replyAddress, correlationID, encoding, timeToLive);
		}

        public <T> void send(String subject, byte[] message,
                             long timeToLive) throws IOException, JMSException {
            send(subject, message, Serializer.getTypeName(message), MessageEncoding.RAW, timeToLive);
        }

        public <T> void send(String subject, String message,
                             long timeToLive) throws IOException, JMSException {
            send(subject, message == null ? null : message.getBytes("utf-8"),
                    Serializer.getTypeName(message), MessageEncoding.RAW, timeToLive);
        }

        public <T> void send(String subject, byte[] message) throws IOException, JMSException {
            send(subject, message, Serializer.getTypeName(message), MessageEncoding.RAW, defaultTimeout);
        }

        public <T> void send(String subject, String message) throws IOException, JMSException {
            send(subject, message == null ? null : message.getBytes("utf-8"),
                    Serializer.getTypeName(message), MessageEncoding.RAW, defaultTimeout);
        }

        public <T> void send(String subject, byte[] message, String replyAddress, String correlationID,
                             long timeToLive) throws IOException, JMSException {
            send(subject, message, Serializer.getTypeName(message), replyAddress,
                    correlationID, MessageEncoding.RAW, timeToLive);
        }

        public <T> void send(String subject, String message, String replyAddress, String correlationID,
                             long timeToLive) throws IOException, JMSException {
            send(subject, message == null ? null : message.getBytes("utf-8"),
                    Serializer.getTypeName(message), replyAddress,
                    correlationID, MessageEncoding.RAW, timeToLive);
        }

        public <T> void send(String subject, byte[] message,
                             String replyAddress, String correlationID) throws IOException, JMSException {
            send(subject, message, Serializer.getTypeName(message), replyAddress,
                    correlationID, MessageEncoding.RAW, defaultTimeout);
        }

        public <T> void send(String subject, String message, String replyAddress, String correlationID) throws IOException, JMSException {
            send(subject, message == null ? null : message.getBytes("utf-8"),
                    Serializer.getTypeName(message), replyAddress,
                    correlationID, MessageEncoding.RAW, defaultTimeout);
        }

		public void send(String subject, byte[] message, String contentType,
                         MessageEncoding encoding, long timeToLive)
				throws IOException, JMSException {
            long beginTime = System.currentTimeMillis();
            boolean success = true;
            try {
                BytesMessage bytesMessage = holder.session.createBytesMessage();
                bytesMessage.writeBytes(message);
                bytesMessage.setStringProperty("subject", subject);
                bytesMessage.setStringProperty("contentType", contentType);
                bytesMessage.setStringProperty("encoding", encoding.toString());
                holder.producer.send(dest, bytesMessage, deliveryMode, 4, timeToLive);
            } catch (Exception ex) {
                success = false;
                throw ex;
            } finally {
                if (setting.trace && tracer != null) {
                    Tracer.Info info = new Tracer.Info();
                    info.catalog = "mq";
                    info.name = "send";
                    info.put("success", success);
                    info.put("serviceName", serviceName);
                    info.put("subject", subject);
                    info.put("contentType", contentType);
                    info.put("startTime", beginTime);
                    info.put("runningTime", System.currentTimeMillis() - beginTime);
                    tracer.trace(info);
                }
            }
		}
		public void send(String subject, byte[] message, String contentType, String replyAddress,
                         String correlationID, MessageEncoding encoding, long timeToLive)
				throws IOException, JMSException {
            long beginTime = System.currentTimeMillis();
            boolean success = true;
            try {
                BytesMessage bytesMessage = holder.session.createBytesMessage();
                bytesMessage.writeBytes(message);
                bytesMessage.setStringProperty("subject", subject);
                bytesMessage.setStringProperty("contentType", contentType);
                bytesMessage.setStringProperty("encoding", encoding.toString());
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
                    info.catalog = "mq";
                    info.name = "send";
                    info.put("success", success);
                    info.put("serviceName", serviceName);
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
			return sendAndWaitForReply(subject, bytes, contentType, MessageEncoding.KRYO, timeout);
		}
		public <T> IncomingMessage sendAndWaitForReply(String subject, T message, MessageEncoding encoding, long timeout)
				throws IOException, JMSException, TimeoutException {
			byte[] bytes;
            if (encoding == MessageEncoding.JSON)
                bytes = Serializer.toJsonBytes(message);
            else if (encoding == MessageEncoding.KRYO)
                bytes = Serializer.toKryo(message);
            else
                throw new IOException("Cannot specify RAW encoding");
			String contentType = Serializer.getTypeName(message);
			return sendAndWaitForReply(subject, bytes, contentType, encoding, timeout);
		}

		public IncomingMessage sendAndWaitForReply(String subject, byte[] message, String contentType,
                                                   MessageEncoding encoding, long timeout) throws IOException,
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
                bytesMessage.setStringProperty("encoding", encoding.toString());
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
                        parseMessageEncoding(replyMessage.getStringProperty("encoding")),
                        replyCode,
                        replyBytes, null, null);
            } catch (Exception ex) {
                success = false;
                throw ex;
            } finally {
                if (setting.trace && tracer != null) {
                    Tracer.Info info = new Tracer.Info();
                    info.catalog = "mq";
                    info.name = "sendAndWaitForReply";
                    info.put("subject", subject);
                    info.put("contentType", contentType);
                    info.put("success", success);
                    info.put("serviceName", serviceName);
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

    private static MessageEncoding parseMessageEncoding(String encoding) {
        MessageEncoding replyEncoding;
        if (encoding == null) {
            replyEncoding = MessageEncoding.RAW;
        } else if (encoding.equals(MessageEncoding.JSON.toString())) {
            replyEncoding = MessageEncoding.JSON;
        } else if (encoding.equals(MessageEncoding.KRYO.toString())) {
            replyEncoding = MessageEncoding.KRYO;
        } else
            replyEncoding = MessageEncoding.RAW;
        return replyEncoding;
    }
	
	public class Receiver implements AutoCloseable {
		private Session session = null;
		private MessageConsumer consumer = null;
		private String address = null;

		protected Receiver(String address, boolean broadcast, String filter) throws JMSException {
            if (!isRunning)
                throw new JMSException("AMQ Service not started!");
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
                        parseMessageEncoding(message.getStringProperty("encoding")),
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
                    info.catalog = "mq";
                    info.name = "receive";
                    info.put("success", success);
                    info.put("serviceName", serviceName);
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
                        parseMessageEncoding(message.getStringProperty("encoding")),
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
                    info.catalog = "mq";
                    info.name = "receive";
                    info.put("serviceName", serviceName);
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
                            parseMessageEncoding(message.getStringProperty("encoding")),
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
                    info.catalog = "mq";
                    info.name = "receiveNoWait";
                    info.put("success", success);
                    info.put("serviceName", serviceName);
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
                                parseMessageEncoding(message.getStringProperty("encoding")),
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
                        info.catalog = "mq";
                        info.name = "onReceiveMessage";
                        info.put("success", success);
                        info.put("serviceName", serviceName);
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
            if (!isRunning)
                throw new JMSException("AMQ Service not started!");
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

    public MQService() {
        this.setting = null;
        this.tracer = null;
    }

	public MQService(MQSetting setting) throws ValidateException {
		this(setting, null);
	}

    public MQService(MQSetting setting, Tracer tracer) throws ValidateException {
        validateSetting(setting);
        this.setting = setting;
        this.tracer = tracer;
    }

    private void validateSetting(MQSetting mailSetting) throws ValidateException {
        Validator validator = new Validator(Localization.getInstance());
        validator.validateObject(mailSetting, MQSetting.class, false);
    }

    @Override
    public boolean config(ConfigManager configManager, String serviceName, boolean isReConfig) {
        this.serviceName = serviceName;
        MQSetting newSetting = configManager.get(serviceName, MQSetting.class);
        try {
            validateSetting(newSetting);
        } catch (ValidateException ex) {
            logger.log(Level.SEVERE, "Invalid MQ configuration settings: {0}", ex.getMessage());
            return false;
        }
        boolean needRestart = !Serializer.equals(newSetting, setting);
        setting = newSetting;
        return needRestart;
    }

    @Override
    public boolean isStarted() {
        return isRunning;
    }

    @Override
    public synchronized void start() {
        if (isRunning)
            return;
        if (setting.broker.equals("activemq")) {
            if (!Strings.isNullOrEmpty(setting.user)) {
                connectionFactory = new ActiveMQConnectionFactory(setting.user, setting.password, setting.uri);
            } else {
                connectionFactory = new ActiveMQConnectionFactory(setting.uri);
            }
        } else { // Used AMQP
            if (!Strings.isNullOrEmpty(setting.user)) {
                connectionFactory = new JmsConnectionFactory(setting.user, setting.password, setting.uri);
            } else {
                connectionFactory = new JmsConnectionFactory(setting.uri);
            }
        }
        connection = null;
        isRunning = true;
        tryConnect();
    }

    public void connect() throws JMSException {
        if (!isRunning)
            throw new JMSException("AMQ Service not started!");
        connection = connectionFactory.createConnection();
        connection.start();
    }

    private void tryConnect() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning && connection == null) {
                    try {
                        connect();
                        synchronized (connectionListeners) {
                            for (ConnectionListener listener : connectionListeners) {
                                listener.onConnect();
                            }
                        }
                        connection.setExceptionListener(exceptionListener);
                    } catch (JMSException e) {
                        connection = null;
                        logger.log(Level.SEVERE, "Try connect failed! (Service name: {0}): {1}",
                                new Object[]{serviceName, e.getMessage()});
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ex)  {}
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public synchronized void stop() {
        if (!isRunning)
            return;
        isRunning = false;
        closeResource(connection);
        connectionFactory = null;
        connection = null;
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


	private static byte[] getBytes(Message message) throws IOException, JMSException {
		if (message == null)
			return null;
		else {
			if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                byte[] content = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(content);
                return content;
            } else if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                if (text == null)
                    return null;
                else
                    return text.getBytes("utf-8");
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
                MQProxy.class.getClassLoader(),
                new Class<?>[]{interfaceType},
                new MQProxy(this));
        return (T)instance;
    }
}
