RapidMiner InfoSel Extension 
=============================

This is a source code for RapidMiner Extansion called Information Selection, previouselly called ISPR.

This extension implements instance selection algorithms as well as some clustering methods and other extra features.
## Implemented Instance selection algorithms:
   - CNN - Condensed Nearest Neighbor Rule
   - ENN - Edited Nearest Neighbor Rule
   - RENN - Repeated Edited Nearest Neighbor Rule
   - All k-NN
   - RNG - Relative Neighbor Graph
   - GE - Gabriel Editing
   - ELH - Encoding Length Heuristic
   - RMHC – Random Mutation Hill Climbing
   - IB2 - Instance Based Learning v2
   - IB3 - Instance Based Learning v2
   - Drop1 - Decremental Reduction Optimization Procedure v1
   - Drop2 - Decremental Reduction Optimization Procedure v1
   - Drop3 - Decremental Reduction Optimization Procedure v1
   - Drop4 - Decremental Reduction Optimization Procedure v1
   - Drop5 - Decremental Reduction Optimization Procedure v1
   - ICF - Iterative Case Filtering
   - MC - Monte Carlo
   - Random Selection
 Note: The most of these algorithms supports regression problems with additive as well as multiplicative noise
 
### The extension also wraps other instance selection libraries:
#### Instance Selection for Weka by Álvar Arnaiz-González:
   - Weka Drop1-5
   - Weka ICF
   - Weka BSE
   - Weka CNN
   - Weka ENN
   - Weka HMNE
   - Weka HMNEI
   - Weka MC
   - Weka MSS
   - Weka RNN    
#### Instance Selection methods from Keel project:
   - Keel CCIS
    
## Ensembles of  Instance Selection methods:
   - Ensemble Instance Selection by Bagging
   - Ensemble Instance Selection by Voting
   - Ensemble Instance Selection by Attribute Subsets
   - Ensemble Instance Selection by Noise
   - Ensemble Instance Selection by AdaBoost
    
## Generalized Instance Selection (these methods allow any classifier to be used within instance selection)
   - Generalized ENN
   - Generalized CNN
    
## Competitive based Neural Networks:
   - LVQ1
   - LVQ2
   - LVQ2.1
   - LVQ3
   - OLVQ
   - Weighted LVQ
   - SNG – Supervised Neural Gas
   - Winer Takes Most LVQ
   - Generalized LVQ
    
## Clustering algorithms:
   - Fuzzy c-means
   - Vector Quantization
   - Conditional Fuzzy c-Means
    
## Feature set reduction algorithms
   - MDS Multidimensional Scaling  (use external library: Creative Commons License - which is not fully compatible with AGPL)
   - Feature Selection based on Infosel++ Package (requires external c++ library)
    
## Performance metrics for:
   - Instance selection
   - Clustering
    
## Preprocessing methods:
   - VDM based feature transformation from categorical into numerical features
    
## Noise estimation methods:
   - Gamma-Test
   - Delta-Test
   - Local Gamma-Test
   - Local Delta-Test
   - ENN based instance weighting
    
 ## Some other usefull operators
    


# Requirenments

### Prerequisite
* Requires Gradle 2.3+ (get it [here](http://gradle.org/installation) or use the Gradle wrapper shipped with this template)
* RapidMiner 7 or later

### Getting started
1. Clone the extension 
2. Build and install your extension by executing the _installExtension_ Gradle task 
3. Start RapidMiner Studio and check whether your extension has been loaded
