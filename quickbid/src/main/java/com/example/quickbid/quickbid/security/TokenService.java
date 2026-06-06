package com.example.quickbid.quickbid.security;
import java.nio.charset.StandardCharsets; import java.security.*; import java.time.Instant; import java.util.*; import javax.crypto.Mac; import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import com.fasterxml.jackson.databind.ObjectMapper;
@Service public class TokenService {
 private final byte[] secret; private final ObjectMapper json; private final SecureRandom random=new SecureRandom(); private final int accessMinutes;
 public TokenService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.access-token-minutes:15}") int minutes){
  if(secret.length()<32) throw new IllegalArgumentException("APP_JWT_SECRET must contain at least 32 characters"); this.secret=secret.getBytes(StandardCharsets.UTF_8);this.accessMinutes=minutes;this.json=new ObjectMapper();
 }
 public String access(Long id,String email,String estado){try{
  String h=b64(json.writeValueAsBytes(Map.of("alg","HS256","typ","JWT"))); long now=Instant.now().getEpochSecond();
  String p=b64(json.writeValueAsBytes(Map.of("sub",id.toString(),"email",email,"estado",estado,"iat",now,"exp",now+accessMinutes*60L)));
  return h+"."+p+"."+b64(sign(h+"."+p)); }catch(Exception e){throw new IllegalStateException(e);}}
 public Long accountId(String jwt){try{String[] p=jwt.split("\\."); if(p.length!=3||!MessageDigest.isEqual(sign(p[0]+"."+p[1]),Base64.getUrlDecoder().decode(p[2])))throw new IllegalArgumentException();
  Map<?,?> claims=json.readValue(Base64.getUrlDecoder().decode(p[1]),Map.class); if(((Number)claims.get("exp")).longValue()<Instant.now().getEpochSecond())throw new IllegalArgumentException(); return Long.valueOf((String)claims.get("sub"));
 }catch(Exception e){throw new IllegalArgumentException("Token invalido");}}
 public String opaque(){byte[] b=new byte[32];random.nextBytes(b);return b64(b);} public String hash(String token){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 public long accessSeconds(){return accessMinutes*60L;} private byte[] sign(String data)throws Exception{Mac m=Mac.getInstance("HmacSHA256");m.init(new SecretKeySpec(secret,"HmacSHA256"));return m.doFinal(data.getBytes(StandardCharsets.UTF_8));} private String b64(byte[] b){return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
}
