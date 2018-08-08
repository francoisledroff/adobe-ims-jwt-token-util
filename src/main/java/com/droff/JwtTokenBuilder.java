package com.droff;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JwtTokenBuilder {

  private Map<String, Object> claims = new HashMap<String, Object>();
  private PrivateKey key;
  private Properties prop;
  OkHttpClient okHttpClient = new OkHttpClient();

  private static final String ISS = "iss";
  private static final String EXP = "exp";
  private static final String SUB = "sub";
  private static final String AUD = "aud";
  private static final String IAT = "iat";

  private static final String AUD_SUFFIX = "/c/";

  private static final String IMS_URL = "imsUrl";
  private static final String META_SCOPES = "metascopes";
  private static final String ORG_ID = "organizationId";
  private static final String TECH_ID = "technicalAccountId";
  private static final String API_KEY = "apiKey";
  private static final String CLIENT_SECRET = "clientSecret";


  private static final String KEYSTORE_TYPE = "PKCS12";

  private static final String P12_FILE_PATH = "p12FilePath";
  private static final String P12_PASSWORD = "p12Password";
  private static final String P12_ALIAS = "p12Alias";

  private static final String DEFAULT_PROPERTIES = "ims-secret.pipeline.properties";

  public static void main(String[] args) {

    try {
      JwtTokenBuilder jwtTokenBuilder = new JwtTokenBuilder();
      // TODO use jCommander to parse the args and allow the retrieval of the access token as an option.
      if (args != null && args.length > 0) {
        jwtTokenBuilder.loadProperties(args[0]);
      } else {
        jwtTokenBuilder.loadProperties(null);
      }

      //TODO validate properties are not null
      String jwtToken = jwtTokenBuilder.getJwtToken();
      System.out.println("jwtToken: "+jwtToken);
      String accessToken = jwtTokenBuilder.getAccessToken(jwtToken).getAccessToken();
      System.out.println("accessToken: "+accessToken);
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private void loadProperties(String filePath)
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {


    if (filePath == null) {
      prop = getProperties(this.getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES));
    } else {
      prop = getProperties(new FileInputStream(filePath));
    }

    claims.put(ISS, prop.getProperty(ORG_ID));
    claims.put(SUB, prop.getProperty(TECH_ID));
    claims.put(AUD, prop.getProperty(IMS_URL) + AUD_SUFFIX + prop.getProperty(API_KEY));

    String[] metascopes = prop.get(META_SCOPES).toString().split(",");
    for (String metascope : metascopes) {
      claims.put(prop.getProperty(IMS_URL) + metascope, true);
    }

    long iat = System.currentTimeMillis() / 1000L;

    claims.put(IAT, iat);
    claims.put(EXP, iat + 180L);

    KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
    String password = prop.getProperty(P12_PASSWORD);
    File p12File = new File(prop.getProperty(P12_FILE_PATH));

    keystore.load(new FileInputStream(p12File), password.toCharArray());
    this.key = (PrivateKey) keystore.getKey(prop.getProperty(P12_ALIAS), password.toCharArray());
  }

  private Properties getProperties(InputStream in) throws IOException {
    Properties prop = new Properties();
    prop.load(in);
    in.close();
    return prop;
  }

  public String getJwtToken() {
    String jwt = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.RS256, key).compact();
    return jwt;
  }

  public AccessToken getAccessToken(String jwtToken) throws IOException {
    Retrofit retrofit = getRetrofit(prop.getProperty(IMS_URL));
    ImsRetrofit imsRetrofit = retrofit.create(ImsRetrofit.class);
    Call<AccessToken> accessTokenCall = imsRetrofit.getAccessToken(prop.getProperty(API_KEY),prop.getProperty(CLIENT_SECRET),jwtToken);

    AccessToken accessToken;
    Response applicationResponse = accessTokenCall.execute();
    if (applicationResponse.isSuccessful()) {
      accessToken = (AccessToken) applicationResponse.body();
    }
    else {throw new RuntimeException("ims response code: "+applicationResponse.code()+
    ", body: "+applicationResponse.body());}
    return accessToken;
  }

  private Retrofit getRetrofit(String url) {
    Retrofit.Builder builder = new Retrofit.Builder();
    builder.baseUrl(url);
    builder.addConverterFactory(GsonConverterFactory.create());
    builder.client(okHttpClient);
    return builder.build();
  }
}