import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
/**
 * 
 * @author Boaz Sharabi
 * This class represent algorithms 
 * standard algorithm - Bayes formula , variable elimination according ABC order 
 * and variable elimination according heuristic ordering - heuristic= minimum weight
 * more details: https://en.wikipedia.org/wiki/Variable_elimination
 */
public class Algo {
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

	private static LinkedList<String> heuristic_order(LinkedList<String> order, Network net, Query q) {

		// ------------- moralize BN -> neighbor= parent -son , son -parent , parent of
		// son -another parent of son
		HashMap<String, HashSet<String>> moralized_BN = new HashMap<>();
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
		  Set<Var> vars_to_order = new HashSet<Var>(net.vars.values());
		  LinkedList<String> Min_Weight_Order = new LinkedList<String>();
		  while (!vars_to_order.isEmpty()) {
				String min_weight_var="";
				int min_weight= Integer.MAX_VALUE;
		for (Var var : vars_to_order) {
			Set<String> neighbors = moralized_BN.get(var.name);
			
			int product_value = 1;
			if(neighbors.isEmpty()) product_value=0;
			for (String neighbor : neighbors) {// neighbor{
				boolean is_evidence=false;
				for(String evidence : q.evidence) {
					if(evidence.contains(neighbor+"=")) is_evidence=true;
				}
				if(is_evidence==false) { 
				product_value = product_value * net.vars.get(neighbor).values.size();
				}
			}
			if(product_value<min_weight) {
			min_weight= product_value;
			min_weight_var=var.name;
			}
		}
		if(order.contains(min_weight_var))
		Min_Weight_Order.addLast(min_weight_var);
		vars_to_order.remove(net.vars.get(min_weight_var));
		Set<String> neighbors = moralized_BN.get(min_weight_var);
		for (String neighbor : neighbors) {// neighbor{
			moralized_BN.get(neighbor).addAll(neighbors);
		}
		  }

		return Min_Weight_Order;
	}

	private static String variable_elimination(Query q, Network net, String type) {
//------------- check if the query can be inferred from entry in var cpt table

		String wanted_var = q.wanted.substring(0, q.wanted.indexOf("="));
		ArrayList<String> evidenceArrayList = new ArrayList<String>(Arrays.asList(q.evidence));
		evidenceArrayList.remove("none");
		double inferred_from_wanted_var_cpt = net.vars.get(wanted_var).getProb(evidenceArrayList,
				q.wanted.substring(q.wanted.indexOf("=") + 1));
		if (inferred_from_wanted_var_cpt != -1) {
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
//--------------choose the elimination order toVE algorithm
		if (type.equals("ABC_order"))
			Collections.sort(order,String.CASE_INSENSITIVE_ORDER); // order var name for elimination
		else if (type.equals("heuristic_order"))
			order = heuristic_order(order, net,q);
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
			while (iter_f.hasNext())  {
				Factor next = iter_f.next();
				if (next.vars.contains(var_in_order)) { // adding all the factors to eliminate to a list , remove from
														// factor list and after elimination put back the new factor
					factors_with_var_to_eliminate.add(next);
					iter_f.remove();
				}

			}
//----------------------- check if we can eliminate now and continue to the next var in order

			if (factors_with_var_to_eliminate.size() == 1) {
				double count_sum_eliminate = factors_with_var_to_eliminate.get(0).eliminate(var_in_order, net);
				factors.add(factors_with_var_to_eliminate.get(0));
				count_of_sum += count_sum_eliminate;// factors_with_var_to_eliminate.get(0).table.size()
				// * (net.vars.get(var_in_order).values.size() - 1);
				continue;

			}
				while(factors_with_var_to_eliminate.size()>1) {
//------------- find all possible ordering to joins factor
			List<List<Factor>> permutations = new LinkedList<List<Factor>>();
			
			for (int i = 0; i <2; i++) {
				permutations.add(factors_with_var_to_eliminate);
			} // check all Cartesian product this group with itself and remove the list
				// without all variables
			permutations = product(permutations);
			Iterator<List<Factor>> iter_p = permutations.iterator();
			while (iter_p.hasNext()) {
				ArrayList<Factor> factor_arr = new ArrayList<>();
				Collection<Factor> factors_to_add = iter_p.next();
				factor_arr.addAll(factors_to_add);
				// the ordering between the first and the second element does'nt meter , in this
				// way we chose only one
				if (factor_arr.get(0).toString().compareTo(factor_arr.get(1).toString()) <= 0) {
					iter_p.remove();
					continue;
				}

			}
////-------------join factors with var to eliminate and check minimum multiplication according to different order of joins
			int min_add_row = Integer.MAX_VALUE;
			int min_ascii_value = Integer.MAX_VALUE;
			List<Factor> min_permutation = null;
			for (int j = 0; j < permutations.size(); j++) {
				int ascii_value = 0;
				List<Factor> permutation = permutations.get(j);
				int count = 0;
				Set<String> take_part = new HashSet<String>(permutation.get(0).vars);
				for (int i = 1; i < permutation.size(); i++) {
					Set<String> union = new HashSet<String>(take_part);
					union.addAll(permutation.get(i).vars);
					count += union.size() ;
					take_part = union;
				}
				for (String s : take_part) {
					ascii_value += s.chars().reduce(0, Integer::sum);
				}
				if (count < min_add_row || (count == min_add_row && ascii_value < min_ascii_value)) {
					min_ascii_value = ascii_value;
					min_add_row = count;
					min_permutation = permutations.get(j);
				}
//				if (j == 0)
//					after_join = f;
			}

			Factor f = min_permutation.get(0);
			factors_with_var_to_eliminate.remove(f);
			for (int i = 1; i < min_permutation.size(); i++) {
				factors_with_var_to_eliminate.remove(min_permutation.get(i));
				f = f.join(min_permutation.get(i));
				factors_with_var_to_eliminate.add(f);
				count_of_multiplication += f.table.size();
				
			}}
//-------------elimination variable in order
				Factor factor= factors_with_var_to_eliminate.get(0);
		
			double count_eliminate_sum = factor.eliminate(var_in_order, net);
			if(factor.table.size()!=1) // there is  a factor after eliminate and this not irrelevant factor
			factors.add(factor);
			count_of_sum += count_eliminate_sum;


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
		double wanted_prob=0;
		if(remaining_factor.table.containsKey(q.wanted))
		// current value of the entry by the total sum
		wanted_prob = remaining_factor.table.get(q.wanted);
		return String.format("%.5f", wanted_prob) + "," + count_of_sum + "," + count_of_multiplication;
	}

	private static String standard(Query q, Network net) {
		String wanted_var = q.wanted.substring(0, q.wanted.indexOf("="));
		ArrayList<String> evidenceArrayList = new ArrayList<String>(Arrays.asList(q.evidence));
		evidenceArrayList.remove("none");
		double inferred_from_wanted_var_cpt = net.vars.get(wanted_var).getProb(evidenceArrayList,
				q.wanted.substring(q.wanted.indexOf("=") + 1));
		if (inferred_from_wanted_var_cpt != -1) {
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
						if (net.vars.get(var_to_comute.substring(0, var_to_comute.indexOf("="))).parents //find the parents of var_to_compute
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
/**
 * credit : oscar lopez : https://stackoverflow.com/questions/9591561/java-cartesian-product-of-a-list-of-lists/9594404#9594404 
 * @param <T>
 * @param lists
 * @return list of list , every internal list is possible combination in the Cartesian product
 */
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

	

/**
 * 
 * @param affect_vars
 * @param net
 * @return set of variable name that relevant to query based on affect_vars(query and evidence variable)
 */
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