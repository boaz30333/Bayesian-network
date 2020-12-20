import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represent factor of Bayesian Network variable consider the
 * evidence variable of any query
 * 
 * @author Boaz Sharabi
 *
 */
public class Factor {

	int num;
	ArrayList<String> vars;
	HashMap<String, Double> table;

	public Factor(int num, ArrayList<String> vars, HashMap<String, Double> factor_table) {
		this.num = num;
		this.vars = vars;
		this.table = factor_table;
	}

	/**
	 * 
	 * @param other_factor
	 * @return new Factor after join operation between current factor and other
	 *         factor
	 */
	public Factor join(Factor other_factor) {
		HashMap<String, Double> table_f3 = new HashMap<String, Double>();
		Set<String> vars_f3 = new HashSet<String>();
		vars_f3.addAll(this.vars);
		vars_f3.addAll(other_factor.vars);
		int num_f3 = vars_f3.size();
		ArrayList<String> vars_f3_as_list = new ArrayList<String>();
		vars_f3_as_list.addAll(vars_f3);
		Set<String> commonElements = new HashSet<String>();
		for (String element : this.vars) {
			if (other_factor.vars.contains(element))
				commonElements.add(element);
		}
		boolean match = true;
		for (String record_f1 : this.table.keySet()) {
			for (String record_f2 : other_factor.table.keySet()) {
				match = true;
				for (String element : commonElements) { // check if common var's value are same
					if (record_f1.split(element + "=").length == 1)
						System.out.println("error");
					String value_a = (record_f1.split(element + "=")[1]).split(",")[0];
					String value_b = (record_f2.split(element + "=")[1]).split(",")[0];
					if (!value_a.equals(value_b))
						match = false;
				}
				// if common variables value are same we want to add new entry to the new factor table
				if (match == true) {
					String key = record_f1;
					for (String var : record_f2.split(",")) {
						if (!var.isEmpty() && !key.contains(var)) {
							key += "," + var;
						}
					}
					if (this.table.get(record_f1) == null || other_factor.table.get(record_f2) == null)
						continue; // no need to multiply and enter 0 probability
					double new_val = this.table.get(record_f1) * other_factor.table.get(record_f2);
					table_f3.put(key, new_val);
				}

			}
		}
		return new Factor(num_f3, vars_f3_as_list, table_f3);

	}

	/**
	 * 
	 * @param var_to_eliminate
	 * @param net
	 * @return number of addition operation that calculate in this elimination
	 *         operation
	 */
	public double eliminate(String var_to_eliminate, Network net) {
		HashMap<String, Double> factor_table_after_eliminate = new HashMap<String, Double>();

		if (!this.vars.contains(var_to_eliminate)) {
			return 0;
		}
		if(this.vars.size()==1&&this.vars.contains(var_to_eliminate)) {
			double prob=0;
			this.vars.remove(var_to_eliminate);
			for(String record : this.table.keySet()) {
				prob+= this.table.get(record);
			}
			factor_table_after_eliminate.put("none", prob);
			this.table= factor_table_after_eliminate;
			return net.vars.get(var_to_eliminate).values.size()-1;
		}
		this.vars.remove(var_to_eliminate);
		this.num--;
		List<List<String>> all_combination = Algo.find_value_combination(this.vars, net);
		int count=0;
		for (List<String> combination : all_combination) {
			int count_combinaton = 0;
			double prob = 0;
			String new_record = "";
			for (String var_value : combination) {
				new_record += var_value + ",";
			}
			new_record = new_record.substring(0, new_record.length() - 1); // remove last ','
			for (String record : this.table.keySet()) {
				boolean match = true;
				for (String var_value : combination) {
					if (!record.contains(var_value))
						match = false;
				}
				if (match == true) {
					prob += this.table.get(record);
					count_combinaton++;
				}
			}
			if(count_combinaton>0) count_combinaton--;
			count+=count_combinaton;
//			if(prob>0)
			factor_table_after_eliminate.put(new_record, prob);
		}
		this.table = factor_table_after_eliminate;
		return count;
	}

	/**
	 * 
	 * @param variable
	 * @param evidence
	 * @return factor from variable cpt after we consider the query evidence
	 */
	public static Factor getFactorByEvidence(Var variable, String[] evidence) {
		HashMap<String, Double> factor_table = new HashMap<String, Double>();

		ArrayList<String> vars_take_part = new ArrayList<String>(variable.parents);
		if (vars_take_part.contains("none"))
			vars_take_part.remove("none");
		vars_take_part.add(variable.name);
		Collection<String> given = variable.cpt.table.keySet();
		for (String record : given) {
			boolean b = true;
			String var_name = "";
			String values = "";
			if (!record.equals("none"))
				values += record + ",";
			for (String key : variable.cpt.table.get(record).keySet()) {
				String keyH = values + variable.cpt.owner + "=" + key;
				b = true;
				for (String e : evidence) {
					var_name = e.substring(0, e.indexOf('='));
					if (vars_take_part.contains(var_name)) {
						vars_take_part.remove(var_name);
					}
					if (keyH.contains(var_name)) {
						if (!keyH.contains(e)) {
							b = false;
						}
						// cut the evidence from new record 3 case for its location : start end or
						// middle
						int index = keyH.indexOf(var_name);
						if (keyH.indexOf('=', index) == keyH.lastIndexOf("=")){// TODO
							if(keyH.indexOf('=', index)!=keyH.indexOf('='))
							keyH = keyH.substring(0, index-1);
							else
								keyH = keyH.substring(0, index);
						}
						else if (index == 0)
							keyH = keyH.substring(keyH.indexOf('|', index + var_name.length() + 3) + 2);// TODO
						else if (index > 0) {
							keyH = keyH.substring(0, index)
									+ keyH.substring(keyH.indexOf('|', index + var_name.length() + 3) + 2);// TODo
						}
					}
				}
				if (vars_take_part.size() == 0)
					return null;
				if (b == true) {
					double valH = variable.cpt.table.get(record).get(key);
//					if (valH == 0)
//						continue;// scenario value =0 we may want remove this entry from the table
					factor_table.put(keyH, valH);
				}
			}
		}
		return new Factor(vars_take_part.size(), vars_take_part, factor_table);
	}

	/**
	 * this function normalize the probability value's in factor table when it's
	 * contain only one variable
	 */
	public void normalize() {
		if (this.num > 1) {
			System.out.println("error: more than one var to normalize");
			return;
		}
		double sum_of_prob = 0;
		for (String record : this.table.keySet()) {
			sum_of_prob += this.table.get(record);
		}
		for (String record : this.table.keySet()) {
			this.table.replace(record, this.table.get(record) / sum_of_prob);
		}
	}
}
