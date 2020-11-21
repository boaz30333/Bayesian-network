import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Algo {

	public static String run(Query q, Network net) {
		switch(q.algo) {
		case(1):{
			return Algo.standard(q,net);
		}
		case(2):{
			return Algo.variable_elimitaion(q,net);
		}
		case(3):{
			return Algo.huristic_variable_elimitaion(q,net);
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
		return null;
	}

	private static String standard(Query q, Network net) {
Collection<String> network_vars_names = net.vars.keySet();
List<String> hidden_vars_names = new LinkedList<String>() ;
for(String var_name: network_vars_names ) {
	if(q.wanted.contains(var_name+"=")) continue;
	for(String e : q.evidence) {
		if(e.contains(var_name+"=")) continue;		
	}
	hidden_vars_names.add(var_name);
}
List<List<String>> all_combinaton = new LinkedList<List<String>>(); // first, all_combination is list of list than any entire list is the values of some variable
for(String var_name: hidden_vars_names ) {                           // after 'product' function , all_combination is list of list than any entire list is possible combination
	Collection<String> var_values= net.vars.get(var_name).values;
	List<String> var_values_new=  new LinkedList<String>() ;
	for (String value : var_values) {
		var_values_new.add(var_name+"="+value);
	}
	List<String> a= new LinkedList<>(var_values_new);
	all_combinaton.add(a);
}
all_combinaton =product(all_combinaton);

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