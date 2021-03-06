package com.tiler;


import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.factory.Hints;

import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Jason Wong
 * @title: ImageTilerModel
 * @projectName tiler
 * @description: TODO
 * @date 2020/7/29 18:47
 */
public class ImageTilerModel {
    private final int NUM_HORIZONTAL_TILES = 16;
    private final int NUM_VERTICAL_TILES = 8;

    private Integer numberOfHorizontalTiles = NUM_HORIZONTAL_TILES;
    private Integer numberOfVerticalTiles = NUM_VERTICAL_TILES;
    private Double tileScale;
    private File inputFile;
    private File outputDirectory;
    private  GridCoverage2D gridCoverage;
    private  CoordinateReferenceSystem targetCRS;
    private AbstractGridFormat format;

    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public Integer getNumberOfHorizontalTiles() {
        return numberOfHorizontalTiles;
    }

    public void setNumberOfHorizontalTiles(Integer numberOfHorizontalTiles) {
        this.numberOfHorizontalTiles = numberOfHorizontalTiles;
    }

    public Integer getNumberOfVerticalTiles() {
        return numberOfVerticalTiles;
    }

    public void setNumberOfVerticalTiles(Integer numberOfVerticalTiles) {
        this.numberOfVerticalTiles = numberOfVerticalTiles;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Double getTileScale() {
        return tileScale;
    }

    public void setTileScale(Double tileScale) {
        this.tileScale = tileScale;
    }

    public CoordinateReferenceSystem getTargetCRS() {
        return targetCRS;
    }

    public void setTargetCRS(CoordinateReferenceSystem targetCRS) {
        this.targetCRS = targetCRS;
    }

    public AbstractGridFormat getFormat() {
        return format;
    }

    public void setFormat(AbstractGridFormat format) {
        this.format = format;
    }

    public void  initImage() throws IOException {
        AbstractGridFormat _format = GridFormatFinder.findFormat(inputFile);
        String fileExtension = this.getFileExtension(this.getInputFile());
        format=_format;
        // working around a bug/quirk in geotiff loading via format.getReader which doesn't set this
        // correctly
        Hints hints = null;
        if (format instanceof GeoTiffFormat) {
            hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        }

        GridCoverage2DReader gridReader = format.getReader(this.getInputFile(), hints);
        gridCoverage = gridReader.read(null);
        targetCRS=gridCoverage.getCoordinateReferenceSystem2D();
    }

    public GridCoverage2D getSubImage(Envelope envelope){
        GridCoverage2D finalCoverage = cropCoverage(gridCoverage, envelope);
        finalCoverage = scaleCoverage(finalCoverage);
       return finalCoverage;
    }

    public void tile() throws IOException {
        AbstractGridFormat format = GridFormatFinder.findFormat(this.getInputFile());
        String fileExtension = this.getFileExtension(this.getInputFile());

        // working around a bug/quirk in geotiff loading via format.getReader which doesn't set this
        // correctly
        Hints hints = null;
        if (format instanceof GeoTiffFormat) {
            hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        }

        GridCoverage2DReader gridReader = format.getReader(this.getInputFile(), hints);
        GridCoverage2D gridCoverage = gridReader.read(null);
        Envelope2D coverageEnvelope = gridCoverage.getEnvelope2D();
        double coverageMinX = coverageEnvelope.getBounds().getMinX();
        double coverageMaxX = coverageEnvelope.getBounds().getMaxX();
        double coverageMinY = coverageEnvelope.getBounds().getMinY();
        double coverageMaxY = coverageEnvelope.getBounds().getMaxY();

        int htc =
                this.getNumberOfHorizontalTiles() != null
                        ? this.getNumberOfHorizontalTiles()
                        : NUM_HORIZONTAL_TILES;
        int vtc =
                this.getNumberOfVerticalTiles() != null
                        ? this.getNumberOfVerticalTiles()
                        : NUM_VERTICAL_TILES;

        double geographicTileWidth = (coverageMaxX - coverageMinX) / (double) htc;
        double geographicTileHeight = (coverageMaxY - coverageMinY) / (double) vtc;

        CoordinateReferenceSystem targetCRS = gridCoverage.getCoordinateReferenceSystem();

        // make sure to create our output directory if it doesn't already exist
        File tileDirectory = this.getOutputDirectory();
        if (!tileDirectory.exists()) {
            tileDirectory.mkdirs();
        }

        // iterate over our tile counts
        for (int i = 0; i < htc; i++) {
            for (int j = 0; j < vtc; j++) {

                System.out.println("Processing tile at indices i: " + i + " and j: " + j);
                // create the envelope of the tile
                Envelope envelope =
                        getTileEnvelope(
                                coverageMinX,
                                coverageMinY,
                                geographicTileWidth,
                                geographicTileHeight,
                                targetCRS,
                                i,
                                j);

                GridCoverage2D finalCoverage = cropCoverage(gridCoverage, envelope);

                if (this.getTileScale() != null) {
                    finalCoverage = scaleCoverage(finalCoverage);
                }

                // use the AbstractGridFormat's writer to write out the tile
                File tileFile = new File(tileDirectory, i + "_" + j + "." + fileExtension);
                format.getWriter(tileFile).write(finalCoverage, null);
            }
        }
    }


    private Envelope getTileEnvelope(
            double coverageMinX,
            double coverageMinY,
            double geographicTileWidth,
            double geographicTileHeight,
            CoordinateReferenceSystem targetCRS,
            int horizontalIndex,
            int verticalIndex) {

        double envelopeStartX = (horizontalIndex * geographicTileWidth) + coverageMinX;
        double envelopeEndX = envelopeStartX + geographicTileWidth;
        double envelopeStartY = (verticalIndex * geographicTileHeight) + coverageMinY;
        double envelopeEndY = envelopeStartY + geographicTileHeight;

       Envelope envelope=new ReferencedEnvelope(
                envelopeStartX, envelopeEndX, envelopeStartY, envelopeEndY,targetCRS);
       return envelope;
    }

    private GridCoverage2D cropCoverage(GridCoverage2D gridCoverage, Envelope envelope) {
        CoverageProcessor processor = CoverageProcessor.getInstance();

        // An example of manually creating the operation and parameters we want
        final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
        param.parameter("Source").setValue(gridCoverage);
        param.parameter("Envelope").setValue(envelope);

        return (GridCoverage2D) processor.doOperation(param);
    }

    /**
     * Scale the coverage based on the set tileScale
     *
     * <p>As an alternative to using parameters to do the operations, we can use the Operations
     * class to do them in a slightly more type safe way.
     *
     * @param coverage the coverage to scale
     * @return the scaled coverage
     */
    private GridCoverage2D scaleCoverage(GridCoverage2D coverage) {
        Operations ops = new Operations(null);
        float scaleX = 256/ (float)coverage.getRenderedImage().getWidth();
        float scaleY = 256 / (float)coverage.getRenderedImage().getHeight();
        coverage =
                (GridCoverage2D)
                        ops.scale(coverage, scaleX, scaleY, 0, 0);
        return coverage;
    }

}
