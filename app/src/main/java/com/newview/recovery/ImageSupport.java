package com.newview.recovery;

import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;

/** Raster-only image boundary for the local recovery build. */
public final class ImageSupport {
    public static final int MAX_BYTES = 2 * 1024 * 1024;
    public static byte[] normalize(byte[] bytes) throws IOException {
        if (bytes.length == 0 || bytes.length > MAX_BYTES) throw new IllegalArgumentException("每張圖片限 2 MB");
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            // Select only the two accepted formats, avoiding unrelated ImageIO plugins left by hot deploys.
            String requested=bytes.length>=8&&bytes[0]==(byte)0x89&&bytes[1]==0x50&&bytes[2]==0x4e&&bytes[3]==0x47?"png":bytes.length>=3&&bytes[0]==(byte)0xff&&bytes[1]==(byte)0xd8&&bytes[2]==(byte)0xff?"jpeg":null;
            if(requested==null)throw new IllegalArgumentException("只接受有效的 PNG 或 JPEG 圖片");
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(requested);
            if (!readers.hasNext()) throw new IllegalArgumentException("只接受有效的 PNG 或 JPEG 圖片");
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!Set.of("png", "jpeg", "jpg").contains(format)) throw new IllegalArgumentException("只接受 PNG 或 JPEG");
                reader.setInput(input, true, true);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > 6000 || height > 6000 || (long) width * height > 16000000)
                    throw new IllegalArgumentException("圖片尺寸過大");
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                if (!ImageIO.write(reader.read(0), "png", output)) throw new IOException("Cannot encode image");
                if (output.size() > 4 * 1024 * 1024) throw new IllegalArgumentException("轉換後圖片過大");
                return output.toByteArray();
            } finally { reader.dispose(); }
        } catch (IOException ex) { throw new IllegalArgumentException("圖片無法解碼", ex); }
    }
    public static String dataUrl(byte[] bytes) {
        String mime = bytes.length >= 8 && bytes[0] == (byte)0x89 && bytes[1] == 0x50 ? "image/png" : "image/jpeg";
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }
}
