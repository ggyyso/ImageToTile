package com.tiler;

import org.junit.jupiter.api.Test;

/**
 * @author Jason Wong
 * @title: ImageJFramTest
 * @projectName tiler
 * @description: TODO
 * @date 2020/8/5 17:51
 */
public class ImageJFramTest {
    @Test
    public void buildImageJFram() throws InterruptedException {
        new ImageTiler();
        Thread.sleep(1000000);
    }
}
