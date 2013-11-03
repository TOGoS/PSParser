package togos.psparser;

import java.util.HashMap;

public class StandardOperators
{
	public final static HashMap<String,Integer> PRECEDENCE = new HashMap<String,Integer>();
	
	/* 
	 * Trying to jive as much as possible with
	 * http://en.wikipedia.org/wiki/Order_of_operations#The_standard_order_of_operations
	 */
	
	static {
		PRECEDENCE.put(".",  new Integer(80));
		PRECEDENCE.put(":",  new Integer(70));
		PRECEDENCE.put("**", new Integer(60));
		
		PRECEDENCE.put("*",  new Integer(50));
		PRECEDENCE.put("/",  new Integer(50));
		PRECEDENCE.put("%",  new Integer(50));
		
		PRECEDENCE.put("+",  new Integer(40));
		PRECEDENCE.put("-",  new Integer(40));
		
		PRECEDENCE.put("<<",  new Integer(39));
		PRECEDENCE.put(">>",  new Integer(39));
		
		/*
		 * In C these go below the comparison operators,
		 * but I never liked that because who really wants to
		 * do bitwise operations on results of comparisons??
		 *
		 * To validate this decision, it seems the Rust developers did it the same way:
		 * http://static.rust-lang.org/doc/rust.html#operator-precedence
		 * 
		 * Also as in Rust (and most other languages with infix operators),
		 * operators at the same precedence level are evaluated left-to-right. 
		 * e.g.  x <op> y <op> z == (x <op> y) <op> z 
		 */ 
		PRECEDENCE.put("&",  new Integer(36));
		PRECEDENCE.put("^",  new Integer(35));
		PRECEDENCE.put("|",  new Integer(34));
		
		PRECEDENCE.put(">",  new Integer(31));
		PRECEDENCE.put("<",  new Integer(31));
		PRECEDENCE.put(">=", new Integer(31));
		PRECEDENCE.put("<=", new Integer(31));
		PRECEDENCE.put("==", new Integer(30));
		PRECEDENCE.put("!=", new Integer(30));
		
		PRECEDENCE.put("&&", new Integer(25));
		PRECEDENCE.put("^^", new Integer(24));
		PRECEDENCE.put("||", new Integer(23));
		
		PRECEDENCE.put("and",new Integer(22));
		PRECEDENCE.put("xor",new Integer(21));
		PRECEDENCE.put("or", new Integer(20));
		
		PRECEDENCE.put("->", new Integer(16));
		PRECEDENCE.put("=",  new Integer(15));
		PRECEDENCE.put("@",  new Integer(12));
		PRECEDENCE.put(",",  new Integer(10));
		PRECEDENCE.put(";",  new Integer( 5));
		PRECEDENCE.put("\n", new Integer( 5));
	}
}
