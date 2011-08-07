package temporal;

import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.classifiers.trees.*;
import weka.classifiers.rules.*;
import weka.classifiers.functions.*;
import weka.classifiers.bayes.*;
import weka.core.Capabilities.Capability;
import weka.core.*;
import java.util.*;

public class MyEnsembleModel extends AbstractClassifier {

  static final long serialVersionUID = 8934314652175299374L;

  private Classifier model;

  private boolean useEnsembleSelection = false;

  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    result.enable(Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.DATE_ATTRIBUTES);
    result.enable(Capability.MISSING_VALUES);
    result.enable(Capability.NOMINAL_CLASS);
    result.enable(Capability.NUMERIC_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);
    return result;
  }

  public void buildClassifier(Instances data) throws Exception {
	if (data.classAttribute().isNumeric()) buildRegressionEnsemble(data);
        else buildClassificationEnsemble(data);
  }

  public void buildRegressionEnsemble(Instances data) throws Exception {
      // AdditiveRegression with REPTree
      Classifier c01 = new REPTree();
      ((REPTree)c01).setNumFolds(50); 
      Classifier c02 = new AdditiveRegression(c01);
      ((AdditiveRegression)c02).setShrinkage(0.25); 
      // AdditiveRegression with M5P regression tree
      Classifier c03 = new M5P();
      Classifier c04 = new AdditiveRegression(c03);
      ((AdditiveRegression)c04).setShrinkage(0.3); 
      // SVM regression model
      Classifier c05 = new SMOreg();
      ((SMOreg)c05).setC(10);
      // AdditiveRegression with M5 rules
      Classifier c06 = new M5Rules();
      ((M5Rules)c06).setBuildRegressionTree(true);
      SingleClassifierEnhancer c07 = new AdditiveRegression();
      ((AdditiveRegression)c07).setShrinkage(0.25); 
      c07.setClassifier(c06);
      // Multilayer Perceptron with AdditiveRegression
      Classifier c09 = new MultilayerPerceptron();
      ((MultilayerPerceptron)c09).setTrainingTime(1000);
      ((MultilayerPerceptron)c09).setReset(true);
      ((MultilayerPerceptron)c09).setDecay(false);
      ((MultilayerPerceptron)c09).setAutoBuild(true);
      ((MultilayerPerceptron)c09).setNormalizeNumericClass(true);
      ((MultilayerPerceptron)c09).setNormalizeAttributes(true);
      ((MultilayerPerceptron)c09).setLearningRate(0.1);
      ((MultilayerPerceptron)c09).setMomentum(0.1);
      SingleClassifierEnhancer c10 = new AdditiveRegression();
      ((AdditiveRegression)c10).setShrinkage(0.25); 
      c10.setClassifier(c09);
      // Multilayer Perceptron with Rotation Forest (best model)
      Classifier c11 = new MultilayerPerceptron();
      ((MultilayerPerceptron)c11).setTrainingTime(1000);
      ((MultilayerPerceptron)c11).setReset(true);
      ((MultilayerPerceptron)c11).setDecay(false);
      ((MultilayerPerceptron)c11).setAutoBuild(true);
      ((MultilayerPerceptron)c11).setNormalizeNumericClass(true);
      ((MultilayerPerceptron)c11).setNormalizeAttributes(true);
      ((MultilayerPerceptron)c11).setLearningRate(0.1);
      ((MultilayerPerceptron)c11).setMomentum(0.1);
      IteratedSingleClassifierEnhancer c12 = new RotationForest();
      ((IteratedSingleClassifierEnhancer)c12).setNumIterations(100);
      c12.setClassifier(c11);
      // REPTree with Rotation Forest
      Classifier c13 = new REPTree();
      ((REPTree)c13).setNumFolds(50); 
      IteratedSingleClassifierEnhancer c14 = new RotationForest();
      ((IteratedSingleClassifierEnhancer)c14).setNumIterations(200);
      c14.setClassifier(c13);
      // Build the ensemble
      EnsembleSelection emodel = new EnsembleSelection();
      emodel.setVerboseOutput(false);
      emodel.setHillclimbIterations(1000);
      emodel.setAlgorithm(new SelectedTag(EnsembleSelection.ALGORITHM_FORWARD_BACKWARD, EnsembleSelection.TAGS_ALGORITHM));
      emodel.setGreedySortInitialization(true);
      emodel.setHillclimbMetric(new SelectedTag(1, EnsembleSelection.TAGS_METRIC));
      emodel.setNumFolds(25);
      emodel.setNumModelBags(10);
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c02)); 
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c04));
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c05));
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c07));
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c10));
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c12));
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c14));
      if ( useEnsembleSelection ) model = emodel; else model = c12;
      model.buildClassifier(data);
  }

  public void buildClassificationEnsemble(Instances data) throws Exception {
      // Random Forest
      Classifier c01 = new RandomForest();
      ((RandomForest)c01).setNumTrees(500); 
      // Rotation Forest with REPTree
      Classifier c02 = new REPTree();
      ((REPTree)c02).setNumFolds(10); 
      IteratedSingleClassifierEnhancer c03 = new RotationForest();
      ((IteratedSingleClassifierEnhancer)c03).setNumIterations(200);
      c03.setClassifier(c02);
      // SVM classifier
      Classifier c04 = new SMO();
      ((SMO)c04).setC(1);
      // Rotation Forest with JRip rules
      Classifier c05 = new JRip();
      ((JRip)c05).setOptimizations(5);
      IteratedSingleClassifierEnhancer c06 = new RotationForest();
      ((IteratedSingleClassifierEnhancer)c06).setNumIterations(150);
      c06.setClassifier(c05);
      // Bagging + Boosting with RandomTree (best model)
      Classifier c07 = new RandomTree();
      ((RandomTree)c07).setMinNum(10);
      ((RandomTree)c07).setKValue(10);
      SingleClassifierEnhancer c08 = new AdaBoostM1();
      ((AdaBoostM1)c08).setWeightThreshold(100);
      ((AdaBoostM1)c08).setNumIterations(100);
      c08.setClassifier(c07);
      SingleClassifierEnhancer c09 = new Bagging();
      c09.setClassifier(c08);
      // Voting with trees, rules and SVMs
      Classifier c10 = new J48();
      Classifier c11 = new JRip();
      ((JRip)c11).setOptimizations(5);
      Classifier c12 = new SMO();
      ((SMO)c12).setC(1);
      Classifier c13 = new REPTree();
      ((REPTree)c13).setNumFolds(10); 
      MultipleClassifiersCombiner c14 = new Vote();
      c14.setClassifiers(new Classifier[]{c10,c11,c12,c13});
      EnsembleSelection emodel = new EnsembleSelection();
      emodel.setVerboseOutput(false);
      emodel.setHillclimbIterations(1000);
      emodel.setAlgorithm(new SelectedTag(EnsembleSelection.ALGORITHM_FORWARD_BACKWARD, EnsembleSelection.TAGS_ALGORITHM));
      emodel.setGreedySortInitialization(true);
      emodel.setHillclimbMetric(new SelectedTag(1, EnsembleSelection.TAGS_METRIC));
      emodel.setNumFolds(25);
      emodel.setNumModelBags(10);
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c01)); 
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c03));
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c04));
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c06));
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c09));
      emodel.getLibrary().addModel(emodel.getLibrary().createModel(c14));
      if ( useEnsembleSelection ) model = emodel; else model = c09;
      model.buildClassifier(data);
  }

  public double[] distributionForInstance(Instance instance) throws Exception {
	return model.distributionForInstance(instance);
  }

  public static void main(String[] argv) { runClassifier(new MyEnsembleModel(), argv); }

}
