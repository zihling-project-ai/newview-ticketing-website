package com.newview.recovery;
import java.io.*;import java.util.*;import com.google.zxing.*;import com.google.zxing.common.*;import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;import com.google.zxing.client.j2se.*;
/** Select a standard QR mask that also passes an ordinary image-detector round trip. */
public final class LocalTicketQR {
 public static byte[] png(String payload)throws IOException{
  for(int mask=-1;mask<8;mask++){
   Map<EncodeHintType,Object> hints=new EnumMap<>(EncodeHintType.class);hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.H);hints.put(EncodeHintType.MARGIN,4);if(mask>=0)hints.put(EncodeHintType.QR_MASK_PATTERN,mask);
   try{BitMatrix matrix=new MultiFormatWriter().encode(payload,BarcodeFormat.QR_CODE,400,400,hints);
    String decoded=new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(MatrixToImageWriter.toBufferedImage(matrix)))),Map.of(DecodeHintType.TRY_HARDER,true)).getText();if(!payload.equals(decoded))continue;
    ByteArrayOutputStream bytes=new ByteArrayOutputStream();MatrixToImageWriter.writeToStream(matrix,"PNG",bytes);return bytes.toByteArray();
   }catch(WriterException|ReaderException e){/* Try another standard mask; never bypass QR detection. */}
  }throw new IOException("Cannot produce a locally readable ticket QR");
 }
 private LocalTicketQR(){}
}
