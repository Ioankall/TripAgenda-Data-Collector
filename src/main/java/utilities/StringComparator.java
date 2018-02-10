package utilities;

/**
 * Created by Gika on 31/12/2016.
 */

import java.util.ArrayList;

public class StringComparator {
    /**
     * @param str1 *  @return lexical similarity value in the range [0,1]
     * @param str2
     * @return  */

    public static double compareStrings(String str1, String str2) {

        if (str1 == null || str2 == null) return 0;
        if (str1.isEmpty() || str2.isEmpty()) return 0;

        ArrayList pairs1 = wordLetterPairs(str1.toUpperCase());
        ArrayList pairs2 = wordLetterPairs(str2.toUpperCase());

        int intersection = 0;
        int union = pairs1.size() + pairs2.size();
        for (int i=0; i<pairs1.size(); i++) {
            Object pair1=pairs1.get(i);
            for(int j=0; j<pairs2.size(); j++) {
                Object pair2=pairs2.get(j);
                if (pair1.equals(pair2)) {
                    intersection++;
                    pairs2.remove(j);
                    break;
                }
            }
        }
        return (2.0*intersection)/union;
    }

    /** @return an ArrayList of 2-character Strings. */
    private static ArrayList wordLetterPairs(String str) {
        ArrayList allPairs = new ArrayList();
        // Tokenize the string and put the tokens/words into an array
        String[] words = str.split("\\s");
        // For each word
        for (int w=0; w < words.length; w++) {
            // Find the pairs of characters
            String[] pairsInWord = letterPairs(words[w]);
            for (int p=0; p < pairsInWord.length; p++) {
                allPairs.add(pairsInWord[p]);
            }
        }
        return allPairs;
    }

    /** @return an array of adjacent letter pairs contained in the input string */

    private static String[] letterPairs(String str) {
        int numPairs = str.length()-1;
        String[] pairs;
        if(str.length() == 0){
            pairs = new String[0];
            return pairs;
        }else{
            pairs = new String[numPairs];
            for (int i=0; i<numPairs; i++) {
                pairs[i] = str.substring(i,i+2);
            }
            return pairs;
        }
    }
}
