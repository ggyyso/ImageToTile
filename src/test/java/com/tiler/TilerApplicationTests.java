package com.tiler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 读取tiff文件信息，并按指定的大小进行缩放输出
 */
@SpringBootTest
class TilerApplicationTests {

    @Test
    void contextLoads() {
        try {
            String path="C:\\Users\\Administrator\\Downloads\\5f1b3c70f4d9a200052a1d39 - 副本.tif";

            zoomImage(path,"C:\\Users\\Administrator\\Downloads" +
            "\\5de7f5832290870005e599941.tif",10000,10000);
            RandomAccessFile file = new RandomAccessFile(path,"r");
            //读取tif图片
            ImageReader reader = ImageIO.getImageReadersByFormatName("tiff").next();
            FileImageInputStream inputStream = new FileImageInputStream(file);
            reader.setInput(inputStream);
            BufferedImage originalImage= reader.read(0);

            originalImage.getRGB(0, 0);
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            int minX = originalImage.getMinX();
            int minY = originalImage.getMinY();
            //将结果画出来
            ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();
            //定义一个BufferedImage对象，用于保存缩小后的位图
            BufferedImage bufferedImage=new BufferedImage(256,256,BufferedImage.TYPE_INT_RGB);
            Graphics graphics= bufferedImage.getGraphics();
            //将原始位图缩小后绘制到bufferedImage对象中
            graphics.drawImage(originalImage,0,0,256,256,null);
            writer.setOutput(new FileImageOutputStream(new File("C:\\Users\\Administrator\\Downloads" +
                    "\\5de7f5832290870005e599941.tif")));
            writer.write(bufferedImage);
            writer.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 图片缩放,w，h为缩放的目标宽度和高度
     * src为源文件目录，dest为缩放后保存目录
     */
    public static void zoomImage(String src,String dest,int w,int h) throws Exception {

        double wr=0,hr=0;

        File destFile = new File(dest);
        ImageReader reader = ImageIO.getImageReadersByFormatName("tiff").next();
        RandomAccessFile file = new RandomAccessFile(src,"r");
        FileImageInputStream inputStream = new FileImageInputStream(file);
        reader.setInput(inputStream);
        BufferedImage bufImg= reader.read(0);
        //读取图片
        //Image Itemp = bufImg.getScaledInstance(w, h, bufImg.SCALE_SMOOTH);//设置缩放目标图片模板
        BufferedImage subImg= bufImg.getSubimage(2000,3000,500,500);
        wr=w*1.0/bufImg.getWidth();     //获取缩放比例
        hr=h*1.0 / bufImg.getHeight();

        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
        Image Itemp = ato.filter(bufImg, null);
        try {
            ImageIO.write((BufferedImage) Itemp,dest.substring(dest.lastIndexOf(".")+1), destFile); //写入缩减后的图片
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
