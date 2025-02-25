/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: AssociationManager.java,v 1.1 2008-10-27 14:17:22 monzillo Exp $
 */
package com.sun.security.sam.openid;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.math.BigInteger;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import com.sun.security.sam.Base64Helper;

/**
 *
 * @author monzillo
 */
public class AssociationManager {

    private static ReentrantReadWriteLock master_rwLock =
            new ReentrantReadWriteLock();
    private static Lock master_rLock = master_rwLock.readLock();
    private static Lock master_wLock = master_rwLock.writeLock();
    // map of Consumer (Diffe-Hellman) keys to AssociationManager
    private static Map<String, AssociationManager> managerCache =
            new HashMap<String, AssociationManager>();
    private static ArrayList<String> sessionTypeArray = new ArrayList();
    

    static {
        sessionTypeArray.add((String) "DH-SHA1");
        sessionTypeArray.add((String) null);
    }

    // need to implement a mechanism to remove expired entries.
    private ReentrantReadWriteLock rwLock =
            new ReentrantReadWriteLock();
    private Lock rLock = rwLock.readLock();
    private Lock wLock = rwLock.writeLock();
    final Logger logger;
    final boolean debug;
    private static BigInteger DEFAULT_MODULUS =
            new BigInteger("155172898181473697471232257763715539915724801966915404479707795314057629378541917580651227423698188993727816152646631438561595825688188889951272158842675419950341258706556549803580104870537681476726513255747040765857479291291572334510643245094715007229621094194349783925984760375594985848253359305585439638443");
    private static BigInteger DEFAULT_GENERATOR = new BigInteger("2");
    private static DHParameterSpec defaultDHParameterSpec =
            new DHParameterSpec(DEFAULT_MODULUS, DEFAULT_GENERATOR);

    // map of handle to Association
    private Map<String, Association> handleCache;

    // per session type maps of idp to Association
    private static Map<String, Association>[] idpCache;
    private HostnameVerifier hostnameVerifier;

    // names of openid paramaters in response from provider
    public final static String[] openIDParams = new String[]{
        "openid.mode",
        "openid.user_setup_url",
        "openid.identity",
        "openid.assoc_handle",
        "openid.return_to",
        "openid.signed",
        "openid.sig",
        "openid.invalidate_handle"
    };

    public AssociationManager(HostnameVerifier verifier,
            Logger loggerParam, boolean debug) {

        rwLock = new ReentrantReadWriteLock();
        rLock = rwLock.readLock();
        wLock = rwLock.writeLock();

        handleCache = new HashMap<String, Association>();

        idpCache = new HashMap[sessionTypeArray.size()];
        for (int i = 0; i < sessionTypeArray.size(); i++) {
            idpCache[i] = new HashMap<String, Association>();
        }

        hostnameVerifier = verifier;

        if (loggerParam != null) {
            this.logger = loggerParam;
        } else {
            this.logger = Logger.getLogger(AssociationManager.class.getName());
        }

        this.debug = debug;
    }

    public String makeCheckImmediate(String idP, String sessionType,
            String id, String returnTo,
            String root)
            throws IOException,
            InvalidParameterSpecException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            UnsupportedEncodingException {


        String handle = getAssociationHandle(idP, sessionType);

        if (handle == null) {
            return null;
        }

        StringBuffer idpURL = new StringBuffer(idP);

        idpURL.append("?openid.mode=checkid_immediate");
        idpURL.append("&openid.identity=" + urlEncode(id));
        idpURL.append("&openid.assoc_handle=" + urlEncode(handle));
        idpURL.append("&openid.return_to=" + urlEncode(returnTo));

        if (root != null) {
            idpURL.append("&openid.trust_root=" + urlEncode(root));
        }

        return idpURL.toString();
    }

    public String makeCheckImmediate(Object token, String returnTo,
            String root)
            throws IOException,
            InvalidParameterSpecException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            UnsupportedEncodingException {

        if (!(token instanceof SessionToken)) {
            return null;
        }

        SessionToken sToken = (SessionToken) token;

        String handle = getAssociationHandle(sToken.idP, sToken.sessionType);

        StringBuffer idpURL = new StringBuffer(sToken.idP);

        idpURL.append("?openid.mode=checkid_immediate");
        idpURL.append("&openid.identity=" + sToken.id);
        idpURL.append("&openid.assoc_handle=" + urlEncode(handle));
        idpURL.append("&openid.return_to=" + urlEncode(returnTo));

        if (root != null) {
            idpURL.append("&openid.trust_root=" + urlEncode(root));
        }

        return idpURL.toString();
    }

    public String makeCheckSetup(String idP, String sessionType,
            String id, String returnTo,
            String root)
            throws IOException,
            InvalidParameterSpecException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            UnsupportedEncodingException {

        String handle = getAssociationHandle(idP, sessionType);

        if (handle == null) {
            return null;
        }

        StringBuffer idpURL = new StringBuffer(idP);

        idpURL.append("?openid.mode=checkid_setup");
        idpURL.append("&openid.identity=" + urlEncode(id));
        idpURL.append("&openid.assoc_handle=" + urlEncode(handle));
        idpURL.append("&openid.return_to=" + urlEncode(returnTo));

        if (root != null) {
            idpURL.append("&openid.trust_root=" + urlEncode(root));
        }

        return idpURL.toString();
    }

    static String getTokenName(String token) {
        if (token.startsWith("openid.")) {
            StringTokenizer tokenizer = new StringTokenizer(token, "=");
            int tokenCount = tokenizer.countTokens();
            if (tokenCount > 0) {
                return tokenizer.nextToken();
            }
        }
        return null;
    }

    static String getTokenValue(String token) {
        if (token.startsWith("openid.")) {
            StringTokenizer tokenizer = new StringTokenizer(token, "=");
            for (int i = 0; tokenizer.hasMoreTokens(); i++) {
                String value = tokenizer.nextToken();
                if (i == 1) {
                    return URLDecoder.decode(value);
                }
            }
        }
        return null;
    }
    
    public static Map getToken(String query) {

        HashMap map = null;

        if (query != null) {

            boolean hasMode = false;
            String mode = new String("openid.mode");
            StringTokenizer tokenizer = new StringTokenizer(query, "&");

            while (tokenizer.hasMoreTokens()) {

                String token = tokenizer.nextToken();
                String value = null;

                boolean standardParam = false;

                for (int i = 0; value == null && i < openIDParams.length; i++) {

                    String name = openIDParams[i];

                    if (token.startsWith(name)) {

                        standardParam = true;
                        value = URLDecoder.decode(token.substring(name.length() + 1));

                        if (map == null) {
                            map = new HashMap();
                        }

                        map.put(name, value);

                        if (mode.equals(name)) {
                            hasMode = true;
                        }

                    }
                }

                if (!standardParam) {
                    String tokenName = getTokenName(token);
                    if (tokenName != null) {
                        String tokenValue = getTokenValue(token);
                        if (map == null) {
                            map = new HashMap();
                        }
                        map.put(tokenName, tokenValue);
                    }
                }
            }
            if (hasMode = false) {
                map = null;
            }
        }

        return map;
    }

    public Object verifyToken(Map token)
            throws IOException,
            UnsupportedEncodingException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidKeySpecException {

        Object rvalue = null;

        if (token.containsKey("openid.invalidate_handle")) {
            return checkInvalidHandle(token);
        }

        Association a = null;
        String handle = (String) token.get("openid.assoc_handle");
        if (handle != null) {
            a = (Association) handleCache.get(handle);
        }
        if (a == null) {
            logger.log(Level.WARNING, "openid.assoc_handle_unknown");
            return false;
        }

        ArrayList fields = null;
        String signed = (String) token.get("openid.signed");
        StringTokenizer tokenizer = new StringTokenizer(signed, ",");

        while (tokenizer.hasMoreTokens()) {
            if (fields == null) {
                fields = new ArrayList();
            }
            fields.add(tokenizer.nextToken());
        }

        StringBuffer token_contents = null;

        for (int i = 0; i < fields.size(); i++) {
            String field = (String) fields.get(i);
            if (token_contents == null) {
                token_contents = new StringBuffer();
            }
            token_contents.append(field + ":" +
                    (String) token.get("openid." + field) + "\n");
        }

        if (token_contents != null) {

            String contents = token_contents.toString();
            byte[] bytes = contents.getBytes("UTF8");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(a.getMacKey());
            String signature = new String(Base64Helper.encode(mac.doFinal(bytes)));

            String msgSignature = (String) token.get("openid.sig");

            if (msgSignature != null && signature.equals(msgSignature)) {
                rvalue = createSessionToken(a, token);
            }
        }

        return rvalue;
    }

    public static boolean tokenModeIsResult(Map token) {
        return "id_res".equals((String) token.get("openid.mode"));
    }

    public static boolean tokenModeIsCancel(Map token) {
        return "cancel".equals((String) token.get("openid.mode"));
    }

    public static String getSetupURL(Map token) {
        return (String) token.get("openid.user_setup_url");
    }

    public static String getOpenID(Map token) {
        return (String) token.get("openid.identity");
    }

    Object createSessionToken(Association a, Map token) {
        String id = (String) token.get("openid.identity");
        return new SessionToken(id, a.idp, a.sessionType);
    }

    public Object checkInvalidHandle(Map token) throws IOException {

        String handle = (String) token.get("openid.invalidate_handle");
        if (handle == null) {
            return null;
        }

        Association a = (Association) handleCache.get(handle);
        if (a == null) {
            // see if the provider has an error
            String newHandle = (String) token.get("openid.assoc_handle");
            a = (Association) handleCache.get(newHandle);
            return null;
        }

        Object rvalue = checkAuthentication(a.idp, token);

        if (token.containsKey("openid.invalidate_handle")) {
            a.setExpired();
        }

        return rvalue;
    }

    public Object checkAuthentication(String idp, Map token)
            throws IOException {

        Object rvalue = null;

        URL idpURL = new URL(idp);

        HttpURLConnection connection = null;

        try {

            StringBuffer pBuffer =
                    new StringBuffer("openid.mode=check_association");

            pBuffer.append("&openid.assoc_handle=" +
                    (String) token.get("openid.assoc_handle"));

            pBuffer.append("&openid.sig=" +
                    (String) token.get("openid.sig"));

            String signed = (String) token.get("openid.signed");

            pBuffer.append("&openid.signed=" + signed);

            ArrayList fields = null;
            StringTokenizer tokenizer = new StringTokenizer(signed, ",");

            while (tokenizer.hasMoreTokens()) {
                String field = new String("openid." + tokenizer.nextToken());
                pBuffer.append("&" + field + "=" + (String) token.get(field));
            }

            String iHandle = (String) token.get("openid.invalidate_handle");
            if (iHandle != null) {
                pBuffer.append("&openid.invalidate_handle=" + iHandle);
            }

            String parameters = pBuffer.toString();

            if (debug) {
                logger.log(Level.INFO,
                        "openid.idp_check_url: " + idpURL);
                logger.log(Level.INFO,
                        "openid.idp_check_parameters: " + parameters);
            }

            connection = (HttpURLConnection) idpURL.openConnection();

            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setHostnameVerifier(hostnameVerifier);
            } else {
                String msg = "openid.idp_url_not_secure";
                logger.log(Level.WARNING, msg, idpURL);
                return null;
            }

            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length",
                    Integer.toString(parameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());

            outStream.writeBytes(parameters);
            outStream.flush();
            outStream.close();

            Map responseToken = getIdpResponse(idp, connection);

            if (responseToken != null) {
                String valid = (String) responseToken.get("openid.is_valid");
                if (valid != null && valid.equals("true")) {
                    rvalue = new SessionToken((String) token.get("openid.identity"), idp, "DH-SHA1");
                }

                if (!responseToken.containsKey("openid.invalidate_handle")) {
                    token.remove("openid.invalidate_handle");
                }
            }

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }

        return rvalue;
    }

    private Map getIdpResponse(String idp, HttpURLConnection connection)
            throws IOException {

        if (connection.getResponseCode() != 200) {
            logger.log(Level.FINE, "openid.post_response_code",
                    connection.getResponseCode());
            return null;
        }

        InputStream inStream = connection.getInputStream();

        Map rMap = new HashMap();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));

        String line = reader.readLine();

        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line, ":");
            if (tokenizer.countTokens() == 2) {
                String key = tokenizer.nextToken();
                String value = tokenizer.nextToken();

                if (debug) {
                    logger.log(Level.INFO, "openid.debug.checkAuth_response: " +
                            key + "=" + value);
                }

                rMap.put(key, value);
            }
            line = reader.readLine();
        }

        reader.close();

        return rMap;
    }

    private String getAssociationHandle(String idp,
            String sessionType)
            throws IOException,
            InvalidParameterSpecException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException {

        rLock.lock();
        Association a = null;
        int idpCacheIndex;
        try {
            idpCacheIndex = sessionTypeArray.indexOf(sessionType);
            if (idpCacheIndex < 0) {
                return null;
            }
            a = idpCache[idpCacheIndex].get(idp);
        } finally {
            rLock.unlock();
        }

        if (debug && a != null) {
            logger.log(Level.INFO, "openid.debug.test_expiresAt/now/result: " +
                    a.expiresAt.getTime().toString() + " / " +
                    Calendar.getInstance().getTime().toString() +
                    " / " + a.isExpired());
        }

        if (a == null || a.isExpired()) {
            Association newA = getAssociation(idp, sessionType);
            try {
                wLock.lock();

                // note that the session type may be downgraded
                // by the provider, such that a session of weaker type
                // will be added to the idp cache for a stronger tyoe.

                a = idpCache[idpCacheIndex].get(idp);
                if (a != null && a.isExpired()) {
                    idpCache[idpCacheIndex].remove(idp);
                    handleCache.remove(a.getHandle());
                    a = null;
                }
                if (a == null && newA != null) {
                    if (sessionTypeArray.indexOf(newA.sessionType) ==
                            idpCacheIndex) {
                        idpCache[idpCacheIndex].put(idp, newA);
                        handleCache.put(newA.getHandle(), newA);
                        a = newA;
                    } else {
                        a = null;
                    }
                }
            } finally {
                wLock.unlock();
            }
        }
        return a == null ? null : a.getHandle();
    }

    private Association getAssociation(String idp,
            String sessionType)
            throws IOException,
            InvalidParameterSpecException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException {

        Association rvalue = null;

        URL idpURL = new URL(idp);

        HttpURLConnection connection = null;

        try {

            connection = (HttpURLConnection) idpURL.openConnection();

            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setHostnameVerifier(hostnameVerifier);
            }

            KeyPair kPair = null;

            StringBuffer pBuffer =
                    new StringBuffer("openid.mode=associate");

            pBuffer.append("&openid.assoc_type=HMAC-SHA1");

            if (sessionType != null) {

                pBuffer.append("&openid.session_type=" + sessionType);

                if (sessionType.equals("DH-SHA1")) {

                    // Use default parameters

                    pBuffer.append("&openid.dh_modulus=" +
                            getEncodedModulus());
                    pBuffer.append("&openid.dh_gen=" +
                            getEncodedGenerator());

                    // generate the keypair

                    KeyPairGenerator keyGen =
                            KeyPairGenerator.getInstance("DH");
                    keyGen.initialize(defaultDHParameterSpec);

                    kPair = keyGen.generateKeyPair();

                    pBuffer.append("&openid.dh_consumer_public=" +
                            getEncodedPublicKey((DHPublicKey) kPair.getPublic()));
                }
            }

            String parameters = pBuffer.toString();

            if (debug) {
                logger.log(Level.INFO,
                        "openid.debug.idp_assoc_url: " + idpURL);
                logger.log(Level.INFO,
                        "openid.debug.idp_assoc_parameters: " + parameters);
            }

            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length",
                    Integer.toString(parameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());

            outStream.writeBytes(parameters);
            outStream.flush();
            outStream.close();

            Map token = getIdpResponse(idp, connection);

            if (token != null) {

                rvalue = new Association(idp, kPair,
                        (String) token.get("assoc_type"),
                        (String) token.get("assoc_handle"),
                        (String) token.get("expires_in"),
                        (String) token.get("session_type"),
                        (String) token.get("dh_server_public"),
                        (String) token.get("enc_mac_key"),
                        (String) token.get("mac_key"));

                if (debug) {

                    logger.log(Level.INFO, "openid.debug.Assoc_expires_at: " +
                            rvalue.handle + " : " +
                            rvalue.expiresAt.getTime().toString() +
                            " : NOW : " +
                            Calendar.getInstance().getTime().toString() +
                            " : SECONDS : " +
                            (new Integer((String) token.get("expires_in"))).toString());
                }

            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        if (debug) {
            if (rvalue != null) {
                logger.log(Level.INFO, "openid.debug.association: " +
                        rvalue.toString());
            } else {
                logger.log(Level.INFO, "openid.debug.no_association");
            }
        }

        return rvalue;
    }

    private static String getEncodedModulus()
            throws UnsupportedEncodingException {
        return urlEncode(Base64Helper.encode(defaultDHParameterSpec.getP().toByteArray()));
    }

    private static String getEncodedGenerator()
            throws UnsupportedEncodingException {
        return urlEncode(Base64Helper.encode(defaultDHParameterSpec.getG().toByteArray()));
    }

    private static String getEncodedPublicKey(DHPublicKey key)
            throws UnsupportedEncodingException {
        return urlEncode(Base64Helper.encode(key.getY().toByteArray()));
    }

    private static String urlEncode(String s)
            throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8");
    }

    private static String urlEncode(byte[] b)
            throws UnsupportedEncodingException {
        return urlEncode(new String(b));
    }

    private static class Association {

        private String idp;
        private KeyPair kPair;
        private String type;
        private String handle;
        private boolean expired;
        private Calendar expiresAt;
        private String sessionType;
        private String publicKey;
        private String encryptedSecret;
        private String secret;
        private Key macKey;

        private Association(String idp, KeyPair kPair,
                String aType, String handle,
                String expires, String stype, String pubKey,
                String encSecret, String secret) {

            this.idp = idp;
            this.kPair = kPair;
            this.type = aType;
            this.handle = handle;

            this.expired = false;
            Calendar e = Calendar.getInstance();
            e.add(Calendar.SECOND, (new Integer(expires)).intValue());
            this.expiresAt = e;

            this.sessionType = (stype == null ? stype : (stype.length() == 0 ? null : stype));

            this.publicKey = pubKey;
            this.encryptedSecret = encSecret;
            this.secret = secret;

            this.macKey = null;
        }

        private boolean isExpired() {
            if (!expired) {
                Calendar now = Calendar.getInstance();
                expired = expiresAt.before(now);
            }

            return expired;
        }

        private void setExpired() {
            expired = true;
        }

        private String getHandle() {
            return this.handle;
        }

        private Key getMacKey()
                throws UnsupportedEncodingException,
                NoSuchAlgorithmException,
                InvalidKeySpecException {

            if (macKey == null) {
                if (encryptedSecret != null) {

                    BigInteger y = new BigInteger(Base64Helper.decode(publicKey.getBytes("UTF8")));

                    BigInteger p = defaultDHParameterSpec.getP();

                    KeyFactory factory = KeyFactory.getInstance("DH");

                    DHPublicKey pubKey = (DHPublicKey) factory.generatePublic(new DHPublicKeySpec(y, p, defaultDHParameterSpec.getG()));
                    DHPrivateKey privKey = (DHPrivateKey) kPair.getPrivate();

                    BigInteger xa = privKey.getX();
                    BigInteger yb = pubKey.getY();

                    BigInteger zz = yb.modPow(xa, p);

                    MessageDigest messageDigest =
                            MessageDigest.getInstance("SHA1");

                    byte[] digest = messageDigest.digest(zz.toByteArray());

                    byte[] keyBytes =
                            Base64Helper.decode(encryptedSecret.getBytes("UTF8"));

                    for (int i = 0; i < keyBytes.length; i++) {
                        keyBytes[i] = (byte) (digest[i] ^ keyBytes[i]);
                    }

                    macKey = new SecretKeySpec(keyBytes, "HmacSHA1");

                } else if (secret != null) {
                    macKey = new SecretKeySpec(Base64Helper.decode(secret.getBytes("UTF8")), "HmacSHA1");
                }
            }
            return this.macKey;
        }

        @Override
        public String toString() {

            StringBuffer b = new StringBuffer();

            b.append(" idp: " + idp);
            b.append("\ntype: " + type);
            b.append("\nhandle: " + handle);
            b.append("\nexpiresAt: " + expiresAt);
            b.append("\nsessionType: " + sessionType);
            b.append("\npublicKey: " + publicKey);
            b.append("\nencryptedSecret: " + encryptedSecret);
            b.append("\nsecret: " + secret);
            b.append("\n");

            return b.toString();
        }
    }

    /* Opaque session token added to HttpSession to preserve result of
     * idp and id lookup. Used by Association manager to create a new 
     * checkImmediate response back through browser to idp.
     */
    private static class SessionToken {

        private String idP;
        private String id;
        private String sessionType;

        private SessionToken(String id, String idP, String sessionType) {

            this.id = id;
            this.idP = idP;
            this.sessionType = sessionType;
        }
    }
}
