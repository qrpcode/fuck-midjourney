package com.example.midjourney.util;

import com.luciad.imageio.webp.WebPReadParam;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImgUtil {


    @SneakyThrows
    public static void webpToPng(String webpPath, String pngPath) {
        ImageReader reader = ImageIO.getImageReadersByMIMEType("image/webp").next();
        WebPReadParam readParam = new WebPReadParam();
        readParam.setBypassFiltering(true);
        reader.setInput(new FileImageInputStream(new File(webpPath)));
        BufferedImage image = reader.read(0, readParam);
        ImageIO.write(image, "png", new File(pngPath));
    }


}
