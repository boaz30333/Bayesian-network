import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

/**
 * This class represents a variable in the Bayesian network
 * 
 * @author User
 *
 */
public class Var {

	String name;
	LinkedList<String> parents = new LinkedList<>();
	LinkedList<String> values = new LinkedList<>();
	CPT cpt;

	public Var(String name, String[] values, String[] parents, CPT cpt) {
		this.name = name;
		this.values.addAll(Arrays.asList(values));
		this.parents.addAll(Arrays.asList(parents));
		this.cpt = cpt;

	}

	/**
	 * 
	 * @param evidence
	 * @return new CPT table represent the factor of this variable when evidence are
	 *         given.
	 */
	public CPT getFactorByEvidence(String[] evidence) {
		Collection<String> given = this.cpt.table.keySet();
		Vector<String> entries = new Vector<String>();
		;
		for (String record : given) {
			boolean b = true;
			for (String e : evidence)
				if (!e.isEmpty()&&record.contains(e))
					b = false;
			if (b == true) {
				String values="";
				if(!record.equals("none")) values+=record;
				for(String key: this.cpt.table.get(record).keySet())
					values+=this.cpt.owner+"="+key+","+this.cpt.table.get(record).get(key)+",";
				values= values.substring(0,+ values.lastIndexOf(','));
				entries.add(values);
			}
		}
		String factor_name="";                               // TODO factor name has to be variable name include owner and the variable show in the table split by ',' without given variable 
		return new CPT(this.cpt.owner,entries);
	}

	public String toString() {
		String result = "name:" + this.name + "\r Values:" + Arrays.toString(this.values.toArray()) + "\r Parent:"
				+ Arrays.toString(this.parents.toArray());
		return result;

	}

}
