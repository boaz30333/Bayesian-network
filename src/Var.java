import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * This class represents a variable in the Bayesian network
 * 
 * @author User
 *
 */
public class Var {

	String name;
	Set<String> parents = new HashSet<String>();
	LinkedList<String> values = new LinkedList<>();
	CPT cpt;
	Network my_net;

	public Var(String name, String[] values, String[] parents, CPT cpt, Network net) {
		this.name = name;
		this.values.addAll(Arrays.asList(values));
		this.parents.addAll(Arrays.asList(parents));
		this.cpt = cpt;
		this.my_net = net;

	}

	/**
	 * 
	 * @param evidence
	 * @return new CPT table represent the factor of this variable when evidence are
	 *         given.
	 */


	public String toString() {
		String result = "name:" + this.name + "\r Values:" + Arrays.toString(this.values.toArray()) + "\r Parent:"
				+ Arrays.toString(this.parents.toArray());
		return result;

	}

	public double getProb(List<String> evidences, String wanted_value) {
		if (parents.contains("none")) {
			if (evidences.isEmpty())return this.cpt.table.get("none").get(wanted_value);
		else return -1;
		}	
		if(evidences.size()!=this.parents.size() ) { //&& (   (!this.parents.contains("none")&&evidences.contains("none"))|| (this.parents.contains("none")&&!evidences.contains("none")) )
			System.out.println("getProb : evidences.size()!=this.parents.size()");
			return -1;
		}
		Collection<String> given = this.cpt.table.keySet(); // all "bhinten" records
		for (String record : given) { // for every line in the table
			boolean b = true;
			for (String e : evidences) {
//				if (!this.parents.contains(e.substring(0, e.indexOf("="))))
//					continue;

				String be = e.substring(0, e.indexOf("=")) + "=" + e.substring(e.indexOf("=") + 1);
				if (!e.isEmpty() && !record.equals("none") && !record.contains(be))
					b = false;
			}
			if (b == true) {
				return this.cpt.table.get(record).get(wanted_value);
			}

		}
		return -1; // error

	}
}
