package main;

public class Start {
	public static void main(String[] args) {
		Compiler c = new Compiler();
		c.compileFromFile(args[0], args[1], true);
		System.out.println("Compiled to " + args[1]);
	}
}
