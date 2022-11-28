package de.buw.fm4se.java_smt.model;

import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

public class Component {
    public String Name;
    public int Price;
    public BooleanFormula Variable;
    public IntegerFormula Cost;

    public Component(String name, int price, FormulaManager formulaManager) {
        var boolMgr = formulaManager.getBooleanFormulaManager();
        var intMgr = formulaManager.getIntegerFormulaManager();
        
        Name = name;
        Price = price;
        Variable = boolMgr.makeVariable(name);
        Cost = boolMgr.ifThenElse(Variable, intMgr.makeNumber(price), intMgr.makeNumber(0));
    }
}
