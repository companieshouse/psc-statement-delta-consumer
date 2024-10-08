package uk.gov.companieshouse.pscstatement.delta.config;

import consumer.deserialization.AvroDeserializer;
import consumer.serialization.AvroSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    @Bean
    SerializerFactory serializerFactory() {
        return new SerializerFactory();
    }

    @Bean
    EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    @Bean
    AvroSerializer serializer() {
        return new AvroSerializer();
    }

    @Bean
    AvroDeserializer<ChsDelta> deserializer() {
        return new AvroDeserializer<>(ChsDelta.class);
    }
}
