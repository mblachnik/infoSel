/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.operator.ports.metadata;

import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.CompatibilityLevel;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.Precondition;

/**
 *
 * @author Marcin
 */
public class IsConnectedPrecondition implements Precondition{
    private InputPort port;
    Precondition precondition;
    
    public IsConnectedPrecondition(InputPort port, Precondition precondition){
        this.port = port;
        this.precondition = precondition;
    }
    
    @Override
    public void check(MetaData metaData) {
        if (port.isConnected()){
            precondition.check(metaData);            
        }
    }

    @Override
    public String getDescription() {
        return precondition.getDescription();
    }

    @Override
    public boolean isCompatible(MetaData input, CompatibilityLevel level) {
        if (port.isConnected()){
            precondition.isCompatible(input, level);
        }
        return true;
    }

    @Override
    public void assumeSatisfied() {
        if (port.isConnected()){
            precondition.assumeSatisfied();
        }
    }

    @Override
    public MetaData getExpectedMetaData() {
        if (port.isConnected()){
            return precondition.getExpectedMetaData();
        }
        return new ExampleSetMetaData();
    }
    
}
