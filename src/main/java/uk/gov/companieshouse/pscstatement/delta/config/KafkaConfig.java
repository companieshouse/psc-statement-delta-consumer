package uk.gov.companieshouse.pscstatement.delta.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.pscstatement.delta.serialization.PscStatementDeltaDeserializer;

@Configuration
@EnableKafka
@Profile("!test")
public class KafkaConfig {

    private final String bootstrapServers;
    private final Integer listenerConcurrency;
    private  final PscStatementDeltaDeserializer pscStatementDeltaDeserializer;

    /**
     * Constructor.
     */
    public KafkaConfig(PscStatementDeltaDeserializer pscStatementDeltaDeserializer,
                       @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                       @Value("${spring.kafka.listener.concurrency}") Integer listenerConcurrency) {
        this.bootstrapServers = bootstrapServers;
        this.listenerConcurrency = listenerConcurrency;
        this.pscStatementDeltaDeserializer = pscStatementDeltaDeserializer;
    }

    /**
     * Kafka Consumer Factory.
     */
    @Bean
    public ConsumerFactory<String, PscStatementDelta> kafkaConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
                new ErrorHandlingDeserializer<>(pscStatementDeltaDeserializer));
    }

    /**
     * Kafka Listener Container Factory.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PscStatementDelta>
            listenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PscStatementDelta> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory());
        factory.setConcurrency(listenerConcurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        return factory;
    }

    private Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                PscStatementDeltaDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        return props;
    }
}
