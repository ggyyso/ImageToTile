package com.service;

import com.tiler.ImageTilerModel;

import java.io.File;
import java.io.IOException;

/**
 * @author Jason Wong
 * @title: TileSourceConfig
 * @projectName tiler
 * @description: TODO
 * @date 2020/7/30 11:44
 */
public class TileSourceConfig {
    public static ImageTilerModel tilerModel;

    //程序启动读取影像存储信息
    public TileSourceConfig() throws IOException {
        ImageTilerModel tiler = new ImageTilerModel();
        tiler.setInputFile(new File("C:\\Users\\Administrator\\Pictures\\地图产品\\aa.tif"));
        tiler.setOutputDirectory(new File("C:\\Users\\Administrator\\Pictures\\地图产品\\NE1_50M_SR_W"));
        tiler.setNumberOfHorizontalTiles(10);
        tiler.setNumberOfVerticalTiles(5);
        tiler.setTileScale(2.0);
        tiler.initImage();
        tilerModel = tiler;

    }
}
