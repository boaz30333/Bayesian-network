import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Algo {
	public static DecimalFormat df = new DecimalFormat("###.#####"); // TODO maybe not needed to format here

	public static String run(Query q, Network net) {
		switch (q.algo) {
		case (1): {
			return Algo.standard(q, net);
		}
		case (2): {
			return Algo.variable_elimitaion(q, net);
		}
		case (3): {
			return Algo.huristic_variable_elimitaion(q, net);
		}
		default:
			break;
		}
		return null;

	}

	private static String huristic_variable_elimitaion(Query q, Network net) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String variable_elimitaion(Query q, Network net) {
		// TODO Auto-generated method stub

		Set<Factor> factors = new HashSet<>();
		for (Var variable : net.vars.values()) {
			factors.add(variable.getFactorByEvidence(q.evidence));
		}

		return null;
	}

	private static String standard(Query q, Network net) {

		String wanted_var = q.wanted.substring(0, q.wanted.indexOf("="));
		boolean no_parents = false;
		Set<String> parents = net.vars.get(wanted_var).parents;
		int count_parent = 0;
		for (String evidence : q.evidence) {
			if (evidence.equals("none")) {
				no_parents = true;
				break;
			}
			if (parents.contains(evidence.substring(0, evidence.indexOf("="))))
				count_parent++;
		}
		if (no_parents == true || count_parent == parents.size()) {
			double result = net.vars.get(wanted_var).getProb(Arrays.asList(q.evidence),
					q.wanted.substring(q.wanted.indexOf("=") + 1));
			System.out.println(df.format(result) + "," + 0 + "," + 0);
			return df.format(result) + "," + 0 + "," + 0;
		}
		Collection<String> network_vars_names = net.vars.keySet(); // names of all vars
		List<String> hidden_vars_names = new LinkedList<String>();
		for (String var_name : network_vars_names) { // FOR loop to find the hidden variable on the query
			boolean b = true;
			if (q.wanted.contains(var_name + "=")) {
				wanted_var = var_name;
				b = false;
				continue;
			}
			for (String e : q.evidence) {
				if (e.contains(var_name + "=")) {
					b = false;
					continue;
				}
			}
			if (b == true)
				hidden_vars_names.add(var_name);
		}
		List<List<String>> all_combination = new LinkedList<List<String>>();
		for (String var_name : hidden_vars_names) {
			Collection<String> var_values = net.vars.get(var_name).values;
			List<String> var_values_new = new LinkedList<String>();
			for (String value : var_values) {
				var_values_new.add(var_name + "=" + value);
			}
			List<String> a = new LinkedList<>(var_values_new);
			all_combination.add(a); // first, all_combination is list of list that any entire list is the values of
									// some variable
		}
		all_combination = product(all_combination); // after 'product' function , all_combination is list of list that
													// every entire list is possible combination of hidden variables
													// values

		for (List<String> combination : all_combination) { // add the evidence to combination
			for (String e : q.evidence)
				if (!e.equals("none"))
					combination.add(e); // add evidence and wanted variables to every list and start compute every list
										// to every value of wanted variable

		}
		int count_plus = -1; // the first element souldn't be counted e.g. (a+b) + (a+b)+ (a+b) only 2 plus
								// not 3
		int count_mult = 0;
		double mone = 0;
		double mechane = 0;
		for (String wanted_var_value : net.vars.get(wanted_var).values) { // for normalization we check for every
																			// possible value of wanted variable
			double b = 0;
			String s = wanted_var + "=" + wanted_var_value;
			for (List<String> combination : all_combination) {
				combination.add(s);
				double c = 1;
				count_mult--; // the first element souldn't be counted e.g. (1*2*3) only 2 multiplication not
								// 3
				for (String var_to_comute : combination) {
					double val = net.vars.get(var_to_comute.substring(0, var_to_comute.indexOf("=")))
							.getProb(combination, var_to_comute.substring(var_to_comute.indexOf("=") + 1));
					// get probability of the variable value when his parent are given on the
					// combination
					;
					c = c * val;
					count_mult++;
				}
				count_plus++;
				b += c;

				combination.remove(combination.size() - 1); // change wanted variable value - remove the old one and in
															// the head of the loop we put new one
			}
			mechane += b;
			if (q.wanted.equals(s)) {
				mone = b;
			}
		}
		System.out.println("ans:" + df.format((mone / mechane)) + "," + count_plus + "," + count_mult);
		return null;
	}

	private static List<List<String>> product(List<List<String>> lists) {

		List<List<String>> result = new ArrayList<List<String>>();
		result.add(new ArrayList<String>());

		for (List<String> e : lists) {
			List<List<String>> tmp1 = new ArrayList<List<String>>();
			for (List<String> x : result) {
				for (String y : e) {
					List<String> tmp2 = new ArrayList<String>(x);
					tmp2.add(y);
					tmp1.add(tmp2);
				}
			}
			result = tmp1;
		}

		return result;
	}

}