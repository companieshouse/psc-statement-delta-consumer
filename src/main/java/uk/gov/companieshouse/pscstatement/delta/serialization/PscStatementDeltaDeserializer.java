package uk.gov.companieshouse.pscstatement.delta.serialization;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.logging.Logger;

@Component
public class PscStatementDeltaDeserializer implements Deserializer<PscStatementDelta> {
    private final Logger logger;

    @Autowired
    public PscStatementDeltaDeserializer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public PscStatementDelta deserialize(String topic, byte[] data) {
        try {
            logger.trace(String.format("Message picked up from topic with data: %s",
                    new String(data)));
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<PscStatementDelta> reader;
            reader = new ReflectDatumReader<>(PscStatementDelta.class);
            PscStatementDelta pscStatementDelta = reader.read(null, decoder);
            logger.trace(String.format("Message successfully de-serialised into "
                    + "Avro pscStatementDelta object: %s", pscStatementDelta));
            return pscStatementDelta;
        } catch (Exception ex) {
            logger.error("De-Serialization exception while converting to Avro schema object", ex);
            throw new RuntimeException(ex);
        }
    }
}
