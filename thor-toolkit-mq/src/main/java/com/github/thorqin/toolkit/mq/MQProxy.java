/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.mq;

import com.github.thorqin.toolkit.mq.annotation.AMQMethod;
import com.github.thorqin.toolkit.utility.Serializer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author nuo.qin
 */
public class MQProxy implements InvocationHandler {
	private final MQService mq;
	public MQProxy(MQService mq) {
		this.mq = mq;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Class<?> classInterface = proxy.getClass().getInterfaces()[0];
		String address = "RMI:" + classInterface.getName();
		AMQMethod amqMethod = method.getAnnotation(AMQMethod.class);
		boolean waitResponse = true;
		if (amqMethod != null) {
			waitResponse = amqMethod.waitResponse();
		}
		MQService.Sender sender = mq.createSender(address);
        if (waitResponse) {
            MQService.IncomingMessage incoming = sender.sendAndWaitForReply(method.getName(), args);
            if (incoming.getReplyCode() == -1)
                throw new RuntimeException((String)incoming.getBody());
            if (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class)) {
                return null;
            } else {
                Object result = Serializer.fromKryo(incoming.getBodyBytes());
                return result;
            }
        } else {
            sender.send(method.getName(), args);
            return null;
        }
	}
}
