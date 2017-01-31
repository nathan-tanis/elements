/*
Copyright 2015 Futeh Kao

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package net.e6tech.elements.security;

import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;

/**
 * Created by futeh.
 */
public class JCEKS {
    static {
        SymmetricCipher.initialize();
    }

    KeyStore keyStore;
    KeyManager[] keyManagers = null;
    TrustManager[] trustManagersWithSystem;
    TrustManager[] trustManagers;
    boolean includeSystem = true;

    public static SecretKey generateSecretKey(String keyType, int keySize) throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(keyType, "BC");
        keyGenerator.init(keySize, RNG.getSecureRandom());
        return keyGenerator.generateKey();
    }

    public static KeyPair generateKeyPair(String keyType, int keySize) throws GeneralSecurityException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(keyType, "BC");
        keyPairGen.initialize(keySize, RNG.getSecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        return keyPair;
    }

    // CN=www.companyname.com,OU=IT,O=<Company Name>,L=Austin,ST=Texas,C=US,E=user@companyname.com
    public static X509Certificate generateSelfSignedCertificate(String info, KeyPair pair, int years){
        try {
            X500Principal principal=new X500Principal(info);
            Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
            Date notAfter = new Date(System.currentTimeMillis() + years * 365 * 24 * 60 * 60 * 1000L);
            BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
            X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                    principal, serial,
                    notBefore, notAfter,
                    principal,
                    pair.getPublic());
            ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider("BC").build(pair.getPrivate());
            X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certGen.build(sigGen));
            cert.checkValidity(new Date());
            cert.verify(cert.getPublicKey());
            return cert;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed to generate self-signed certificate!",t);
        }
    }

    public JCEKS() throws GeneralSecurityException {
        keyStore = createKeyStore();
    }

    public JCEKS(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public JCEKS(String file, char[] password) throws GeneralSecurityException, IOException {
        if (file != null) {
            keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(new FileInputStream(file), password);
        }
    }

    public JCEKS(InputStream inputStream, char[] password) throws GeneralSecurityException, IOException {
        if (inputStream != null) {
            keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(inputStream, password);
        }
    }

    public void init(char[] password) throws GeneralSecurityException {
        if (password != null) {
            KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            factory.init(keyStore, password);
            keyManagers = factory.getKeyManagers();
        }

        initTrustManagers();
    }

    public boolean isIncludeSystem() {
        return includeSystem;
    }

    public void setIncludeSystem(boolean includeSystem) {
        this.includeSystem = includeSystem;
    }

    public KeyManager[] getKeyManagers() {
        return keyManagers;
    }

    public TrustManager[] getTrustManagers() {
        return trustManagers;
    }

    protected TrustManager[] initTrustManagers() throws GeneralSecurityException {
        if (trustManagers == null) {
            if (keyStore != null) {
                TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init(keyStore);
                trustManagers = factory.getTrustManagers();
            }
            if (trustManagers == null) trustManagers = new TrustManager[0];
        }

        if (includeSystem) {
            if (trustManagersWithSystem == null) {
                TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init((KeyStore)null);
                TrustManager[] system = factory.getTrustManagers();
                trustManagersWithSystem = new TrustManager[system.length + trustManagers.length];
                System.arraycopy(trustManagers, 0, trustManagersWithSystem, 0, trustManagers.length);
                System.arraycopy(system, 0, trustManagersWithSystem, trustManagers.length, system.length);
            }
            return trustManagersWithSystem;
        }
        return trustManagers;
    }

    public Key getKey(String alias, char[] password) throws GeneralSecurityException {
        KeyStore.Entry entry = keyStore.getEntry(alias, new KeyStore.PasswordProtection(password));
        return keyStore.getKey(alias, password);
    }

    public void setKey(String alias, SecretKey secretKey, char[] password) throws GeneralSecurityException {
        KeyStore.ProtectionParameter protectionParameter;
        protectionParameter = new KeyStore.PasswordProtection(password);
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
        keyStore.setEntry(alias, secretKeyEntry, protectionParameter);
    }

    public X509Certificate getCertificate(String alias) throws GeneralSecurityException {
        return (X509Certificate) keyStore.getCertificate(alias);
    }

    public void setCertificate(String name, X509Certificate certificate) throws GeneralSecurityException{
        keyStore.setCertificateEntry(name, certificate);
    }

    public void remove(String name) throws GeneralSecurityException {
        keyStore.deleteEntry(name);
    }

    public boolean isEntry(String name) throws GeneralSecurityException {
        return keyStore.isCertificateEntry(name) || keyStore.isKeyEntry(name);
    }

    public static KeyStore createKeyStore() throws GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        try {
            keyStore.load(null, null);
        } catch (IOException e) {
        }
        return keyStore;
    }

    public void createSelfSignedCertificate(String alias, String info, char[] password, int years) throws GeneralSecurityException {
        KeyPair pair = generateKeyPair("RSA", 2048);
        X509Certificate cert = generateSelfSignedCertificate(info, pair, years);
        keyStore.setKeyEntry(alias, pair.getPrivate(), password, new java.security.cert.Certificate[]{cert});
    }

    public void save(File keyStoreFile, char[] password) throws IOException, GeneralSecurityException {
        FileOutputStream fos = new FileOutputStream(keyStoreFile);
        try {
            keyStore.store(fos, password);
        } finally {
            fos.close();
        }
    }

    public void save(OutputStream output, char[] password) throws GeneralSecurityException, IOException {
        try {
            keyStore.store(output, password);
        } finally {
            output.close();
        }
    }

    public static void main(String ... args) throws Exception {
        char[] password = "password".toCharArray();
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(password);

        KeyPair pair = generateKeyPair("RSA", 2048);
        X509Certificate cert = generateSelfSignedCertificate("CN=www.nowhere.com,OU=IT,O=No Where,L=Austin,ST=Texas,C=US", pair, 10);
        KeyStore keyStore = createKeyStore();
        keyStore.setKeyEntry("alias", pair.getPrivate(), password, new java.security.cert.Certificate[]{cert});
        Key privKey = keyStore.getKey("alias", password);

        String hashed = Password.getSaltedHash("password".toCharArray());
        X509Certificate cert2 = generateSelfSignedCertificate("CN=futeh kao,UID=" + hashed, pair, 2);
        keyStore.setCertificateEntry("cert2", cert2);
        cert2 = (X509Certificate) keyStore.getCertificate("cert2");
        String dn = cert2.getIssuerDN().getName();
        String[] names = dn.split(",");
        Properties props = new Properties();
        for (String n : names) {
            String[] keyvalue = n.trim().split("=");
            props.put(keyvalue[0], keyvalue[1]);
        }
        hashed = props.getProperty("UID");
        boolean check = Password.check("password".toCharArray(), hashed);

        JCEKS jceks = new JCEKS(keyStore);
        jceks.init(password);
        KeyManager[] keyManagers = jceks.getKeyManagers();
        TrustManager[] trustManagers = jceks.getTrustManagers();

        SecretKey secretKey = generateSecretKey("AES", 256);
        SecretKey key2 = generateSecretKey("AES", 256);
        SymmetricCipher encryption = SymmetricCipher.getInstance("AES");
        byte[] bytes = encryption.encryptBytes(secretKey, key2.getEncoded(), null);
        SecretKey encryptedKey = new SecretKeySpec(bytes, "AES");
        jceks.setKey("encryptedKey", encryptedKey, password);
        SecretKey encryptedKey2 = (SecretKey) jceks.getKey("encryptedKey", password);

        bytes = encryption.decryptBytes(secretKey, encryptedKey2.getEncoded(), null);
        SecretKey decryptedKey = new SecretKeySpec(bytes, "AES");
        System.out.println(decryptedKey.equals(key2));

        SecretKeySpec junk = new SecretKeySpec(new byte[2567], "JUNK");
        jceks.setKey("junk", junk, password);
        junk = (SecretKeySpec) jceks.getKey("junk", password);
    }


}