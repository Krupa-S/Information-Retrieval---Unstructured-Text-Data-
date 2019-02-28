import java.io.File;
import java.util.*;
import java.util.Map.Entry;


/**
 * 
 * Krupa Shah
 * Date : 03/10/2017
 */

public class InvertedIndex {
	//attributes
	private String[] myDocs;               //input docs
	private ArrayList<String> termList;    //dictionary
	private ArrayList<ArrayList<Integer>> docLists;

	//Constructor
	public InvertedIndex (String[] docs) { 
		myDocs = docs;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> docList; 

		for(int i = 0; i < myDocs.length; i++){
			String[] words = myDocs[i].split(" ");
			for(String word:words){
				if(!termList.contains(word)){
					termList.add(word);
					docList = new ArrayList<Integer>();
					docList.add(new Integer(i));
					docLists.add(docList);
				}  
				else{
					int index = termList.indexOf(word);
					docList = docLists.get(index);
					if(!docList.contains(new Integer(i))) {
						docList.add(new Integer(i));
						docLists.set(index, docList);
					}
				} 
			}
		}
	}
	/*
	 * Method to search 
	 * 
	 */
	public ArrayList<Integer> search(String query) {

		int index = termList.indexOf(query);
		if(index >=0) {
			return docLists.get(index);
		}
		return null;
	}

	public String toString() {
		String outputString = new String();
		for(int i=0;i<termList.size();i++) {
			outputString += String.format("%-15s", termList.get(i));

			ArrayList<Integer> docList = docLists.get(i);
			for(int j=0;j<docList.size();j++) {
				outputString += docList.get(j) + "\t";
			}
			outputString += "\n";

		}
		return outputString;
	}
	
	/*
	 *method to merge posting for more than one word search
	 * @param q1, q2 arraylist of posting of each search term
	 * @return merged arraylist
	 */
	public ArrayList<Integer> merge(ArrayList q1, ArrayList q2){
		ArrayList<Integer> result = new ArrayList<Integer>();
		int lengthQ1 = q1.size();
		int lengthQ2 = q2.size();
		int i = 0;
		int j = 0;
		while (i<lengthQ1 && j<lengthQ2){
			if( (q1.get(i)).equals(q2.get(j))){

				result.add((Integer) q1.get(i));
				i = i + 1;
				j = j + 1;
			}
			else if( (Integer) q1.get(i) < (Integer) q2.get(j)) { 
				i = i + 1;

			}
			else { 
				j = j + 1;

			}
		}
		return result;
	}

	public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
		Comparator<K> valueComparator = 
				new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = 
						map.get(k1).compareTo(map.get(k2));
				if (compare == 0) 
					return 1;
				else 
					return compare;
			}
		};

		Map<K, V> sortedByValues = 
				new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}
    /* Method to display the results for search query
     * @param result : result to display
     * 
     */
	public static void displayResult(ArrayList<Integer> result){
		File folder = new File("Lab1_Data");
		File[] listOfFiles = folder.listFiles();
		String[] fileNames = new String[listOfFiles.length];
		for(int i=0;i<listOfFiles.length;i++) {
			fileNames[i] = listOfFiles[i].getName();

		}
		System.out.println("Result for search query is:");
		if(result != null){
			for(Integer i:result) {
				System.out.println("Doc ID:\t" + i.intValue() + "\tDocument Name:\t" + fileNames[i.intValue()]);
			}
		}
		else{
			System.out.println("No Match Found!");
		}


	}


	public static void main(String[] args) {
		//a document collection: corpus
		Parser parser = new Parser("Lab1_Data");                 
		String docs[];
		File f = new File("C:\\");
		File[] list = f.listFiles();
		docs = parser.getDocuments();
		InvertedIndex matrix =  new InvertedIndex(docs);
		Stemmer st = new Stemmer();

		System.out.println(matrix);
		
	
		if(args.length == 1) {
			System.out.println("Query: " + args[0]);
			st.add(args[0].toCharArray(), args[0].length());
			st.stem();

			//st.stem();
			ArrayList<Integer> result = matrix.search(st.toString());
			displayResult(result);
		}
		else if(args.length == 3){
			String operator = args[1].trim();

			String word1 = args[0].trim();
			String word2 = args[2].trim();

			st.add(word1.toCharArray(), word1.length());
			st.stem();
			String stemmed_word1 = st.toString();
			ArrayList<Integer> posting1 = matrix.search(stemmed_word1);
			st.add(word2.toCharArray(), word2.length());
			st.stem();
			String stemmed_word2 = st.toString();

			ArrayList<Integer> posting2 = matrix.search(stemmed_word2);
			
			
			if("AND".equalsIgnoreCase(operator)){
				//Merge Algorithm
				ArrayList<Integer> result = new ArrayList<Integer>();
				if(posting1 != null && posting2 != null)
				{
					result  = matrix.merge(posting1, posting2);
				}
				else{
					result = null;
				}


				displayResult(result);
			}
			else if ("OR".equalsIgnoreCase(operator)){
				ArrayList<Integer> result_OR = new ArrayList<Integer>();
				int i = 0; 
				int j = 0;

				if(posting1 != null && posting2 !=null)
				{
					result_OR = posting1;
					int size1 = posting1.size()-1;
					int tempSize = posting1.size();

					for(int r=0; r<tempSize;r++){
						for(int s=0; s<posting2.size();s++){

							if(!(posting1.get(r).equals(posting2.get(s))))
							{
								result_OR.add(size1++, posting2.get(s));
							}

						}
					}
					Collections.sort(result_OR);

				}
				else
				{
					result_OR = null;
				}
				displayResult(result_OR);
			}
		}
		else if (args.length > 3){
			//query search for more than 3 words
			
			int j = 0;
			ArrayList <String> query_words = new ArrayList<String>();  // Arraylist of terms in search query
			TreeMap<String, ArrayList> results = new TreeMap<String, ArrayList>();   //treemap for final result storing term as key and value as posting
			TreeMap<String, Integer> lengthMap = new TreeMap<String, Integer>();    //treemap for term and length of posting
			ArrayList<Integer> posting = new ArrayList<Integer>();   //  posting for each keyword of search query
			
			
			//trim each keyword and add to query_words
			for (int i = 0; i<args.length; i=i+2){
				if(!"".equalsIgnoreCase(args[i].trim()))
				{
					query_words.add(args[i].trim());
					j++;
				}

			}
			
			//perform stemming of each keyword and search in matrix
			//add posting to treemap and its length to length map
			for(int p = 0; p<query_words.size(); p++){
				st.add(query_words.get(p).toCharArray(), query_words.get(p).length());
				st.stem();
				String stemmed_word = st.toString();
				posting = matrix.search(stemmed_word);
				results.put(query_words.get(p),posting);
				if(posting != null){
				lengthMap.put(query_words.get(p),posting.size());
				}

			}
			
			//display treemap of posting and its lengths
			System.out.println("Keywords and posting:");
			System.out.println(results.entrySet());
			System.out.println("\nKeywords and Length of posting:");
			System.out.println(lengthMap.entrySet());
            
			//sort length treemap for optimized search
			Map sortedMap = sortByValues(lengthMap);

			// Get a set of the entries on the sorted map
			Set set = sortedMap.entrySet();
            
			Iterator i = set.iterator();
			Map.Entry val = null;
			Map.Entry val2 = null;
			boolean check = false;
			ArrayList<Integer> result = new ArrayList<Integer>();
			ArrayList<Integer> a1 = new ArrayList<Integer>(); 
			ArrayList<Integer> a2 = new ArrayList<Integer>(); 
			
			System.out.println("=========================================================");
            System.out.println("Order of keywords combined:");
			
            //merge operations for more than 2 keywords
            while(i.hasNext()){
				if(!check){
					val = (Map.Entry)i.next();
					System.out.println(val.getKey());
					a1 = results.get(val.getKey());
					
				}
				
				if(i.hasNext()){
					val2 = (Map.Entry)i.next();
					System.out.println(val2.getKey());
					a2 = results.get(val2.getKey());
					
				}
				
				
				if(!val2.equals(null)){
					result = matrix.merge(a1,a2);   //method call to merge
					a1 = result;
					check  = true;
				}
			}
            
            for(int s = 0; s<results.size(); s++){
            	if(results.containsValue(null)){
            		result = null;
            	}
            }
            System.out.println("========================================================");
			displayResult(result);    //method call to display result
		}

	}
}


/* Test Cases:
 * One keyword query:
 * 1) bore 
 * 2) element
 * Two keyword query for AND : 
 * 1) bar AND fact
 * 2) thriller AND strip
 * 
 * Two keyword query for OR:
 * 1) bar OR fact
 * 
 * Three or more keyword query(only boolean AND):
 * 1) [movie=[0, 1, 2, 4], teen=[0, 2, 3], watch=[0, 2, 3, 4]]
 * 2) [attempt=[0, 2, 3, 4], bad=[0, 3, 4], film=[0, 2, 3, 4]]
 * 3) [cool=[0, 2], mold=[0, 3], touch=[0]]
 * 
 */
