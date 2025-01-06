package uk.gov.companieshouse.pscstatement.delta.mapper;

import java.util.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MapperUtils {

    private final String salt;

    public MapperUtils(@Value("${encoding.salt}") String salt) {
        this.salt = salt;
    }

    /**
     * encode the String passed in for use in links and ids.
     */
    public String encode(String unencodedString) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(DigestUtils.sha1(unencodedString + salt));
    }
}