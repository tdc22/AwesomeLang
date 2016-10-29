package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Compiler {
	public Compiler() {
		
	}
	
	public void compileFromFile(String inLocation, String outLocation, boolean keepComments) {
		StringBuilder fileContent = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inLocation));
			String line;
			while ((line = reader.readLine()) != null) {
				fileContent.append(line).append('\n');
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String result = compile(fileContent.toString());
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outLocation));
			writer.write(result);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private HashMap<String, Integer> nameToType;
	private List<String> primitiveTypes;
	private boolean skipSpace = false;
	private boolean inString = false;
	private boolean stringEnd = false;
	private int bracketCounter = 0;
	private boolean methodReturnsArray = false;
	
	private static final int TYPE_VOID						= 0;
	private static final int TYPE_SIGNED					= 1;
	private static final int TYPE_UNSIGNED					= 2;
	private static final int TYPE_CHAR						= 3;
	private static final int TYPE_SIGNED_CHAR				= 4;
	private static final int TYPE_UNSIGNED_CHAR				= 5;
	private static final int TYPE_SHORT						= 6;
	private static final int TYPE_INT						= 7;
	private static final int TYPE_SHORT_INT					= 8;
	private static final int TYPE_SIGNED_SHORT				= 9;
	private static final int TYPE_SIGNED_SHORT_INT			= 10;
	private static final int TYPE_UNSIGNED_SHORT			= 11;
	private static final int TYPE_UNSIGNED_SHORT_INT		= 12;
	private static final int TYPE_SIGNED_INT				= 13;
	private static final int TYPE_UNSIGNED_INT				= 14;
	private static final int TYPE_LONG						= 15;
	private static final int TYPE_LONG_INT					= 16;
	private static final int TYPE_SIGNED_LONG				= 17;
	private static final int TYPE_SIGNED_LONG_INT			= 18;
	private static final int TYPE_UNSIGNED_LONG				= 19;
	private static final int TYPE_UNSIGNED_LONG_INT			= 20;
	private static final int TYPE_LONG_LONG					= 21;
	private static final int TYPE_LONG_LONG_INT				= 22;
	private static final int TYPE_SIGNED_LONG_LONG			= 23;
	private static final int TYPE_SIGNED_LONG_LONG_INT		= 24;
	private static final int TYPE_UNSIGNED_LONG_LONG		= 25;
	private static final int TYPE_UNSIGNED_LONG_LONG_INT	= 26;
	private static final int TYPE_FLOAT						= 27;
	private static final int TYPE_DOUBLE					= 28;
	private static final int TYPE_LONG_DOUBLE				= 29;
	// OWN TYPES
	private static final int TYPE_STRING					= 30;
	
	public String compile(String input) {
		analyze(input);
		return translate(input).toString();
	}
	
	private void analyze(String input) {
		nameToType = new HashMap<String, Integer>();
		
		// ORDER MATTERS!!
		primitiveTypes = new ArrayList<String>();
		String[] primitives = new String[] {"string ", "long double ", "double ", "float ", "unsigned long long int ", "unsigned long long ", "signed long long int"
				, "signed long long ", "long long int ", "long long ", "unsigned long int ", "unsigned long ", "signed long int ", "signed long"
				, "long int ", "long ", "unsigned int ", "signed int ", "unsigned short int ", "unsigned short ", "signed short int ", "signed short ", "short int"
				, "int ", "short ", "unsigned char ", "signed char ", "char ", "unsigned ", "signed ", "void"};
		for(String primitive : primitives)
			primitiveTypes.add(primitive);
		
		String[] lines = input.split("\n");
		for(String l : lines) {
			for(int i = 0; i < primitives.length; i++) {
				if(l.contains(primitives[i])) {
					String primitive = primitives[i];
					int index = l.indexOf(primitive);
					l = l.replace(primitive, "");
					String valuename = l.substring(index, l.indexOf(" ", index));
					int valuetype = primitives.length - (i + 1);
					
					System.out.println("Value: " + valuename + "; Type: " + valuetype);
					nameToType.put(valuename, valuetype);
				}
			}
		}
	}
	
	private StringBuilder translate(String input) {
		StringBuilder result = new StringBuilder();
		
		String[] lines = input.split("\n");
		for(String l : lines) {
			String[] words = l.split(" ");
			boolean firstWord = true;
			for(int i = 0; i < words.length; i++) {
				String w = words[i];
				if(firstWord) {
					firstWord = false;
				}
				else {
					if(!skipSpace) result.append(" ");
					skipSpace = false;
				}
				
				// Bracket detection
				if(w.contains("{")) {
					bracketCounter++;
				}
				if(w.contains("}")) {
					bracketCounter--;
				}
				
				// In-String detection
				if(w.contains("\"")) {
					for(int s = 0; s < w.length(); s++) {
						if(w.charAt(s) == '\"' && (s == 0 || w.charAt(s-1) != '\\')) {
							inString = !inString;
							stringEnd = true;
						}
					}
				}
				
				// Overload +-Operator for strings
				if(w.contains("+")) {
					String leftword = words[i-1];
					String rightword = words[i+1];
					System.out.println(leftword + "; " +  rightword);
					if(isString(leftword) || isString(rightword) || nameToType.get(words[i-1]) == TYPE_STRING || nameToType.get(words[i+1]) == TYPE_STRING) {
						w = w.replace("+", ",");
					}
				}
				
				if(inString || stringEnd) {
					result.append(w);
					stringEnd = false;
				}
				else {
					result.append(translateWord(w));
				}
			}
			result.append("\n");
		}
		return result;
	}
	
	private boolean translateImporting = false;
	
	private String translateWord(String word) {
		if(translateImporting) {
			if(word.contains(";")) {
				translateImporting = false;
				return word.replace(";", ">");
			}
		}
		
		switch(word.replace("\t", "")) {
		
		case "import":
			translateImporting = true;
			skipSpace = true;
			return "#include<";
		
		case "string":
//			if(isInMethod())
			return word.replace("string", "char*");
//			else {
//				methodReturnsArray = true;
//				return word.replace("string", "char");
//			}
		
		}
		
		return word;
	}
	
	private boolean isString(String word) {
		return word.contains("\"");
	}
	
	private boolean isTranslatorInMethod() {
		return bracketCounter > 0;
	}
}