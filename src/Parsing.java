import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class Parsing {
	Network net = new Network();
	ArrayList<Querie> q;

	public Parsing(String string) {
		// TODO Auto-generated constructor stub
		// TODO Auto-generated method stub
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

	public  void parse_String_queries(String string) {
		// TODO Auto-generated method stub
		Iterator<String> iter = string.lines().iterator();
		System.out.println(iter.next());
	}

	public  void parse_String_network(String string) {
		// TODO Auto-generated method stub
//		Iterator<String> iter= string.lines().iterator();
//		String[] var_names = iter.next().substring(11).split(",");
//		for(String var_name : var_names) new Var(var_name);
		String[] Vars = string.substring(4).split("Var ");
//		System.out.println(Arrays.toString(Vars));
		for (String Var_setting : Vars) {
			String[] SplitBYCpt = Var_setting.split("CPT:");
			String[] setting = SplitBYCpt[0].split("\r");
			String VarName = setting[0];
			String[] values = setting[1].substring(8).split(",");
			String[] parents = setting[2].substring(9).split(",");
			String[] entriesArr = SplitBYCpt[1].split("\r");
			Vector<String> newEntries= new Vector<>();
			for(String entry :entriesArr) {
				if(entry.isEmpty()) continue;
				String[] aa = entry.split("=");
				String[] bb= aa[0].split(",");
				String newEntry = "";
				for(int i=0;i<bb.length&&!bb[i].isEmpty();i++) { 
					newEntry=parents[i]+"="+bb[i];
					newEntry+=",";
				}
				for(int i=1;i<aa.length;i++)
				newEntry+=VarName+"="+aa[i];
				newEntries.add(newEntry);
			}
			CPT table = new CPT(VarName,newEntries);
			
			Var variable = new Var(VarName, values, parents, table);
//			System.out.println(variable.toString());
//			Iterator<String> iter= Var_setting.lines().iterator();
//			String[] var_names = iter.next().substring(11).split(",");
//			for(String var_name : var_names) new Var(var_name);
			this.net.put(variable);
		}
	}

	public static void save() {
		String fileName = "output.txt";

		try {
			PrintWriter pw = new PrintWriter(new File(fileName));

			StringBuilder sb = new StringBuilder();

			pw.write(sb.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
	}

	public Network getNetwork() {
		// TODO Auto-generated method stub
		return this.net;
	}

}
