import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
/**
 * 
 * @author Boaz Sharabi
 *
 */
public class Query {
	public int algo;
	public String wanted;
	public String[] evidence;
	public List<String> hidden_vars;

	public Query(String wanted, String[] evidence, int algo, Network net) {
		this.algo = algo;
		this.evidence = evidence;
		this.wanted = wanted;
		hidden_vars = find_hidden(net,this.evidence,this.wanted);
	}
/**
 * 
 * @param net
 * @param evidences
 * @param wanted
 * @return list of the hidden variable in this query = no a evidence variable and no wanted variable (query variable)
 */
	private List<String> find_hidden(Network net, String[] evidences, String wanted) {
		// TODO Auto-generated method stub
		Collection<String> network_vars_names = net.vars.keySet(); // names of all vars	
		List<String> hidden_vars_names = new LinkedList<String>();
		for (String var_name : network_vars_names) { // FOR loop to find the hidden variable on the query
			boolean b = true;
			if (this.wanted.contains(var_name + "=")) {
				b = false;
				continue;
			}
			for (String e : this.evidence) {
				if (e.contains(var_name + "=")) {
					b = false;
					continue;
				}
			}
			if (b == true)
				hidden_vars_names.add(var_name);
		}
		return hidden_vars_names;
	}
}
