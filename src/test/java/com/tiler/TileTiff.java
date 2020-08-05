package com.tiler;

import org.geotools.util.Arguments;

import java.io.File;
import java.io.IOException;

/**
 * @author Jason Wong
 * @title: 将一张影像切分已大小固定的瓦片，存储到文件夹
 * @projectName tiler
 * @description: TODO
 * @date 2020/7/29 18:47
 */
public class TileTiff {
    public static void main(String[] args) {
        // GeoTools provides utility classes to parse command line arguments
        Arguments processedArgs = new Arguments(args);
        ImageTilerModel tiler = new ImageTilerModel();

        try {
//            tiler.setInputFile(new File(processedArgs.getRequiredString("-f")));
//            tiler.setOutputDirectory(new File(processedArgs.getRequiredString("-o")));
//            tiler.setNumberOfHorizontalTiles(processedArgs.getOptionalInteger("-htc"));
//            tiler.setNumberOfVerticalTiles(processedArgs.getOptionalInteger("-vtc"));
//            tiler.setTileScale(processedArgs.getOptionalDouble("-scale"));
            tiler.setInputFile(new File("C:\\Users\\Administrator\\Pictures\\地图产品\\NE1_50M_SR_W\\NE1_50M_SR_W\\NE1_50M_SR_W.tif"));
            tiler.setOutputDirectory(new File("C:\\Users\\Administrator\\Pictures\\地图产品\\NE1_50M_SR_W"));
            tiler.setNumberOfHorizontalTiles(10);
            tiler.setNumberOfVerticalTiles(5);
            tiler.setTileScale(2.0);

            tiler.tile();
        } catch (IllegalArgumentException | IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

}
