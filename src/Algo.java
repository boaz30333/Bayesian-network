import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

public class Algo {
	public static DecimalFormat df = new DecimalFormat("###.#####"); // TODO maybe not needed to format here

	public static String run(Query q, Network net) {
		switch (q.algo) {
		case (1): {
			return Algo.standard(q, net);
		}
		case (2): {
			return Algo.variable_elimination(q, net, "ABC_order");
		}
		case (3): {
			return Algo.variable_elimination(q, net, "heuristic_order");
		}
		default:
			break;
		}
		return null;

	}

	private static LinkedList<String> heuristic_order(LinkedList<String> order, Network net) {
		// TODO Auto-generated method stub

		// -------------morlaize bn - neighbor= parent -son , son -parent , parent of
		// son -another parent of son
		HashMap<String, HashSet<String>> moralized_BN = new HashMap<>();
		HashMap<String, Integer> weight_values = new HashMap<>();

		for (Var var : net.vars.values()) {
			moralized_BN.put(var.name, new HashSet<>());
		}

		for (Var var : net.vars.values()) {
			for (String parent_a : var.parents) {
				if (parent_a.equals("none"))
					continue;
				moralized_BN.get(parent_a).add(var.name);
				moralized_BN.get(var.name).add(parent_a);
				moralized_BN.get(parent_a).addAll(var.parents);
				moralized_BN.get(parent_a).remove(parent_a);
			}
		}

		for (Var var : net.vars.values()) {
			Set<String> neighbors = moralized_BN.get(var.name);
			int product_value = 1;
			for (String neighbor : neighbors) {// neighbor{
				product_value= product_value*net.vars.get(neighbor).values.size();
			}
			weight_values.put(var.name, product_value);
		}

		// -------------compute product of values of all neighbors values
//		PriorityQueue<String> byMinWight = new PriorityQueue<>(new Comparator<String>() {
//
//			@Override
//			public int compare(String arg0, String arg1) {
//				// TODO Auto-generated method stub
//				if(weight_values.get(arg0)>weight_values.get(arg1)) return -1;
//				else if (weight_values.get(arg0)<weight_values.get(arg1)) return 1;
//				else
//				return 0;
//			}
//		});

Collections.sort(order,new Comparator<String>() {
	
	@Override
	public int compare(String arg0, String arg1) {
		// TODO Auto-generated method stub
		if(weight_values.get(arg0)>weight_values.get(arg1)) return -1;
		else if (weight_values.get(arg0)<weight_values.get(arg1)) return 1;
		else
		return 0;
	}
});
return order;
	}

	private static String variable_elimination(Query q, Network net, String type) {
		// TODO Auto-generated method stub
//------------- check if the query can be inferred from entry in var cpt table

		String wanted_var = q.wanted.substring(0, q.wanted.indexOf("="));
		ArrayList<String> evidenceArrayList = new ArrayList<String>(Arrays.asList(q.evidence));
		evidenceArrayList.remove("none");
		double inferred_from_wanted_var_cpt = net.vars.get(wanted_var).getProb(evidenceArrayList,
				q.wanted.substring(q.wanted.indexOf("=") + 1));
		if (inferred_from_wanted_var_cpt != -1) {
			System.out.println(String.format("%.5f", inferred_from_wanted_var_cpt) + "," + 0 + "," + 0);
			return String.format("%.5f", inferred_from_wanted_var_cpt) + "," + 0 + "," + 0;

		}
//-------------

		LinkedList<String> order = new LinkedList<String>(q.hidden_vars);
		int count_of_multiplication = 0;
		int count_of_sum = 0;

//------------- check which vars are relevant: " In general, every variable that is not an ancestor of a query
		// variable or evidence variable is irrelevant to the query." (lesson PDF)
		Set<String> affect_vars = new HashSet<String>();
		affect_vars.add(wanted_var);
		for (String evidence : q.evidence)
			affect_vars.add(evidence.substring(0, evidence.indexOf("=")));
		Set<String> relevant_vars = all_relevant(affect_vars, net);
		Iterator<String> iter = order.iterator();
		while (iter.hasNext()) {
			String var = iter.next();
			if (!relevant_vars.contains(var))
				iter.remove();
		}
		if (type.equals("ABC_order"))
			Collections.sort(order); // order var name for elimination
		else if (type.equals("heuristic_order"))
			order = heuristic_order(order, net);
//------------- prepare factors from the variables
		List<Factor> factors = new LinkedList<Factor>();
		for (Var variable : net.vars.values()) {
			if (!relevant_vars.contains(variable.name))
				continue;
			Factor factor_to_add = Factor.getFactorByEvidence(variable, q.evidence);
			if (factor_to_add != null && !factor_to_add.table.isEmpty())
				factors.add(factor_to_add);
		}
//-------------join and eliminate according the order
		for (String var_in_order : order) {
			List<Factor> factors_with_var_to_eliminate = new LinkedList<Factor>(); // collect all factor include the var
																					// to eliminate
			Iterator<Factor> iter_f = factors.iterator();
			while (iter_f.hasNext()) {
				Factor next = iter_f.next();
				if (next.vars.contains(var_in_order)) { // adding all the factors to eliminate to a list , remove from
														// factor list and after elimination put back the new factor
					factors_with_var_to_eliminate.add(next);
					iter_f.remove();
				}

			}
//------------- check if we can eliminate now and continue to the next var in order

			if (factors_with_var_to_eliminate.size() == 1) {
				factors.add(factors_with_var_to_eliminate.get(0).eliminate(var_in_order, net));
				count_of_sum += factors_with_var_to_eliminate.get(0).table.size()
						* (net.vars.get(var_in_order).values.size() - 1);
				continue;

			}

//------------- find all possible ordering to joins factor
			List<List<Factor>> permutations = new LinkedList<List<Factor>>();

			for (int i = 0; i < factors_with_var_to_eliminate.size(); i++) {
				permutations.add(factors_with_var_to_eliminate);
			} // check all Cartesian product this group with itself and remove the list
				// without all variables
			permutations = product(permutations);
			Iterator<List<Factor>> iter_p = permutations.iterator();
			boolean exist = true;
			while (iter_p.hasNext()) {
				exist = true;
				ArrayList<Factor> factor_arr = new ArrayList<>();
				Collection<Factor> factors_to_add = iter_p.next();
				factor_arr.addAll(factors_to_add);
				if (factor_arr.get(0).toString().compareTo(factor_arr.get(1).toString()) < 0) { // the ordering between
																								// the first and the
																								// second element
																								// does'nt meter , in
																								// this way we chose
																								// only one
					iter_p.remove();
					continue;
				}
				for (Factor f : factors_with_var_to_eliminate) {
					if (!factor_arr.contains(f)) {
						exist = false;
					}
				}
				if (exist == false)
					iter_p.remove();
			}
//-------------join factors with var to eliminate and check minimum multiplication according to different order of joins
			int min_multiply = Integer.MAX_VALUE;
			Factor after_join = null;
			for (int j = 0; j < permutations.size(); j++) {
				List<Factor> permutation = permutations.get(j);
				int count = 0;
				Factor f = permutation.get(0);
				for (int i = 1; i < permutation.size(); i++) {
					f = f.join(permutation.get(i));
					count += f.table.size();
				}
				if (count < min_multiply)
					min_multiply = count;
				if (j == 0)
					after_join = f;
			}
//-------------elimination variable in order
			factors.add(after_join.eliminate(var_in_order, net));
			count_of_sum += after_join.table.size() * (net.vars.get(var_in_order).values.size() - 1);
			count_of_multiplication += min_multiply;

		}

//-------------finals joins
		Factor remaining_factor = factors.get(0);
		for (int i = 1; i < factors.size(); i++) {
			count_of_multiplication += remaining_factor.table.size();// == number of values of wanted variable
			remaining_factor = remaining_factor.join(factors.get(i));
		}

//-------------normalization
		remaining_factor.normalize();
		count_of_sum += remaining_factor.table.size() - 1; // we sum all entries for normalization , And divide the
															// current value of the entry by the total sum
		double wanted_prob = remaining_factor.table.get(q.wanted);
		System.out.println(String.format("%.5f", wanted_prob) + "," + count_of_sum + "," + count_of_multiplication);
		return String.format("%.5f", wanted_prob) + "," + count_of_sum + "," + count_of_multiplication;
	}

	private static String standard(Query q, Network net) {
		String wanted_var = q.wanted.substring(0, q.wanted.indexOf("="));
		ArrayList<String> evidenceArrayList = new ArrayList<String>(Arrays.asList(q.evidence));
		evidenceArrayList.remove("none");
		double inferred_from_wanted_var_cpt = net.vars.get(wanted_var).getProb(evidenceArrayList,
				q.wanted.substring(q.wanted.indexOf("=") + 1));
		if (inferred_from_wanted_var_cpt != -1) {
			System.out.println(String.format("%.5f", inferred_from_wanted_var_cpt) + "," + 0 + "," + 0);
			return String.format("%.5f", inferred_from_wanted_var_cpt) + "," + 0 + "," + 0;

		}

		List<String> hidden_vars_names = q.hidden_vars;
		List<List<String>> all_combination = find_value_combination(hidden_vars_names, net);

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
					List<String> parents_value = new LinkedList<>();
					for (String var : combination) {
						if (net.vars.get(var_to_comute.substring(0, var_to_comute.indexOf("="))).parents
								.contains(var.substring(0, var.indexOf("="))))
							parents_value.add(var);
					}
					double val = net.vars.get(var_to_comute.substring(0, var_to_comute.indexOf("=")))
							.getProb(parents_value, var_to_comute.substring(var_to_comute.indexOf("=") + 1));
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
		return (String.format("%.5f", (mone / mechane)) + "," + count_plus + "," + count_mult);
	}

	public static List<List<String>> find_value_combination(List<String> vars, Network net) {
		List<List<String>> all_combination = new LinkedList<List<String>>();
		for (String var_name : vars) {
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
		return all_combination;
	}

	public static <T> List<List<T>> product(List<List<T>> lists) {

		List<List<T>> result = new ArrayList<List<T>>();
		result.add(new ArrayList<T>());

		for (List<T> e : lists) {
			List<List<T>> tmp1 = new ArrayList<List<T>>();
			for (List<T> x : result) {
				for (T y : e) {
					List<T> tmp2 = new ArrayList<T>(x);
					tmp2.add(y);
					tmp1.add(tmp2);
				}
			}
			result = tmp1;
		}

		return result;
	}

	private static Set<String> all_relevant(Set<String> affect_vars, Network net) {
		Set<String> relevant_vars = new HashSet<String>();
		for (String affect_var : affect_vars) {

			// ------------- use pre order to scan all relevant variable - copyright
			// https://www.geeksforgeeks.org/iterative-preorder-traversal/
			if (net.vars.get(affect_var).parents.contains("none")) {
				continue;
			}

			// Create an empty stack and push root to it
			Stack<Var> nodeStack = new Stack<Var>();
			nodeStack.push(net.vars.get(affect_var));

			/*
			 * Pop all items one by one. Do following for every popped item a) print it b)
			 * push its right child c) push its left child Note that right child is pushed
			 * first so that left is processed first
			 */
			while (nodeStack.empty() == false) {

				// Pop the top item from stack and print it
				Var mynode = nodeStack.peek();
				relevant_vars.add(mynode.name);
				nodeStack.pop();

				// Push right and left children of the popped node to stack
				Set<String> parents_of_mynode = mynode.parents;
				for (String parent : parents_of_mynode) {
					if (parent.equals("none"))
						continue;
					Var p = net.vars.get(parent);
					nodeStack.push(p);
				}
			}
		}
		return relevant_vars;
	}
}