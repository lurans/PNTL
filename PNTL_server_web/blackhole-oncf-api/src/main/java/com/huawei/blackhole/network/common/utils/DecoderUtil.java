package com.huawei.blackhole.network.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.bc.BcRSASignerInfoVerifierBuilder;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

public class DecoderUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DecoderUtil.class);

    /**
     * token解析
     *
     * @param token       String
     * @param signingCert String
     * @return getTokenContentAsString
     */
    public static String decodeToken(String token, String signingCert) {
        if (StringUtils.isEmpty(signingCert)) {
            LOG.error("decodeToken: signing cert is null or empty");
            return null;
        }

        try {
            CMSSignedData lSignedData = getSignedDataFromRawToken(token);
            SignerInformationVerifier lVerifier = createTokenVerifier(signingCert);
            if (null == lVerifier) {
                return null;
            }
            boolean isValidToken = isValidTokenSignature(lSignedData, lVerifier);
            if (isValidToken) {
                return getTokenContentAsString(lSignedData);
            }
        } catch (Exception e) {
            LOG.error("decode token exception", e);
        }
        return null;
    }

    /**
     * 创建Token
     *
     * @param aInSigningCertificate String
     * @return lVerifier
     * @throws IOException               Exception
     * @throws OperatorCreationException Exception
     */
    public static SignerInformationVerifier createTokenVerifier(String aInSigningCertificate) throws IOException,
            OperatorCreationException {
        // cert is PEM encoded - need to translate those bytes into a PEM object
        Reader lCertBufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                aInSigningCertificate.getBytes("utf-8")), "utf-8"));

        @SuppressWarnings("resource")
        PemObject lPemObj = new PemReader(lCertBufferedReader).readPemObject();
        if (null == lPemObj) {
            return null;
        }

        // verify builder - basically make object that will verify the cert
        BcRSASignerInfoVerifierBuilder signerInfoBuilder = new BcRSASignerInfoVerifierBuilder(
                new DefaultCMSSignatureAlgorithmNameGenerator(), new DefaultSignatureAlgorithmIdentifierFinder(),
                new DefaultDigestAlgorithmIdentifierFinder(), new BcDigestCalculatorProvider());

        // Using the PEM object, create a cert holder and a verifier for cert
        SignerInformationVerifier lVerifier = signerInfoBuilder.build(new X509CertificateHolder(lPemObj.getContent()));

        return lVerifier;
    }

    /**
     * 从Token获得签名
     *
     * @param lRawKeystoneToken String
     * @return lSignedData
     * @throws CMSException Exception
     * @throws IOException  Exception
     */
    public static CMSSignedData getSignedDataFromRawToken(String lRawKeystoneToken) throws CMSException, IOException {
        // Keystone takes all '/' characters and replaced them by '-' in order
        // to encode the token into base 64. Let's reverse that..
        String lRealTokenData = lRawKeystoneToken.replace("-", "/");
        byte[] lData = Base64.decodeBase64(lRealTokenData.getBytes("utf-8"));

        // the raw encoded token we can make a CMSSigned data out of it
        CMSSignedData lSignedData = new CMSSignedData(lData);
        return lSignedData;
    }

    /**
     * 将Token内容转化为字符串
     *
     * @param aInSignedData CMSSignedData
     * @return lObjString
     * @throws IOException Exception
     */
    public static String getTokenContentAsString(CMSSignedData aInSignedData) throws IOException {
        Object lObj = aInSignedData.getSignedContent().getContent();
        if (lObj instanceof byte[]) {
            String lObjString = new String((byte[]) lObj, "utf-8");
            return lObjString;
        }
        return null;
    }

    /**
     * Token签名是否有效
     *
     * @param aInSignedData CMSSignedData
     * @param aInVerifier   SignerInformationVerifier
     * @return boolean
     * @throws CMSException Exception
     */
    public static boolean isValidTokenSignature(CMSSignedData aInSignedData, SignerInformationVerifier aInVerifier)
            throws CMSException {
        /*
         * The token contained the signer Info and has been parsed out For each
         * signer on the token, attempt to verify against the certificate
         */
        SignerInformationStore lSignerInfo = aInSignedData.getSignerInfos();
        @SuppressWarnings("rawtypes")
        Collection lSigners = lSignerInfo.getSigners();
        for (Object lObj : lSigners) {
            if (lObj instanceof SignerInformation) {
                SignerInformation lSigner = (SignerInformation) lObj;
                boolean lIsValid = lSigner.verify(aInVerifier);
                if (lIsValid) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * MD5哈希
     *
     * @param message String
     * @return hd
     * @throws NoSuchAlgorithmException     Exception
     * @throws UnsupportedEncodingException Exception
     */
    public static String hashMD5(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String hd;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(message.getBytes("utf-8"));
        BigInteger hash = new BigInteger(1, md5.digest());
        // BigInteger strips leading 0's
        hd = hash.toString(16);
        while (hd.length() < 32) {
            // pad with leading 0's
            hd = "0" + hd;
        }
        return hd;
    }
}
