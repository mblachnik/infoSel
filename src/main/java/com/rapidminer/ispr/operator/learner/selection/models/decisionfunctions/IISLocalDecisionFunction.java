/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions;

/**
 * IISLocalDecisionFunction extends IISDecisionFunction, and is used to identify all
 * decision functions which use local noise estimation
 * @author Marcin
 */
public interface IISLocalDecisionFunction extends IISDecisionFunction{
    void setK(int k);
    int getK();
}
