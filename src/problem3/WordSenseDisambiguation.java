package problem3;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.IndexWordSet;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

public class WordSenseDisambiguation {

	// Stop list during running Simplified Lesk algorithm 
	private static final String[] STOPLIST = {"bank", "The", "can", "will", "because", "it", "in"};

	// for output
	private String word;
	private String sentence;
	private List<Map.Entry<String, List<Map.Entry<String, String[]>>>> wordSensesForEachPOSDisplayList;
	private int[] bestSensePOSIndex;
	private int[] bestSenseIndex;

	/**
	 * Constructor collecting information to be displayed
	 * @param word the word to be processed
	 * @param sentence the context of the word to be processed
	 */
	public WordSenseDisambiguation(String word, String sentence) {
		this.word = word;
		this.sentence = sentence;
	}

	/**
	 * Instance method, calling the private static same name method, keeping information to display.
	 * @return the best sense as Synset type object
	 */
	public Synset simplifiedLesk() {
		wordSensesForEachPOSDisplayList = new ArrayList<>();
		bestSensePOSIndex = new int[1];
		bestSenseIndex = new int[1];
		return simplifiedLesk(word, sentence, wordSensesForEachPOSDisplayList, bestSensePOSIndex, bestSenseIndex);
	}

	/**
	 * Static method, calling the private static same name method, only returning best sense as Synset object without keeping information to display. Called directly with Class name,
	 * @param word The word to be processed
	 * @param sentence The context of the word to be processed
	 * @return the best sense as Synset type object
	 */
	public static Synset simplifiedLesk(String word, String sentence) {
		return simplifiedLesk(word, sentence, null, null, null);
	}

	/**
	 * Static implementation of Simplified Lesk algorithm. To be reused for instance method with displaying information and static method only returning best word sense
	 * wordSensesForEachPOSDisplayList, bestSensePOSIndex and bestSenseIndex are to hold the displaying information 
	 * @param word The word to be processed
	 * @param sentence The context of the word to be processed
	 * @param wordSensesForEachPOSDisplayList Word Senses for each POS display information list
	 * @param bestSensePOSIndex The first index(POS) of wordSensesForEachPOSDisplayList 
	 * @param bestSenseIndex The second index(Senses) of wordSensesForEachPOSDisplayList
	 * @return the best sense as Synset type object
	 */
	private static Synset simplifiedLesk(String word, String sentence, List<Map.Entry<String, List<Map.Entry<String, String[]>>>> wordSensesForEachPOSDisplayList, int[] bestSensePOSIndex, int[] bestSenseIndex) {
		Dictionary dictionary = null;
		Synset bestSense = null;
		try {
			dictionary = Dictionary.getDefaultResourceInstance();
			IndexWordSet indexWordSet = dictionary.lookupAllIndexWords(word);
			Set<POS> posSet = indexWordSet.getValidPOSSet();
			Iterator<POS> it = posSet.iterator();
			int maxOverlap = 0;
			String[] context = sentence.split("[\\s\\.]+");

			while (it.hasNext()) {
				POS pos = (POS) it.next();
				IndexWord indexWord = indexWordSet.getIndexWord(pos);
				List<Synset> wordSenses = indexWord.getSenses();

				if(wordSensesForEachPOSDisplayList != null) { // for output
					wordSensesForEachPOSDisplayList.add(new AbstractMap.SimpleEntry<String, List<Map.Entry<String, String[]>>>(indexWord.getPOS().name(), new ArrayList<>()));
				}
				
				for (int i = 0; i < wordSenses.size(); i++) {
					Synset currentSense = wordSenses.get(i);
					String[] signature = currentSense.getGloss().split("[\\(\\)\\;\\\"\\?\\!\\.\\s]+");
					String[] overlapStringArray = computeOverlap(signature, context);
					int overlap = overlapStringArray.length;

					if(wordSensesForEachPOSDisplayList != null) { // for output
						wordSensesForEachPOSDisplayList.get(wordSensesForEachPOSDisplayList.size() - 1).getValue().add(new AbstractMap.SimpleEntry<String, String[]>(currentSense.getGloss(), overlapStringArray));
					}

					if (overlap > maxOverlap) {
						maxOverlap = overlap;
						bestSense = wordSenses.get(i);

						if(wordSensesForEachPOSDisplayList != null) { // for output
							bestSensePOSIndex[0] = wordSensesForEachPOSDisplayList.size() - 1;
							bestSenseIndex[0] = i;
						}

					}
				}
			}
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		return bestSense;
	}

	/**
	 * Compute overlap of two String array(signature and context)
	 * @param signature Word sense signature String array
	 * @param context Word context String array
	 * @return Overlap of two String array(signature and context) as String array
	 */
	private static String[] computeOverlap(String[] signature, String[] context) {
		Set<String> stopListSet = new HashSet<>(Arrays.asList(STOPLIST));
		Set<String> signatureSet = new HashSet<>(Arrays.asList(signature));
		Set<String> contextSet = new HashSet<>(Arrays.asList(context));
		Set<String> overlapSet = new HashSet<>(contextSet);
		overlapSet.removeAll(stopListSet);
		overlapSet.retainAll(signatureSet);
		return overlapSet.toArray(new String[0]);
	}

	/**
	 * Display the word overlap for each sense of the word in WordNet and the final chosen sense.
	 * This method should be called after simplifiedLesk() method.
	 */
	public void displayResult() {
		if (wordSensesForEachPOSDisplayList == null) {
			System.out.println("Please run simplifiedLesk() method first!");
		} else {
			// Word and Sentence
			System.out.println("The word is:");
			System.out.println("    " + word);
			System.out.println("The context sentence is:");
			System.out.println("    " + sentence);
			System.out.println();
			System.out.println();

			// Senses and overlap output
			for (int i = 0; i < wordSensesForEachPOSDisplayList.size(); i++) {
				System.out.println("For POS of " + wordSensesForEachPOSDisplayList.get(i).getKey());
				List<Map.Entry<String,String[]>> wordSensesGloss = wordSensesForEachPOSDisplayList.get(i).getValue();
				for (int j = 0; j < wordSensesGloss.size(); j++) {
					System.out.println("For sense " + (j + 1) + ":");
					System.out.println("    " + wordSensesGloss.get(j).getKey());
					String[] overlapStringArray = wordSensesGloss.get(j).getValue();
					System.out.println("The overlap word(s) number is " + overlapStringArray.length);
					System.out.print("The overlap word(s) is(are) as follow(if there is): ");
					System.out.print(Arrays.toString(overlapStringArray));
					System.out.println();
					System.out.println();
				}
			}

			// Best sense output
			System.out.println();
			System.out.println("Hence, the best sense is sense " + (bestSenseIndex[0] + 1) + " of POS of " + wordSensesForEachPOSDisplayList.get(bestSensePOSIndex[0]).getKey() + ":");
			Map.Entry<String,String[]> bestSenseGloss = wordSensesForEachPOSDisplayList.get(bestSensePOSIndex[0]).getValue().get(bestSenseIndex[0]);
			System.out.println("    " + bestSenseGloss.getKey());
			String[] overlapStringArray = bestSenseGloss.getValue();
			System.out.println("The overlap word(s) number is " + overlapStringArray.length);
			System.out.print("The overlap word(s) is(are) as follow(if there is): ");
			System.out.print(Arrays.toString(overlapStringArray));
			System.out.println();
			System.out.println();
		}
	}
}
