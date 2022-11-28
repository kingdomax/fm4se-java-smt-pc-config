package de.buw.fm4se.java_smt.pcconfig;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

import de.buw.fm4se.java_smt.model.Category;
import de.buw.fm4se.java_smt.model.Component;
import de.buw.fm4se.java_smt.model.Constraint;

public class PcConfigGeneratorAndSolver {
	public static void main(String[] args) throws Exception {
		// Init SMT solver related stuff
		SolverContext context = SolverContextFactory.createSolverContext(
									Configuration.fromCmdLineArguments(args),
									BasicLogManager.create(Configuration.fromCmdLineArguments(args)),
									ShutdownManager.create().getNotifier(),
									Solvers.PRINCESS);
		FormulaManager formulaManager = context.getFormulaManager();
		IntegerFormulaManager intMgr = formulaManager.getIntegerFormulaManager();
		BooleanFormulaManager boolMgr = formulaManager.getBooleanFormulaManager();

		// User's input
		int budget = getUserBudget();

		// Retreive config from files
		Map<Category, List<Component>> componentConfigs = PcConfigReader.getAllComponents(formulaManager);
		List<Constraint> constraintConfigs = PcConfigReader.getAllConstraints(formulaManager);

		// Build 3 boolean formulas
		// - Every valid PC needs at least component from each of these categories: CPU, motherboard, RAM, and storage
		// - Constraints between components of kind requires and excludes
		// - Within user's budget
		List<BooleanFormula> possibleComponents = new ArrayList<BooleanFormula>();
		List<IntegerFormula> costings = new ArrayList<IntegerFormula>();
		for (var category : Category.values()) {
			var componentVariables = componentConfigs.get(category).stream().map(c -> c.Variable).collect(Collectors.toList());
			var pickOneOfThem = boolMgr.or(componentVariables);
			possibleComponents.add(pickOneOfThem);

			var componentCosts = componentConfigs.get(category).stream().map(c -> c.Cost).collect(Collectors.toList());
			costings.addAll(componentCosts);
		}
		BooleanFormula integrityConstrain = boolMgr.and(possibleComponents);
		BooleanFormula budgetConstrain = intMgr.lessOrEquals(intMgr.sum(costings), intMgr.makeNumber(budget));
		BooleanFormula additionalConstraints = boolMgr.and(constraintConfigs.stream().map(c -> c.ConstraintFormula).collect(Collectors.toList()));

		// Solve
		try (ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS)) {
			prover.addConstraint(integrityConstrain);
			prover.addConstraint(budgetConstrain);
			prover.addConstraint(additionalConstraints);
			printResult(prover, componentConfigs);
		}
	}

	private static int getUserBudget() {
		Scanner scan = new Scanner(System.in);
		System.out.print("Please enter a budget: ");
		int budget = scan.nextInt();
		scan.close();
		return budget;
	}

	private static void printResult(ProverEnvironment prover, Map<Category, List<Component>> componentConfigs) throws Exception {		
		System.out.println("Searching for a configuration....");
		
		if (prover.isUnsat()) {
			System.out.println("=> No configuration is founded");
			return;
		}
		
		List<Component> allComponentsInfo = componentConfigs.values().stream().flatMap(List::stream).collect(Collectors.toList());
		prover.getModel().forEach(m -> {
			if ((Boolean) m.getValue()) { 
				var name = m.getName();
				var price = allComponentsInfo.stream().filter(c -> c.Name.equals(name)).findFirst().get().Price;
				System.out.println("=> " + m.getName() + " (" + price + ")");
			}
		});
	}
}
