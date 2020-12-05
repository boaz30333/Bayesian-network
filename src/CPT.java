
import java.util.HashMap;
import java.util.Vector;

/**
 * CPT class represent conditional probability table (CPT) each Var in the
 * Baysian network has CPT
 * 
 * @author Boaz Sharabi
 */
public class CPT {
	public String owner;
	public HashMap<String, HashMap<String, Double>> table = new HashMap<String, HashMap<String, Double>>();

	/**
	 * Build CPT table from string : each line in the string represent a new entry
	 * in the table
	 * 
	 * @param entriesArr2
	 */
	public CPT(String owner, Vector<String> newEntries) {
		this.owner = owner;
		for (String entry : newEntries) {
			String[] values = entry.substring(entry.indexOf(owner) + owner.length() + 1).split("," + owner + "=");
			String key = entry.substring(0, entry.indexOf(owner));
			if (key.endsWith(","))
				key = key.substring(0, key.length() - 1);
			if (key.isEmpty())
				key = "none";
			table.put(key, new HashMap<String, Double>());
			for (String value : values) {
				String[] prob = value.split(",");
				table.get(key).put(prob[0], Double.parseDouble(prob[1]));
			}

		}
	}

}
