package uk.gov.companieshouse.pscstatement.delta.mapper;

import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;

public class MapperUtils {
    /**
     * encode the String passed in for use in links and ids.
     */
    public static String encode(String unencodedString) {
        String salt = "ks734s_sdgOc4£b2";
        String encodedString = Base64.getUrlEncoder().withoutPadding().encodeToString(
                DigestUtils.sha1(unencodedString + salt));
        return encodedString;
    }
}