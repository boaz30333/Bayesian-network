import java.util.Arrays;
import java.util.LinkedList;

/**
 * This class represents a variable in the Bayesian network
 * @author User
 *
 */
public class Var {

	String name;
	LinkedList<String> parents = new LinkedList<>();
	LinkedList<String> values = new LinkedList<>();
	CPT table;
	public Var(String name,String[] values,String[] parents,CPT table ) {
		this.name=name;
		this.values.addAll(Arrays.asList(values));
		this.parents.addAll(Arrays.asList(parents));
		this.table= table;
		
	}
	
	public Var(String var_name) {
		// TODO Auto-generated constructor stub
		this.name= var_name;
	}
	public String toString() {
		String result = "name:"+this.name+"\r Values:"+Arrays.toString(this.values.toArray()) +"\r Parent:"+Arrays.toString(this.parents.toArray());
		return result;
		
	}
	
}
