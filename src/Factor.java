import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Factor {

	int num;
	ArrayList<String> vars;
	HashMap<String, Double> table;

	public Factor(int num, ArrayList<String> vars, HashMap<String, Double> factor_table) {
		super();
		this.num = num;
		this.vars = vars;
		this.table = factor_table;
	}
//	DecimalFormat df = new DecimalFormat("###.#####"); // TODO maybe not needed to format here

	public  Factor join(Factor b) {
		HashMap<String, Double> table_c = new HashMap<String, Double>();
		Set<String> vars_c = new HashSet<String>();
		vars_c.addAll(this.vars);
		vars_c.addAll(b.vars);
		int num_c = vars_c.size();
		ArrayList<String> vars_c_as_list = new ArrayList<String>();
		vars_c_as_list.addAll(vars_c);
		Set<String> commonElements = new HashSet<String>();
		for (String element : this.vars) {
			if (b.vars.contains(element))
				commonElements.add(element);
		}
		boolean match = true;
		for (String record_a : this.table.keySet()) {
			for (String record_b : b.table.keySet()) {
				match = true;
				for (String element : commonElements) { // check if common var's value are same
					if(record_a.split(element + "=").length==1) 
						System.out.println("error");
					String value_a = (record_a.split(element + "=")[1]).split(",")[0];
					String value_b = (record_b.split(element + "=")[1]).split(",")[0];
//					System.out.println(value_a);
//					System.out.println(value_b);
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
					double new_val = this.table.get(record_a) * b.table.get(record_b);// TODO maybe 					double new_val = Double.parseDouble(df.format(this.table.get(record_a) * b.table.get(record_b)));

					table_c.put(key, new_val);
				}

			}
		}
		return new Factor(num_c, vars_c_as_list, table_c);

	}

	public Factor eliminate(String var_to_eliminate, Network net) {
		HashMap<String, Double> factor_table_after_eliminate = new HashMap<String, Double>();
		
		
		
		if (!this.vars.contains(var_to_eliminate)) {
			System.out.println("no wanted var to eliminate in this factor");
			return this;
		}
		this.vars.remove(var_to_eliminate);
		this.num--;
		List<List<String>> all_combination = Algo.find_value_combination(this.vars, net);
		for(List<String> combination : all_combination) {
			double prob=0;
			
			String new_record="";
			for(String var_value: combination) {
				new_record+= var_value+",";
			}
			new_record=new_record.substring(0, new_record.length()-1); // remove last ','
				for(String record : this.table.keySet()) {
					boolean match=true;
					for(String var_value : combination) {
					if(!record.contains(var_value)) match=false;
				}
					if(match==true) prob+= this.table.get(record);
			}
				factor_table_after_eliminate.put(new_record, prob);
		}
		this.table= factor_table_after_eliminate;
return this;
	}

	public static Factor getFactorByEvidence(Var variable, String[] evidence) {
//		String factor_name="|";                               // TODO factor name has to be variable name include owner and the variable show in the table split by ',' without given variable 

		// ___try_do_hashmap_____________________________________

		HashMap<String, Double> factor_table = new HashMap<String, Double>();

		// _________________________________________________
		ArrayList<String> vars_take_part = new ArrayList<String>(variable.parents);
		if (vars_take_part.contains("none"))
			vars_take_part.remove("none");
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
					b = true;
					for (String e : evidence) {
						var_name = e.substring(0, e.indexOf('='));
						if (vars_take_part.contains(var_name)) {
							vars_take_part.remove(var_name);
						}
//					if(!variable.parents.contains(var_name))
//						vars_take_part.add(var_name);
						if (keyH.contains(var_name)) {
							if (!keyH.contains(e)) { // maybe && variable.parents.contains(var_name)
								b = false;
							}
							int index = keyH.indexOf(var_name);
							int x = 1;
							if (keyH.indexOf('=', index) == keyH.lastIndexOf("="))// TODO
								keyH = keyH.substring(0, index);
							else if (index == 0)
								keyH = keyH.substring(keyH.indexOf('|', index + var_name.length() + 3)+2);// TODO
							else if (index > 0) {
							keyH = keyH.substring(0, index)+ keyH.substring(keyH.indexOf('|', index + var_name.length() + 3) + 2);// TODo
							}
						}
					}
					if(vars_take_part.size()==0) return null;
					if (b == true) {
						double valH = variable.cpt.table.get(record).get(key);
						// TODO scenario val =0 we need remove from the table
						factor_table.put(keyH, valH);
//					values += variable.cpt.owner + "=" + key + "," + this.cpt.table.get(record).get(key) + ",";
					}
				}
//				values = values.substring(0, +values.lastIndexOf(','));
//				entries.add(values);
			}
		}

		return new Factor(vars_take_part.size(), vars_take_part, factor_table);
	}

	public void normalize() {
		// TODO Auto-generated method stub
		if(this.num>1) {
			System.out.println("problem: more than one var to normalize");
			return;
		}
		double sum_of_prob=0;
		for(String record: this.table.keySet()) {
			sum_of_prob+=this.table.get(record);
		}
		for(String record: this.table.keySet()) {
			this.table.replace(record, this.table.get(record)/sum_of_prob);
		}		
	}
}
