package uk.gov.companieshouse.pscstatement.delta.serialization;

import java.util.Arrays;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscStatementDelta;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;



@Component
public class PscStatementDeltaDeserializer implements Deserializer<ChsDelta> {
    private final Logger logger;

    @Autowired
    public PscStatementDeltaDeserializer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public ChsDelta deserialize(String topic, byte[] data) {
        logger.info(String.format("Message picked up from topic with data: %s",
                new String(data)));
        //        byte [] trimmedData = Arrays.copyOf(data, data.length - 29);
        //        logger.info(String.format("Trimmed data to : %s",
        //                new String(trimmedData)));
        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<ChsDelta> reader =
                    new ReflectDatumReader<>(ChsDelta.class);
            ChsDelta pscStatementDelta = reader.read(null, decoder);
            logger.info(String.format("Message successfully de-serialised into "
                    + "Avro pscStatementDelta object: %s", pscStatementDelta));
            return pscStatementDelta;
        } catch (Exception ex) {
            logger.error("De-Serialization exception while converting to Avro schema object", ex);
            throw new RuntimeException(ex);
        }
    }
}
