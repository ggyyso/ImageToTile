package com.service;

import com.mercator.MercatorProjection;
import com.mercator.TileUtils;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.ServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Jason Wong
 * @title: GeoTiffService
 * @projectName tiler
 * @description: TODO
 * @date 2020/7/30 11:14
 */
@Service
public class GeoTiffService {

    public void getTile(int x, int y, int z, ServletResponse response) throws IOException {
        Envelope tileEnv =
                TileUtils.parseXyz2Envelope((int) x, (int) y,
                        z);
        double xmin = MercatorProjection.longitudeToMetersX(tileEnv.getMinX());
        double xmax = MercatorProjection.longitudeToMetersX(tileEnv.getMaxX());
        double ymin = MercatorProjection.latitudeToMetersY(tileEnv.getMinY());
        double ymax = MercatorProjection.latitudeToMetersY(tileEnv.getMaxY());
        CoordinateReferenceSystem targetCRS = TileSourceConfig.tilerModel.getTargetCRS();
        org.opengis.geometry.Envelope envelope = new ReferencedEnvelope(
                xmin, xmax, ymin, ymax, targetCRS);
        GridCoverage2D gridCoverage = TileSourceConfig.tilerModel.getSubImage(envelope);

        //AbstractGridFormat format =TileSourceConfig.tilerModel.getFormat();

//        File tileFile = new File(TileSourceConfig.tilerModel.getOutputDirectory(), x + "_" + y + ".tif" );
//        format.getWriter(tileFile).write(gridCoverage, null);


        OutputStream out = response.getOutputStream();
        ImageIO.write(gridCoverage.getRenderedImage(), "png", out);
//        format.getWriter(out).write(gridCoverage,null);
        out.flush();
        out.close();
    }
}
