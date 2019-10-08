/*
 * Marcin Blachnik
 *
 * Copyright (C) 2016-2016 by Marcin Blachnik and the contributors
 *
 * Complete list of developers available at our web site:
 *
 * www.prules.org
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 */
package org.prules;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import com.rapidminer.tools.math.similarity.divergences.SquaredEuclideanDistance;
import org.prules.tools.math.similarity.numerical.NormalizedManhattanDistance;
import org.prules.tools.math.similarity.numerical.RBFKernel;


/**
 * This class provides hooks for initialization and its methods are called via reflection by
 * RapidMiner Studio. Without this class and its predefined methods, an extension will not be
 * loaded.
 *
 * @author REPLACEME
 */
public final class PluginInitISPR {

    private PluginInitISPR() {
        // Utility class constructor
    }

    /**
     * This method will be called directly after the extension is initialized. This is the first
     * hook during start up. No initialization of the operators or renderers has taken place when
     * this is called.
     */
    public static void initPlugin() {
        DistanceMeasures.registerMeasure(DistanceMeasures.NUMERICAL_MEASURES_TYPE, "Normalized Manhattan Distance", NormalizedManhattanDistance.class);
        DistanceMeasures.registerMeasure(DistanceMeasures.NUMERICAL_MEASURES_TYPE, "Square Euclidian Distance", SquaredEuclideanDistance.class);
        DistanceMeasures.registerMeasure(DistanceMeasures.NUMERICAL_MEASURES_TYPE, "RBF Kernel", RBFKernel.class);
    }

    /**
     * This method is called during start up as the second hook. It is called before the gui of the
     * mainframe is created. The Mainframe is given to adapt the gui. The operators and renderers
     * have been registered in the meanwhile.
     *
     * @param mainframe the RapidMiner Studio {@link MainFrame}.
     */
    public static void initGui(MainFrame mainframe) {
    }

    /**
     * The last hook before the splash screen is closed. Third in the row.
     */
    public static void initFinalChecks() {
    }

    /**
     * Will be called as fourth method, directly before the UpdateManager is used for checking
     * updates. Location for exchanging the UpdateManager. The name of this method unfortunately is
     * a result of a historical typo, so it's a little bit misleading.
     */
    public static void initPluginManager() {
    }
}
