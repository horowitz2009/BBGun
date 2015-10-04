package com.horowitz.mickey;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.horowitz.commons.ImageData;
import com.horowitz.mickey.data.Material;
import com.horowitz.mickey.ocr.OCRB;

public class MaterialsScanner {

  private OCRB _ocr;

  public MaterialsScanner() {
    super();
    try {
      _ocr = new OCRB("digit");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Material[] scanMaterials(BufferedImage materialsImage, MaterialLocation[] materials, boolean isHome) {
    List<Material> res = new ArrayList<Material>(17);
    for (int i = 0; i < materials.length; i++) {
      Rectangle area = materials[i].getArea();
      BufferedImage subimage = materialsImage.getSubimage(area.x, area.y, area.width, area.height);
      String resAmount = _ocr.scanImage(subimage);
      String name = materials[i].getHomeName();
      System.out.println(name + ": " + resAmount);
      Integer amount = 0;
      if (resAmount.length() > 0)
        amount = Integer.parseInt(resAmount);
      res.add(new Material(name, amount));
    }
    
    return res.toArray(new Material[0]);
  }
  
  
  public ImageData createMaterialImageData(String name, int xOff, int yOff) throws IOException {
    ImageData id = new ImageData(name+".bmp", new Rectangle(0, 0, 288, 325) , new SimilarityImageComparator(0.04, 1200), xOff, yOff);
    return id;
  }

  public ImageData createMaterialImageData(String name) throws IOException {
    return createMaterialImageData(name, 43, 0);
  }
  
  public static void main(String[] args) {
    try {
      File f = new File("home/data/" + "Home" + "_materials.bmp");
      BufferedImage image = ImageIO.read(f);
      MaterialsScanner scanner = new MaterialsScanner();
      Material[] scanMaterials = scanner.scanMaterials(image, Locations.MATERIALS_1, false);
      System.err.println(scanMaterials);
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
