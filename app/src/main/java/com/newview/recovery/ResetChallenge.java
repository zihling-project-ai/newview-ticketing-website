package com.newview.recovery;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
/** State remains server-side in one browser session; caller serializes access. */
public final class ResetChallenge {
 public final int userID;
 public final String passwordStamp,email;
 private final String code;
 private final long expiresAt;
 private int remaining=5;
 private boolean consumed;
 public ResetChallenge(int id,String stamp,String email,String code,long now){userID=id;passwordStamp=stamp;this.email=email;this.code=code;expiresAt=now+600000L;}
 public boolean verify(String supplied,long now){
  if(consumed||now>=expiresAt||remaining<=0)return false;
  remaining--;
  return supplied!=null&&MessageDigest.isEqual(code.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8));
 }
 public void consume(){consumed=true;}
}
