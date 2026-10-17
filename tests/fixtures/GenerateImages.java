import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
/** Original solid-color synthetic fixtures; no external image dependency. */
class GenerateImages {
 public static void main(String[] args) throws Exception {
  BufferedImage image=new BufferedImage(96,64,BufferedImage.TYPE_INT_RGB);
  for(int x=0;x<96;x++)for(int y=0;y<64;y++)image.setRGB(x,y,0xb06a30);
  for(String ext:new String[]{"png","jpg"})ImageIO.write(image,ext,new File(args[0],"sample."+ext));
 }
}
