package org.prules.operator.learner.classifiers.data_splitter;

import com.rapidminer.tools.math.similarity.DistanceMeasure;

import java.util.List;
import java.util.Map;

public interface NearestPrototypesSplitter extends DecisionBorderSplitter {
    long getPair(int id1, int id2);

    double[][] getPrototypes();

    double[] getPrototypeLabels();

    List<String> getAttributes();

    Map<Long,PiredTriple> getSelectedPairs();

    double getMinFactor();

    void setMinFactor(double minFactor);

    int getMinSupport();

    void setMinSupport(int minSupport);

    DistanceMeasure getDistance();

    void setDistance(DistanceMeasure distance);
}
