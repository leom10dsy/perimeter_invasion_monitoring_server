package com.csrd.pims.tools.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHeaders;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class MqttUtils {

	public static final String MQTT_MESSAGE_SERVICE_SUFFIX = "MessageServiceImpl";

	public static AtomicReference<String> findIncomingMessageTopicOfGroup(
			Map<String, Set<String>> configInboundTopicGroups,
			String incomingMessageTopic) {

		AtomicReference<String> topicGroupName = new AtomicReference<>();

		configInboundTopicGroups
				.forEach((topicGroup, topicList) -> topicList
						.forEach((configTopic) -> {
							if (MqttTopic.isMatched(configTopic, incomingMessageTopic)) {
								topicGroupName.set(topicGroup);
							}
						}));
		return topicGroupName;
	}

	public static Optional<String> getMessageTopic(MessageHeaders messageHeaders) {
		// msg header: [mqtt_receivedRetained, id, mqtt_duplicate, mqtt_receivedTopic,
		// mqtt_receivedQos, timestamp]
		// 打印头信息
		// log.info("Message header info: {}",
		// Arrays.toString(messageHeaders.entrySet().toArray()));
		// 消息主题
		String receivedTopicHeader = MqttHeaders.RECEIVED_TOPIC;

		if (!messageHeaders.containsKey(receivedTopicHeader)) {
			log.error("Illegal message header: {}", receivedTopicHeader);
			return Optional.empty();
		}
		return Optional.ofNullable(messageHeaders.get(receivedTopicHeader, String.class));

	}

	public static String getMessageTypeByTopic(String topic) {

		String[] split = topic.split(MqttTopic.TOPIC_LEVEL_SEPARATOR);

		return split[split.length - 1];
	}

	public static String messageServiceBeanNameBuilder(String messageTypeName) {
		return messageTypeName + MQTT_MESSAGE_SERVICE_SUFFIX;
	}

}
