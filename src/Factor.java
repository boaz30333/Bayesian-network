import java.util.Set;

public class Factor {

	int num;
	Set<String> vars;
	CPT table;

	public Factor(int num, Set<String> vars, CPT table) {
		super();
		this.num = num;
		this.vars = vars;
		this.table = table;
	}

}
