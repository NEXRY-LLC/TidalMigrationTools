package com.dataconverter.csv.model.notused;

import com.dataconverter.csv.model.CvsAbstractDependency;
import com.dataconverter.csv.model.CvsVariable;

public class VariableDependency extends CvsAbstractDependency {

	private CvsVariable variable;

	public CvsVariable getVariable() {
		return variable;
	}

	public void setVariable(CvsVariable variable) {
		this.variable = variable;
	}


	
}
