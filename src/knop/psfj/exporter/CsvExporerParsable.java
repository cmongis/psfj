/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knop.psfj.exporter;

import knop.psfj.BeadImage;
import knop.psfj.BeadImageManager;
import knop.psfj.FovDataSet;
import knop.psfj.PSFj;
import knop.psfj.graphics.AsymmetryHeatMap;
import knop.psfj.graphics.DistanceHeatMap;
import knop.psfj.graphics.FullHeatMap;
import knop.psfj.graphics.PsfJGraph;
import knop.psfj.graphics.ThetaHeatMap;
import knop.psfj.resolution.Microscope;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author cyril
 */
public class CsvExporerParsable extends CsvExporter {

    public CsvExporerParsable(BeadImageManager m) {
        super(m);
    }

    /**
     * Adds the bead image infos.
     *
     * @param dataSet the data set
     * @param imageId the image id
     */
    public void addBeadImageInfos(FovDataSet dataSet, int imageId) {

        BeadImage image = manager.getBeadImage(imageId);
        Microscope mic = manager.getMicroscope(0);

        dataSet.setMetaDataValue("", "");
        dataSet.addMetaDataSpace();
        dataSet.setMetaDataValue(image.getImageName(), "");
        dataSet.setMetaDataValue("Emission wavelength",Microscope.UNIT_KEY,
                mic.getWaveLength());
        dataSet.setMetaDataValue("NA", mic.getNAAsString());
        dataSet.setMetaDataValue("Refraction index",
                mic.getRefractionIndexAsString());
        dataSet.setMetaDataValue("Voxel size", mic.DEFAULT_UNIT, mic.getPixelDepth());
        dataSet.setMetaDataValue("Bead diameter", mic.DEFAULT_UNIT, mic.getBeadDiameter());
        dataSet.addMetaDataSpace();
        dataSet.setMetaDataValue("Frame size", "pixels", image.getFrameSize());
        dataSet.setMetaDataValue("Threshold", image.getThresholdValue());
        dataSet.addMetaDataSpace();
        dataSet.setMetaDataValue("Theoretical FWHM in XY", mic.DEFAULT_UNIT, image.getMicroscope()
                .getTheoreticalResolution(PSFj.X_AXIS));
        dataSet.setMetaDataValue("Theoratical FWHM in Z", mic.DEFAULT_UNIT, image.getMicroscope()
                .getTheoreticalResolution(PSFj.Z_AXIS));

    }

    /**
     * Adds the heat map infos.
     *
     * @param dataSet the data set
     */
    public void addHeatMapInfos(FovDataSet dataSet) {
        dataSet.addMetaDataSpace();
        dataSet.setMetaDataValue("HeatMap Statistics", "(in "+Microscope.DEFAULT_UNIT+")");

        for (PsfJGraph graph : manager.getGraphList()) {
            if (FullHeatMap.class.isAssignableFrom(graph.getClass())) {

                FullHeatMap heatmap = (FullHeatMap) graph;
                String unit = heatmap instanceof ThetaHeatMap || heatmap instanceof AsymmetryHeatMap ? null : Microscope.DEFAULT_UNIT;
                String suffix = (manager.getAnalysisType() == BeadImageManager.DUAL_CHANNEL && heatmap instanceof DistanceHeatMap == false)
                        ? " (" + heatmap.getBeadImage().getImageName() + ")"
                        : "";

                DescriptiveStatistics heatmapStatistics = manager
                        .getHeatmapStatistics(heatmap,
                                PSFj.NOT_NORMALIZED);
                
                dataSet.setMetaDataValue(
                        heatmap.getHeatmapName(PSFj.NOT_NORMALIZED) + suffix,unit,heatmapStatistics.getMean());
                dataSet.setMetaDataValue(heatmap.getHeatmapName(PSFj.NOT_NORMALIZED) + suffix + " (Std. Dev)",unit, heatmapStatistics.getStandardDeviation());
                
                if (heatmap instanceof ThetaHeatMap == false
                        && heatmap instanceof AsymmetryHeatMap == false) {
                    dataSet.setMetaDataValue(heatmap.getHeatmapName(PSFj.NORMALIZED)
                            + suffix, manager.getHeatmapStatistics(heatmap,
                                    PSFj.NORMALIZED).getMean());
                }

                if (graph instanceof ThetaHeatMap) {
                    dataSet.addMetaDataSpace();
                }
            }
        }

    }

}
