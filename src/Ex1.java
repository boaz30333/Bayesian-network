import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class Ex1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Parsing input = new Parsing("input.txt");
		Network net = input.getNetwork();

//String[] evidence= new String[1];
//evidence[0]="A=false";
//Iterator<Var> iter=net.vars.values().iterator();
//iter.next();
//iter.next();
//CPT vv= iter.next().getFactorByEvidence(evidence);

		StringBuilder result = new StringBuilder();
		List<Query> queries = input.getQueries();
		for (int i = 0; i < queries.size(); i++) {
			result.append(Algo.run(queries.get(i), net));
		}
		save(result.toString());

		System.out.println("end");
	}

	private static List<List<String>> product(List<List<String>> lists) {

		List<List<String>> result = new ArrayList<List<String>>();
		result.add(new ArrayList<String>());

		for (List<String> e : lists) {
			List<List<String>> tmp1 = new ArrayList<List<String>>();
			for (List<String> x : result) {
				for (String y : e) {
					List<String> tmp2 = new ArrayList<String>(x);
					tmp2.add(y);
					tmp1.add(tmp2);
				}
			}
			result = tmp1;
		}

		return result;

	}

	public static void save(String result) {
		String fileName = "output.txt";

		try {
			PrintWriter pw = new PrintWriter(new File(fileName));
			pw.write(result);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
	}
}