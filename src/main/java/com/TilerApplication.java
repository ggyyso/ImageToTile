package com;

import com.service.TileSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class TilerApplication {

    public static void main(String[] args) throws IOException {
       SpringApplication.run(TilerApplication.class, args);
        //启动一个窗体，将一幅图片按指定的行列数排列输出
       //new ImageTiler();
        //读取默认的影像存储信息，用于服务的访问
        TileSourceConfig tileSourceConfig=new TileSourceConfig();
    }

}
