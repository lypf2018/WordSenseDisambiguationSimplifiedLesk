/**
 * 
 */
package application;

import problem3.WordSenseDisambiguation;

/**
 * @author yzc
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String word = "bank"; 
		String sentence = "The bank can guarantee deposits will eventually cover future tuition costs because it invests in adjustable-rate mortgage securities."; 

		WordSenseDisambiguation wordSenseDisambiguation = new WordSenseDisambiguation(word, sentence);
		wordSenseDisambiguation.simplifiedLesk();
		wordSenseDisambiguation.displayResult();
	}
}
