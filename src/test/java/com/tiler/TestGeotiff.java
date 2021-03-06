package com.tiler;

import net.minidev.json.JSONObject;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.factory.Hints;
import org.junit.jupiter.api.Test;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import javax.media.jai.PlanarImage;
import java.awt.geom.Point2D;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jason Wong
 * @title: 读取tiff影像，计算每个像素的坡度信息，并进行输出
 * @projectName tiler
 * @description: TODO
 * @date 2020/7/29 17:46
 */
public class TestGeotiff {

    @Test
    public void testCalcSlope() throws NoSuchAuthorityCodeException, FactoryException, IOException {
        String path = "C:\\Users\\Administrator\\Pictures\\地图产品\\tif1_modified.tif";
        String outputPath = "D:\\workData\\geotiff\\output.tif";
        File file = new File(path);
        // 设置tiff影像默认设置
        Hints tiffHints = new Hints();
        tiffHints.add(new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
        // 默认坐标系EPSG:3857
//      tiffHints.add(new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, CRS.decode("EPSG:4326")));
        tiffHints.add(new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, DefaultGeographicCRS.WGS84));

        GeoTiffReader reader = new GeoTiffReader(file, tiffHints);

        GridCoverage2D coverage = reader.read(null);
        Envelope env = coverage.getEnvelope();
        PlanarImage image = (PlanarImage) coverage.getRenderedImage();
        //获取图斑名称
        String[] names = reader.getGridCoverageNames();
        //获取影像长宽
        int width = image.getWidth(); // Image Width
        int height = image.getHeight(); // Image Height
        int tileX = image.getMaxTileX();
        int tileY = image.getMaxTileY();
        int xx = image.tileXToX(100);
        int yy = image.XToTileX(100);
        int irasternumbands = image.getNumBands();

        GridCoverageFactory factory = new GridCoverageFactory();
        GridCoverage2D outputCoverage = factory.create("test", image, env);

        GeoTiffWriter writer = new GeoTiffWriter(new File(outputPath));
        writer.write(outputCoverage, null);
        writer.dispose();
    }

    @Test
    public String getAltitude(List<String> strPoints) throws IOException, TransformException {

        String demPath = "C:\\Users\\Administrator\\Pictures\\地图产品\\tif1_modified.tif";
        // String demPath = "F:/Data/yanta/YanTaDOM.tif";
        File file = new File(demPath);
        Hints tiffHints = new Hints();
        tiffHints.add(new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
        // 默认坐标系EPSG:3857
        //tiffHints.add(new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, CRS.decode("EPSG:4326")));
        tiffHints.add(new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, DefaultGeographicCRS.WGS84));

        GeoTiffReader tifReader = new GeoTiffReader(file, tiffHints);
        GridCoverage2D coverage = tifReader.read(null);
        RenderedImage sourceImage = coverage.getRenderedImage();

        PlanarImage planarImage = (PlanarImage) sourceImage;

        //获取左上右下，包含仿射影子的左上角
        Envelope env = coverage.getEnvelope();

        //创建输出tif
        String outputPath = "D:/testTiff.tif";
        float[][] slopeData = new float[1000][1000];
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++)
                slopeData[i][j] = i + j;
        }
        GridCoverageFactory factory = new GridCoverageFactory();
        GridCoverage2D outputCoverage = factory.create("test", slopeData, env);
        GeoTiffWriter writer = new GeoTiffWriter(new File(outputPath));
        writer.write(outputCoverage, null);
        writer.dispose();

        int ixtiles = sourceImage.getNumXTiles();
        Raster raster = sourceImage.getTile(0, 0);

        int itilerasterwidth = raster.getWidth();
        int itilerasterheight = raster.getHeight();
        //波段数
        int irasternumbands = raster.getNumBands();

        //获取坐标系
        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();

        //获取图斑名称
        String[] names = tifReader.getGridCoverageNames();

        //获取影像长宽
        int iwidth = coverage.getRenderedImage().getWidth();
        int iheight = coverage.getRenderedImage().getHeight();

        //获取仿射因子其他参数
        int a = coverage.getGridGeometry().gridDimensionX;
        int b = coverage.getGridGeometry().gridDimensionY;
        int c = coverage.getGridGeometry().axisDimensionX;
        int d = coverage.getGridGeometry().axisDimensionY;

        //获取栅格图斑个数
        int ibandcount = coverage.getNumSampleDimensions();
        String[] sampleDimensionNames = new String[ibandcount];
        for (int i = 0; i < ibandcount; i++) {
            GridSampleDimension dim = coverage.getSampleDimension(i);
            sampleDimensionNames[i] = dim.getDescription().toString();
        }

        //获取行列对应的像元值
        Raster sourceRaster = sourceImage.getData();
        float[] adsaf = {0};
        sourceRaster.getPixel(1500, 800, adsaf);
        float ibandvalue = sourceRaster.getSampleFloat(0, 0, 0);

        //获取源数据类型
        int iDataType = coverage.getRenderedImage().getSampleModel().getDataType();

        //??栅格转矢量
        // PolygonExtractionProcess process = new PolygonExtractionProcess();
        // SimpleFeatureCollection features = process.execute(tiffCoverage, 0, Boolean.TRUE, null, null, null, null);

        List list = new ArrayList();
        strPoints=new ArrayList<>();
        strPoints.add("108,34");
        for (int i = 0; i < strPoints.size(); i++) {
            String strLonlat = strPoints.get(i);
            String[] strLonlats = strLonlat.split(",");

            double lon = Double.parseDouble(strLonlats[0]),
                    lat = Double.parseDouble(strLonlats[1]);

            //构建地理坐标
            DirectPosition position = new DirectPosition2D(crs, lon, lat);
            float[] results = (float[]) coverage.evaluate(position);

            //通过地理坐标获取行列号
            Point2D point2d = coverage.getGridGeometry().worldToGrid(position);

            //通过行列号获取地理坐标
            GridCoordinates2D coord = new GridCoordinates2D(0, 0);
            DirectPosition tmpPos = coverage.getGridGeometry().gridToWorld(coord);
            float[] sss = (float[]) coverage.evaluate(tmpPos);

            Map map = new HashMap();
            map.put("lon", lon);
            map.put("lat", lon);
            map.put("dem", results[0]);
            list.add(JSONObject.toJSONString(map));
        }
        return list.toString();
    }
}

