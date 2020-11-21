
public class Query {
public int algo;
public String wanted;
public String[] evidence;
public Query(String wanted, String[] evidence,int algo) {
	this.algo=algo;
	this.evidence=evidence;
	this.wanted=wanted;
}
}
