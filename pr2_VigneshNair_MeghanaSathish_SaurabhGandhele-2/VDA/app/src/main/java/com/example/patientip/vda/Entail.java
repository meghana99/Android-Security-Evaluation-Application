package com.example.patientip.vda;

import android.content.Context;
import android.util.Log;

import java.util.*;
import java.io.*;

public class Entail{
	public static BufferedReader br;
	public static String currentLine;
	public static String sentencesS = "";
	public static String[] sentencesA;
	public static LinkedList<String> facts = new LinkedList<String>();
	public static Hashtable<String, Boolean> factsTable = new Hashtable<String, Boolean>();
	public static LinkedList<String> notfacts = new LinkedList<String>();
	public static LinkedList<Sentence> sentences = new LinkedList<Sentence>();
	public static LinkedList<String> agenda = new LinkedList<String>();
	public static Hashtable<String, Boolean> entailed = new Hashtable<String, Boolean>();
	public static Hashtable<String, Boolean> setSymbol = new Hashtable<String, Boolean>(); 
	public static Hashtable<String, Boolean> valSymbol = new Hashtable<String, Boolean>();
	public static LinkedList<String> toPush = new LinkedList<String>();
	public static LinkedList<String> bcSentence = new LinkedList<String>();
	public static LinkedList<String> bcFacts = new LinkedList<String>();
	public Context context;
	public Entail(Context getappContext)
	{
		context=getappContext;
	}
	public static void print(Object stringOrMore){
		System.out.println(stringOrMore);
	}

	public static boolean notOp(String check){
		return ((!check.equals("^")) && (!check.equals("v")) && (!check.equals("=>")));
	}

	public static boolean notOpAtAll(String check){
		return ((!check.contains("^")) && (!check.contains("v")) && (!check.contains("=>")));
	}


	public static void addSentences(LinkedList<String> sentencesList){
		Iterator<String> sentenceIt = sentencesList.iterator();
		while(sentenceIt.hasNext()){
			String sent = sentenceIt.next();
			// print(sent);
			LinkedList<Operator> ops = new LinkedList<Operator>();
			if(notOpAtAll(sent)){
				if(sent.charAt(0)=='~'){
					notfacts.add(sent);
					factsTable.put(sent, true);
				}else{
					facts.add(sent);
					factsTable.put(sent, true);
				}
			} else {
				String[] tokens = sent.split(" ");
				Variable[] variables = new Variable[tokens.length];
				for(int j=0;j<tokens.length;j++){
					if(notOp(tokens[j])){
						if(tokens[j].charAt(0) == '~'){
							variables[j] = new Variable(tokens[j], false, true);
							if(tokens[j].equals("false")){
								variables[j] = new Variable("true");
							}
							if(tokens[j].equals("true")){
								variables[j] = new Variable("false");
							}
						}else{
							variables[j] = new Variable(tokens[j], false, false);
							if(tokens[j].equals("false")){
								variables[j] = new Variable("false");
							}
							if(tokens[j].equals("true")){
								variables[j] = new Variable("true");
							}
						}
					}
				}
				for(int j=0;j<tokens.length;j++){
					if(tokens[j].equals("^")){
						ops.add(new And(variables[j-1], variables[j+1]));	
					}
					if(tokens[j].equals("v")){
						ops.add(new Or(variables[j-1], variables[j+1]));
					}
					if(tokens[j].equals("=>")){
						ops.add(new Imp(variables[j-1], variables[j+1]));	
					}
				}
			sentences.add(new Sentence(ops, false));				
			}
		}
	}
	public static void deParenSentence(String sentence, int depth, LinkedList<String> toPush, String build, boolean ready){
		if(sentence.length()>0 && sentence.charAt(0)=='('){
			deParenSentence(sentence.substring(1), (depth +1), toPush, build, true);
		} else if(sentence.length()>0 && sentence.charAt(0)==')'){
			deParenSentence(sentence.substring(1), (depth -1), toPush, build, true);
		} else if(depth == 0 && ready){
			String rest = "";
			String[] splitC = sentence.split(" \\^ ");
			if(splitC.length > 1){
				int i;
				for (i =1;i<splitC.length-1;i++){
					rest += splitC[i]+" ^ ";
				}
				rest += splitC[i];
				toPush.push(build);
				deParenSentence(rest, depth, toPush, "", false);
			} else{
				toPush.push(build);
			}
		} else {
			build += sentence.charAt(0);
			deParenSentence(sentence.substring(1), depth, toPush, build, true);
		}
	}
	public static LinkedList<String> removeParens(String[] sentencesA){
		LinkedList<String> sentenceList = new LinkedList<String>();
		for(int i=0;i<sentencesA.length;i++){
			if(sentencesA[i].contains("(")){
				toPush.clear();
				deParenSentence(sentencesA[i], 0, toPush, "", false);
				Iterator<String> toPushi = toPush.iterator();
				while(toPushi.hasNext()){
					sentenceList.push(toPushi.next());
				}
			} else {
				sentenceList.push(sentencesA[i]);
			}
		}
		return sentenceList;
	}

	public String mainFunction(String alg, String kBFile, String querySymbol){
		Boolean bool;
		try{
			InputStream is = context.getApplicationContext().getResources().getAssets().open(kBFile);

			br = new BufferedReader(new InputStreamReader(is));
			while ((currentLine = br.readLine()) != null) {
				sentencesS += currentLine.trim().replaceAll(" +", " ") + "@";
			}
			sentencesA = sentencesS.split("@");
			LinkedList<String> sentenceList = new LinkedList<String>();
			sentenceList = removeParens(sentencesA);
			addSentences(sentenceList);

			//convert the sentences to implicative form
			LinkedList<Sentence> toRemove = new LinkedList<Sentence>();
			LinkedList<Sentence> toAdd = new LinkedList<Sentence>();
			Iterator<Sentence> i = sentences.iterator();
			while(i.hasNext()){
				Sentence temp = i.next();
				if(temp.opList.getLast().token.equals("=>")){
					temp.impForm = true;
				}

				Variable head = null;
				Operator opHead = null;
				Operator opHeadFirst = null;
				boolean allOr = true;
				boolean noHead = true;
				Iterator<Operator> j = temp.opList.iterator();
				while(j.hasNext()){
					Operator tempOp = j.next();
					if(!tempOp.token.equals("v")){
						allOr = false;
					}
					if(!(tempOp.first.not)){
						head = tempOp.first;
						opHeadFirst = tempOp;
						noHead = false;
					}
					if(!tempOp.second.not){
						head = tempOp.second;
						opHead = tempOp;
						noHead = false;
					}
				}

				if(noHead){
					Or addFalse = new Or(temp.opList.getLast().second, new Variable("false"));
					opHead = addFalse;
					temp.opList.add(addFalse);
				}

				//convert to imp
				Iterator<Operator> k = temp.opList.iterator();
				if(allOr){
					Sentence impSentence = new Sentence();
					
					Variable impFirst = new Variable();
					Variable impSecond = new Variable();
					Operator impOp = new And();

					LinkedList<Operator> impOps = new LinkedList<Operator>();
					toRemove.add(temp);
					while(k.hasNext()){
						Operator tempOp = k.next();
						if(tempOp != opHead){
							impFirst = new Variable();
							impSecond = new Variable();

							impFirst.name = tempOp.first.name.substring(1);
							impFirst.value = tempOp.first.value;
							impFirst.set = tempOp.first.set;
							impFirst.not = false;
							//
							impSecond.name = tempOp.second.name.substring(1);
							impSecond.value = tempOp.second.value;
							impSecond.set = tempOp.second.set;
							impSecond.not = false;

							impOp = new And(impFirst, impSecond);
							impOp.token = "^";
							if(impOp.first.name.equals("")){
							} else {
								impOps.add(impOp);
							}

						} else{
							if(opHeadFirst != null){
								impFirst = new Variable();
								impSecond = new Variable();
								impFirst.name = tempOp.first.name.substring(1);
								impFirst.value = tempOp.first.value;
								impFirst.set = tempOp.first.set;
								impFirst.not = false;
								impSecond.name = opHeadFirst.second.name.substring(1);
								impSecond.value = opHeadFirst.second.value;
								impSecond.set = opHeadFirst.second.set;
								impSecond.not = false;
								impOp = new And(impFirst, impSecond);
								impOp.token = "^";
								impOps.add(impOp);
							}
						}
					}

					impSentence.opList = impOps;

					if(noHead){
						impFirst = impOps.getLast().second;
						Operator nImp = new Imp(impFirst, new Variable("false"));
						impOps.add(nImp);
						impSentence.impForm = true;
					} else {
						impFirst = impOps.getLast().second;
						impSecond = head;
						Operator nImp = new Imp(impFirst, impSecond);
						impOps.add(nImp);
						impSentence.impForm = true;
					}
						toAdd.add(impSentence);
				}
			}
			Iterator<Sentence> removeSentences = toRemove.iterator();
			while(removeSentences.hasNext()){
				Sentence remove = removeSentences.next();
				sentences.remove(remove);
			}
			Iterator<Sentence> addSentences = toAdd.iterator();
			while(addSentences.hasNext()){
				Sentence add = addSentences.next();
				sentences.add(add);
			}

			agenda = facts;
			Iterator<String> a = agenda.iterator();
			while(a.hasNext()){
				String symbol = a.next();
				setSymbol.put(symbol, true);
				valSymbol.put(symbol, true);
			}
			Iterator<String> b = notfacts.iterator();
			while(b.hasNext()){
				String symbol = b.next();
				setSymbol.put(symbol, true);
				valSymbol.put(symbol.substring(1), false);
			}

			if(alg.equals("forward")){
				Iterator<String> factsIt = facts.iterator();
				while(factsIt.hasNext()){
					print(factsIt.next());
				}

				while(!agenda.isEmpty()){
					LinkedList<Sentence> clauses = new LinkedList<Sentence>();
					Iterator<Sentence> clauseInit = sentences.iterator();
					while(clauseInit.hasNext()){
						clauses.push(clauseInit.next());
					}

					String fact = agenda.pop();
					if(!fact.equals("false")){
						entailed.put(fact, true);
					}
					Iterator<Sentence> clauseI = clauses.iterator();
					while(clauseI.hasNext()){
						Sentence clause = clauseI.next(); 

						Iterator<Operator> oi = clause.opList.iterator();
						while(oi.hasNext()){
							Operator operator = oi.next();
							if(setSymbol.containsKey(operator.first.name)){
								operator.first.set(true);
							}
							if(setSymbol.containsKey(operator.second.name)){
								operator.second.set(true);
							}
							if(valSymbol.containsKey(operator.first.name)){
								operator.first.setValue(valSymbol.get(operator.first.name));
							}
							if(valSymbol.containsKey(operator.second.name)){
								operator.second.setValue(valSymbol.get(operator.second.name));
							}
						}

						oi = clause.opList.iterator();
						Operator headOp = null;
						while(oi.hasNext()){
							Operator operator = oi.next();
							operator.op();
							if(operator.token.equals("=>")){
								headOp = operator;
							}
						}
						if(headOp!= null){
							if(headOp.first.getValue()){
								setSymbol.put(headOp.second.name, true);
								valSymbol.put(headOp.second.name, true);

								sentences.remove(clause);
								agenda.add(headOp.second.name);
								printSentence(clause);
							}
							
						}
					}
				}
				HashMap<String,String> map = new HashMap<String, String>();
				map.put("0","VERY LOW");
				map.put("1","SLIGHTLY LOW");
				map.put("2","LOW");
				map.put("3","LESS MEDIUM");
				map.put("4","MEDIUM");
				map.put("5","MORE MEDIUM");
				map.put("6","HIGH");
				map.put("7","VERY HIGH");

				if(entailed.containsKey(querySymbol)){
					return map.get(querySymbol);
				}
			}

		} catch (IOException e){
			System.err.println("Try <algorithm> <knowledgeBaseFile.txt> <querySybol>");
			e.printStackTrace();
		}
		return "";
	}


	public static void printSentence(Sentence clause){
		Iterator<Operator> opTest = clause.opList.iterator();
		String sentenceTest = "";
		int i = 0;
		while(opTest.hasNext()){
			Operator testOP = opTest.next();
			if(i == 0){
				sentenceTest += testOP.first.name + " " + testOP.token + " " + testOP.second.name;
			} else {
				sentenceTest += " " + testOP.token + " " + testOP.second.name;
			}
			i++;
		}
		print(sentenceTest);
	}

}