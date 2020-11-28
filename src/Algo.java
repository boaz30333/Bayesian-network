import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
			return Algo.variable_elimination(q, net);
		}
		case (3): {
			return Algo.huristic_variable_elimination(q, net);
		}
		default:
			break;
		}
		return null;

	}

	private static String huristic_variable_elimination(Query q, Network net) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String variable_elimination(Query q, Network net) {
		// TODO Auto-generated method stub
 		LinkedList<String> order = new LinkedList<String>(q.hidden_vars);
 		int count_of_multiplication=0;
 		int count_of_sum=0;
		Collections.sort(order); // order var name for elimination
		List<Factor> factors = new LinkedList<Factor>();
		for (Var variable : net.vars.values()) {
			Factor factor_to_add = Factor.getFactorByEvidence(variable,q.evidence); 
			if(factor_to_add!=null&& !factor_to_add.table.isEmpty())
			factors.add(factor_to_add);
		}
		for  (String var_in_order : order) {
			List<Factor> factors_with_var_to_eliminate =  new LinkedList<Factor>(); // collect all factor include the var to eliminate
			Iterator<Factor> iter_f = factors.iterator();
			while(iter_f.hasNext()) {
				Factor next= iter_f.next();
				if(next.vars.contains(var_in_order)) {               // adding all the factors to eliminate to a list , remove from factor list and after elimination put back the new factor
					factors_with_var_to_eliminate.add(next);
					iter_f.remove();
				}
			
			}
			//----------------------------------------------------------------
			if(factors_with_var_to_eliminate.size()==1){
				factors.add(factors_with_var_to_eliminate.get(0).eliminate(var_in_order, net));	
				count_of_sum+= factors_with_var_to_eliminate.get(0).table.size()/net.vars.get(var_in_order).values.size();
				continue;

			}
			List<List<Factor>> permutations = new LinkedList<List<Factor>>(); // to calculate all order option to multiply factors
			for(int i=0;i<factors_with_var_to_eliminate.size();i++) { //
				permutations.add(factors_with_var_to_eliminate);
			}                                                            // check all Cartesian product this group with itself  and remove the list without all variables
			permutations = product(permutations);
			Iterator<List<Factor>> iter_p = permutations.iterator();
			boolean exist=true;
			while(iter_p.hasNext()) {
				exist=true;
				ArrayList<Factor> factor_arr= new ArrayList<>();
				Collection<Factor> factors_to_add=	iter_p.next();
				factor_arr.addAll(factors_to_add );
				if(factor_arr.get(0).toString().compareTo(factor_arr.get(1).toString())<0) {
					iter_p.remove();
					continue;
				}
				for(Factor f : factors_with_var_to_eliminate) {
					if(!factor_arr.contains(f)) {
						exist=false;
					}
				}
				if(exist==false) iter_p.remove();
			}
			//-----------------------------------------------------------------------------------
		int min_multiply= Integer.MAX_VALUE;
		Factor after_join=null;
			for(int j=0 ;j<permutations.size();j++) {
				List<Factor> permutation= permutations.get(j);
				int count=0;
	Factor f= permutation.get(0);
	for(int i=1; i<permutation.size();i++) {
		f= f.join( permutation.get(i));
		count+=f.table.size();	
	}
		if(count<min_multiply)	min_multiply=count;
		if(j==0)after_join=f ;
		}	
	//mergination
		count_of_sum+= after_join.table.size()/net.vars.get(var_in_order).values.size();
		factors.add(after_join.eliminate(var_in_order, net));
//		System.out.println(min_multiply);	
count_of_multiplication+= min_multiply;

			
		}
		Factor remaining_factor= factors.get(0);
		for(int i =1;i<factors.size();i++) {
			remaining_factor=remaining_factor.join(factors.get(i));
		}
		remaining_factor.normalize();
		double wanted_prob= remaining_factor.table.get(q.wanted);
		System.out.println(df.format(wanted_prob)+","+count_of_sum+","+count_of_multiplication);
		return df.format(wanted_prob)+","+count_of_sum+","+count_of_multiplication;
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
		List<String> hidden_vars_names =q.hidden_vars;
		List<List<String>> all_combination = find_value_combination(hidden_vars_names,net);


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
	public static List<List<String>> find_value_combination(List<String> vars ,Network net){
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

}