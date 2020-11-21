import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class Parsing {
	Network net = new Network();
	Vector<Query> q;

	public Parsing(String string) {
		BufferedReader br = null;
		StringBuilder String_network = new StringBuilder();
		StringBuilder String_queries = new StringBuilder();

		try {
			br = new BufferedReader(new FileReader("input2.txt"));
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
			s = br.readLine();
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

	public void parse_String_queries(String string) {
		// TODO Auto-generated method stub
		String[] queries = string.split("\r");
		Vector<Query> q = new Vector<Query>();
		for (String Query : queries) {
			String wanted = Query.substring(2, Query.indexOf('|'));
			String[] evidence = Query.substring(Query.indexOf('|') + 1, Query.length() - 3).split(",");
			int algo = Integer.parseInt("" + Query.charAt(Query.length() - 1));
			q.add(new Query(wanted, evidence, algo));
		}
		this.q = q;
	}

	public void parse_String_network(String string) {
		String[] Vars = string.substring(4).split("Var ");
		for (String Var_setting : Vars) {
			String[] SplitBYCpt = Var_setting.split("CPT:");
			String[] setting = SplitBYCpt[0].split("\r");
			String VarName = setting[0];
			String[] values = setting[1].substring(8).split(",");
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
					newEntry += parents[i] + "=" + bb[i];
					newEntry += ",";
				}
				double sum_prob = 0;
				for (int i = 1; i < aa.length; i++) {
					sum_prob += Double.parseDouble(aa[i].split(",")[1]);
					newEntry += VarName + "=" + aa[i];
				}
				newEntry += "," + VarName + "=" + values[values.length - 1] + "," + (double) (1 - sum_prob);

				newEntries.add(newEntry);
			}
			CPT table = new CPT(VarName, newEntries);
			Var variable = new Var(VarName, values, parents, table);
			this.net.put(variable);
		}
	}

	public Network getNetwork() {
		// TODO Auto-generated method stub
		return this.net;
	}

	public Vector<Query> getQueries() {
		return this.q;
	}

}
