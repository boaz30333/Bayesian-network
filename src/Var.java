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
 * @author Boaz Sharabi
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
	 * return The probability of wanted event given his parents
	 * 
	 * @param evidences
	 * @param wanted_value
	 * @return
	 */
	public double getProb(List<String> evidences, String wanted_value) {
		// no evidence and no parent - there is answer
		if (parents.contains("none")&&evidences.isEmpty()) 
				return this.cpt.table.get("none").get(wanted_value);
		 // parents number different from  evidence number - no answer
		if( parents.size() != evidences.size())
				return -1;
		
		Set<String> parents = new HashSet<String>(this.parents);
		for (String e : evidences) {
			String evidence_var = e.substring(0, e.indexOf("="));
			if (parents.contains(evidence_var)) {
				parents.remove(evidence_var);
			}
		}
		if (!parents.isEmpty()) { // no enough evidence match parent - no solution
			System.out.println("getProb : evidences.size()!=this.parents.size()");
			return -1;
		}

		Collection<String> given = this.cpt.table.keySet(); // all "bhinten" records
		for (String record : given) { // for every line in the table
			boolean b = true;
			for (String evidence : evidences) {
				String evidence_var = evidence.substring(0, evidence.indexOf("="));
				if (!evidence.isEmpty() && !record.equals("none") && !record.contains(evidence)
						&& record.contains(evidence_var))
					b = false;
			}
			if (b == true) {
				return this.cpt.table.get(record).get(wanted_value);
			}
		}
		return -1; // error

	}
}
