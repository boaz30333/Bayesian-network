import java.util.HashMap;

/**
 * Network class represent Bayesian network
 * more details : https://en.wikipedia.org/wiki/Bayesian_network 
 * @author User
 *
 */
public class Network {
public HashMap<String, Var> vars = new HashMap<>();

public void print() {
		// TODO Auto-generated method stub
		System.out.println("dcfsdf");;
	}
public void put(Var variable) {
	this.vars.put(variable.name, variable);
}
}