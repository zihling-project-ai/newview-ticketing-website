package com.newview.recovery;
import javax.crypto.SecretKeyFactory; import javax.crypto.spec.PBEKeySpec;
import java.security.*; import java.util.Base64;
public final class Passwords {
 public static String hash(String password) {byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);return encode(password,salt,210000);}
 private static String encode(String p,byte[] salt,int iterations) {try {byte[] hash=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(p.toCharArray(),salt,iterations,256)).getEncoded();return "pbkdf2$"+iterations+"$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(hash);} catch(Exception e){throw new IllegalStateException(e);}}
 public static boolean verify(String p,String encoded) {try {String[] a=encoded.split("\\$");return MessageDigest.isEqual(encode(p,Base64.getDecoder().decode(a[2]),Integer.parseInt(a[1])).getBytes(java.nio.charset.StandardCharsets.UTF_8),encoded.getBytes(java.nio.charset.StandardCharsets.UTF_8));}catch(Exception e){return false;}}
 public static void main(String[] args){System.out.println(hash(args[0]));}
}