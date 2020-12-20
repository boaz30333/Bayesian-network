import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
/**
 * 
 * @author Boaz Sharabi
 *
 */
public class Ex1 {

	public static void main(String[] args) {

		Parsing input = new Parsing("inputs/input.txt");
		Network net = input.getNetwork();
		StringBuilder result = new StringBuilder();
		List<Query> queries = input.getQueries();
		for (int i = 0; i < queries.size(); i++) {
			result.append(Algo.run(queries.get(i), net));
			if(i!=queries.size()-1) result.append(System.getProperty("line.separator"));
		}
		save(result.toString());
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