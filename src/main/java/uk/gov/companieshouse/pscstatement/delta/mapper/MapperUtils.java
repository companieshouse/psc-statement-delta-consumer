package uk.gov.companieshouse.pscstatement.delta.mapper;

import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;

public class MapperUtils {

    @Value("${encoding.salt}")
    private static String salt;

    /**
     * encode the String passed in for use in links and ids.
     */
    public static String encode(String unencodedString) {
        String encodedString = Base64.getUrlEncoder().withoutPadding().encodeToString(
                DigestUtils.sha1(unencodedString + salt));
        return encodedString;
    }
}