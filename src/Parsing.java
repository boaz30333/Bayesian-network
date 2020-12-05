import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
/**
 * 
 * @author Boaz Sharabi
 *
 */
public class Parsing {
	Network net = new Network();
	List<Query> q;

	public Parsing(String input_path) {
		BufferedReader br = null;
		StringBuilder String_network = new StringBuilder();
		StringBuilder String_queries = new StringBuilder();

		try {
			br = new BufferedReader(new FileReader(input_path));
			br.readLine();// Network
			br.readLine();// Vars names - not necessary
			br.readLine(); // /r
			String s;
			while (br.ready()) {
				s = br.readLine();
				if (s.equals("Queries"))
					break;
				String_network.append(s + "\r");
			}
			while (br.ready()) {
				s = br.readLine();
				String_queries.append(s + "\r");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("could not read file");
		}
		parse_String_network(String_network.toString());
		parse_String_queries(String_queries.toString());
	}
/**
 * this method parse String input and create List of queries
 * @param string
 */
	public void parse_String_queries(String string) {
		String[] queries = string.split("\r");
		List<Query> q = new LinkedList<Query>();
		for (String Query : queries) {
			String wanted;
			String[] evidence = null;
			if (Query.contains("|")) {
				wanted = Query.substring(2, Query.indexOf('=') + 1) + "|"
						+ Query.substring(Query.indexOf('=') + 1, Query.lastIndexOf('|')) + "|";
				evidence = Query.substring(Query.indexOf('|') + 1, Query.length() - 3).split(",");
				for (int i = 0; i < evidence.length; i++) {
					evidence[i] = evidence[i].substring(0, evidence[i].indexOf("=") + 1) + "|"
							+ evidence[i].substring(evidence[i].indexOf("=") + 1) + "|";
				}
			} else {
				wanted = Query.substring(2, Query.indexOf('=') + 1) + "|"
						+ Query.substring(Query.indexOf('=') + 1, Query.lastIndexOf(')')) + "|";
				String[] none_evidence = new String[] { "none" };
				evidence = none_evidence;
			}
			int algo = Integer.parseInt("" + Query.charAt(Query.length() - 1));
			q.add(new Query(wanted, evidence, algo,net));
		}
		this.q = q;
	}
/**
 * this method parse String input and create Bayesian Network
 * @param string
 */
	public void parse_String_network(String string) {
		String[] Vars = string.substring(4).split("Var ");
		for (String Var_setting : Vars) {
			String[] SplitBYCpt = Var_setting.split("CPT:");
			String[] setting = SplitBYCpt[0].split("\r");
			String VarName = setting[0];
			String[] values = setting[1].substring(8).split(",");
			for (int i = 0; i < values.length; i++) {
				values[i] = values[i].substring(0, values[i].indexOf("=") + 1) + "|"
						+ values[i].substring(values[i].indexOf("=") + 1) + "|";
			}
			String[] parents = setting[2].substring(9).split(",");
			String[] entriesArr = SplitBYCpt[1].split("\r");
			Vector<String> newEntries = new Vector<>();
			for (String entry : entriesArr) {
				if (entry.isEmpty())
					continue;
				String[] aa = entry.split("=");
				String[] bb = aa[0].split(",");
				String newEntry = "";
				for (int i = 0; i < bb.length && !bb[i].isEmpty(); i++) {
					newEntry += parents[i] + "=|" + bb[i] + "|";
					newEntry += ",";
				}
				double sum_prob = 0;
				for (int i = 1; i < aa.length; i++) {
					sum_prob += Double.parseDouble(aa[i].split(",")[1]);
					newEntry += VarName + "=|" + aa[i].substring(0, aa[i].indexOf(',')) + "|"
							+ aa[i].substring(aa[i].indexOf(','));
				}
				newEntry += "," + VarName + "=" + values[values.length - 1] + ","
						+  (1 - sum_prob);

				newEntries.add(newEntry);
			}
			CPT table = new CPT(VarName, newEntries);
			Var variable = new Var(VarName, values, parents, table, this.net);
			this.net.put(variable);
		}
	}
/**
 * 
 * @return Bayesian network 
 */
	public Network getNetwork() {
		return this.net;
	}
/**
 * 
 * @return list of queries
 */
	public List<Query> getQueries() {
		return this.q;
	}

}
