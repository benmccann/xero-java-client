package com.connectifier.xeroclient.oauth;

import java.io.IOException;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;
import org.scribe.services.RSASha1SignatureService;
import org.scribe.services.SignatureService;

public class XeroOAuthService extends DefaultApi10a {

  private static final String BASE_URL = "https://api.xero.com/oauth/";

  private final RSASha1SignatureService signatureService;

  public XeroOAuthService(Reader reader) {
    Security.addProvider(new BouncyCastleProvider());
    try (PEMParser pemParser = new PEMParser(reader)) {
      PEMKeyPair pair = (PEMKeyPair) pemParser.readObject();
      byte[] encodedPrivateKey = pair.getPrivateKeyInfo().getEncoded();
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
      PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
      signatureService = new RSASha1SignatureService(privateKey);
    } catch(IOException e) {
      throw new IllegalStateException(e);
    } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public String getAccessTokenEndpoint() {
    return BASE_URL + "AccessToken";
  }

  @Override
  public String getRequestTokenEndpoint() {
    return BASE_URL + "RequestToken";
  }

  @Override
  public String getAuthorizationUrl(Token token) {
    return BASE_URL + "Authorize?oauth_token=" + token.getToken();
  }

  @Override
  public SignatureService getSignatureService() {
    return signatureService; 
  }

}
