package com.example.base.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

public class ImageTest {

    public static void main(String[] args) throws Exception{
        // 文件对象
        File file = new File("/Users/a58/Desktop/2.png");
        // 文件大小
        long size = file.length() / 1024;
        // 图片对象
        BufferedImage bufferedImage = ImageIO.read(new FileInputStream(file));

        System.out.println(bufferedImage.getWidth());
        System.out.println(bufferedImage.getHeight());

    }
}
