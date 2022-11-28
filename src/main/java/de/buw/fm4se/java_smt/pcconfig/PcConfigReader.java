package de.buw.fm4se.java_smt.pcconfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sosy_lab.java_smt.api.FormulaManager;

import de.buw.fm4se.java_smt.model.Category;
import de.buw.fm4se.java_smt.model.Component;
import de.buw.fm4se.java_smt.model.Constraint;
import de.buw.fm4se.java_smt.model.ConstraintKind;

public class PcConfigReader {
	private static final String compoentsFile = "components.csv";
	private static final String constraintsFile = "constraints.csv";
	
	public static Map<String, Integer> getComponents(String category) {
		Map<String, Integer> cmps = new LinkedHashMap<String, Integer>();
		try {
			for (String line : Files.readAllLines(Paths.get(compoentsFile))) {
				String[] ls = line.split(",");
				if (ls.length == 3) {					
					if (ls[0].trim().equalsIgnoreCase(category.trim())) {
						cmps.put(ls[1].trim(), Integer.parseInt(ls[2].trim()));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cmps;
	}

	private static List<Component> getComponents(Category category, FormulaManager formulaManager) {
		List<Component> cmps = new ArrayList<Component>();
		try {
			for (String line : Files.readAllLines(Paths.get(compoentsFile))) {
				String[] ls = line.split(",");
				if (ls.length == 3) {					
					if (ls[0].trim().equalsIgnoreCase(category.toString().trim())) {
						cmps.add(new Component(ls[1].trim(), Integer.parseInt(ls[2].trim()), formulaManager));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cmps;
	}
	
	public static Map<Category, List<Component>> getAllComponents(FormulaManager formulaManager) {
		Map<Category, List<Component>> allComponents = new HashMap<Category, List<Component>>(); 
		for (var category : Category.values()) { allComponents.put(category, getComponents(category, formulaManager));  }
		return allComponents;
	}

	public static List<String[]> getConstraints(String kind) {
		List<String[]> pairs = new ArrayList<>();
		try {
			for (String line : Files.readAllLines(Paths.get(constraintsFile))) {
				String[] ls = line.split(",");
				if (ls.length == 3) {					
					if (ls[0].trim().equalsIgnoreCase(kind)) {
						pairs.add(new String[] {ls[1].trim(), ls[2].trim()});
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pairs;
	}

	private static List<Constraint> getConstraints(ConstraintKind kind, FormulaManager formulaManager) {
		List<Constraint> pairs = new ArrayList<>();
		try {
			for (String line : Files.readAllLines(Paths.get(constraintsFile))) {
				String[] ls = line.split(",");
				if (ls.length == 3) {					
					if (ls[0].trim().equalsIgnoreCase(kind.toString())) {
						pairs.add(new Constraint(ls[1].trim(), ls[2].trim(), kind, formulaManager));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pairs;
	}

	public static List<Constraint> getAllConstraints(FormulaManager formulaManager) {
		return new ArrayList<Constraint>() {{
			addAll(getConstraints(ConstraintKind.requires, formulaManager));
			addAll(getConstraints(ConstraintKind.excludes, formulaManager));
		}};
	}
}
