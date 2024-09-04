package uk.gov.companieshouse.pscstatement.delta.config;

import consumer.deserialization.AvroDeserializer;
import consumer.exception.TopicErrorInterceptor;
import consumer.serialization.AvroSerializer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import uk.gov.companieshouse.delta.ChsDelta;

@TestConfiguration
public class KafkaTestContainerConfig {

    private final AvroDeserializer<ChsDelta> deserializer;
    private final AvroSerializer serializer;

    @Autowired
    public KafkaTestContainerConfig(AvroSerializer serializer,
            AvroDeserializer<ChsDelta> deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @Bean
    public KafkaContainer kafkaContainer() {
        KafkaContainer kafkaContainer = new KafkaContainer(
                DockerImageName.parse("confluentinc/cp-kafka:latest"));
        kafkaContainer.start();
        return kafkaContainer;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, ChsDelta> listenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChsDelta> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ChsDelta> kafkaConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(kafkaContainer()),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deserializer));
    }

    @Bean
    public Map<String, Object> consumerConfigs(KafkaContainer kafkaContainer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, AvroDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaContainer kafkaContainer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroDeserializer.class);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TopicErrorInterceptor.class.getName());
        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(
                props, new StringSerializer(), serializer);

        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(kafkaContainer()));
    }

    @Bean
    public KafkaConsumer<String, Object> invalidTopicConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer().getBootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "psc-statement-delta-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of("psc-statement-delta-invalid",
                "psc-statement-delta-error", "psc-statement-delta-retry"));

        return consumer;
    }


}
