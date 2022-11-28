package de.buw.fm4se.java_smt.model;

import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FormulaManager;

public class Constraint {
    public String Left;
    public String Right;
    public ConstraintKind Kind;
    public BooleanFormula ConstraintFormula;
  
    public Constraint(String left, String right, ConstraintKind kind, FormulaManager formulaManager) {
        var boolMgr = formulaManager.getBooleanFormulaManager();

        Left = left;
        Right = right;
        Kind = kind;
        ConstraintFormula = boolMgr.implication(
                                boolMgr.makeVariable(Left),
                                Kind == ConstraintKind.requires ? boolMgr.makeVariable(right) : boolMgr.not(boolMgr.makeVariable(right)));                            
    }
}
