import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Factor {

	int num;
	Set<String> vars;
	HashMap<String, Double> table;

	public Factor(int num, Set<String> vars, HashMap<String, Double> factor_table) {
		super();
		this.num = num;
		this.vars = vars;
		this.table = factor_table;
	}

	public static Factor multply(Factor a, Factor b) {
		HashMap<String, Double> table_c = new HashMap<String, Double>();
		Set<String> vars_c = new HashSet<String>();
		vars_c.addAll(a.vars);
		vars_c.addAll(b.vars);
		int num_c= vars_c.size();
		Set<String> commonElements = new HashSet<String>();
		for (String element : a.vars) {
			if (b.vars.contains(element))
				commonElements.add(element);
		}
		boolean match = true;
		for (String record_a : a.table.keySet()) {
			for (String record_b : b.table.keySet()) {
				match = true;
				for (String element : commonElements) { // check if common var's value are same
					String value_a = (record_a.split(element + "=")[1]).split(record_a, ',')[0];
					String value_b = (record_b.split(element + "=")[1]).split(record_b, ',')[0];
					if (!value_a.equals(value_b))
						match = false;
				}
				if (match == true) {
					String key = record_a; // if coomon vars value are same we want to add new entry to the new factor
											// table
					for (String var : record_b.split(",")) {
						if (!var.isEmpty() && !key.contains(var)) {
							key += "," + var;
						}
					}
					double new_val = a.table.get(record_a)*b.table.get(record_b);
					table_c.put(key, new_val);
				}

			}
		}
		return new Factor(num_c, vars_c, table_c);

	}


	public static Factor getFactorByEvidence(Var variable ,String[] evidence) {
//		String factor_name="|";                               // TODO factor name has to be variable name include owner and the variable show in the table split by ',' without given variable 

		// ___try_do_hashmap_____________________________________

		HashMap<String, Double> factor_table = new HashMap<String, Double>();

		// _________________________________________________
		Set<String> vars_take_part = new HashSet<String>(variable.parents);
		if(vars_take_part.contains("none")) vars_take_part.remove("none");
		vars_take_part.add(variable.name);
		Collection<String> given = variable.cpt.table.keySet();
//		Vector<String> entries = new Vector<String>();
		for (String record : given) {
			boolean b = true;
			String var_name = "";

			if (b == true) {
				String values = "";
				if (!record.equals("none"))
					values += record + ",";

//				if(record.equals("none")) {
//					
//				}

				for (String key : variable.cpt.table.get(record).keySet()) {
					String keyH = values + variable.cpt.owner + "=" + key;
					b=true;
					for (String e : evidence) {
					var_name = e.substring(0, e.indexOf('='));
					if (vars_take_part.contains(var_name)) {
						vars_take_part.remove(var_name);
					}
//					if(!variable.parents.contains(var_name))
//						vars_take_part.add(var_name);
					if (!keyH.contains(e)&&keyH.contains(var_name))  // maybe && variable.parents.contains(var_name)
						b = false;
				}
					if(b==true) {
					double valH = variable.cpt.table.get(record).get(key);
					//TODO scenario val =0  we need remove from the table
					factor_table.put(keyH, valH);
//					values += variable.cpt.owner + "=" + key + "," + this.cpt.table.get(record).get(key) + ",";
				}}
//				values = values.substring(0, +values.lastIndexOf(','));
//				entries.add(values);
			}
		}

		return new Factor(vars_take_part.size(), vars_take_part, factor_table);
	}
}
