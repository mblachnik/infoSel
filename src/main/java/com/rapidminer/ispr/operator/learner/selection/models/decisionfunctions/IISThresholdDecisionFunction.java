/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions;

/**
 * IISThresholdDecisionFunction extends IISDecisionFunction, and is used to identify all
 * decision functions which require a threshold
 * @author Marcin
 */
public interface IISThresholdDecisionFunction extends IISDecisionFunction{
    void setThreshold(double threshold);
}
