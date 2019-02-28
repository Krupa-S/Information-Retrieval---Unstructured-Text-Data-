import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

public class NBClassifier {

	private ArrayList<String> stopWords;
	private String[] trainingDocs;
	private ArrayList<Integer> trainingClasses;
	private int numClasses;
	private int[] classDocCounts; 
	private String[] classStrings; 
	private int[] classTokenCounts; 
	private HashMap<String,Double>[] condProb;
	private HashSet<String> vocabulary;
	private ArrayList<ArrayList<String>> docList;
	private Stemmer s;

	/**
	 * Build a Naive Bayes classifier using a training document set
	 * @param trainDataFolder the training document folder
	 */
	public NBClassifier(String trainDataFolder)
	{

		this.numClasses = 2;
		classDocCounts = new int[numClasses];
		classStrings = new String[numClasses];
		classTokenCounts = new int[numClasses];
		condProb = new HashMap[numClasses];
		vocabulary = new HashSet<String>();
		docList = new ArrayList<ArrayList<String>>();
		s = new Stemmer();
		trainingClasses = new ArrayList<Integer>();

		//Intinitialize classStrings and condProb
		for(int i=0;i<numClasses;i++){
			classStrings[i] = "";
			condProb[i] = new HashMap<String,Double>();
		}

		//process the training data folder to form doclist 
		preprocess(trainDataFolder);

		int count1 = 0;
		for (int q = 0; q < docList.size(); q++){

			for(int p=0;p<docList.get(q).size();p++){

				classDocCounts[trainingClasses.get(count1)]++;
				classStrings[q] += (docList.get(q).get(p) + " ");
				count1 = count1 + 1;

			}
		}


		for(int i=0;i<numClasses;i++){

			String[] tokens = classStrings[i].split("[ \\;]+");
			classTokenCounts[i] = tokens.length;

			for(String token:tokens){
				vocabulary.add(token);
				if(condProb[i].containsKey(token)){
					double count = condProb[i].get(token);
					condProb[i].put(token, count+1);
				}
				else
					condProb[i].put(token, 1.0);	
			}
		}



		for (int index1 = 0; index1 < numClasses; index1++) {
			Iterator<Map.Entry<String, Double>> iterator = condProb[index1].entrySet().iterator();
			int vSize = vocabulary.size();
			while (iterator.hasNext()) {
				Map.Entry<String, Double> entry = iterator.next();
				String token = entry.getKey();
				Double count = entry.getValue();
				Double prob = (count + 1) / (classTokenCounts[index1] + vSize);

				condProb[index1].put(token, prob);
			}
			
		}
	}

	/**
	 * Classify a test doc
	 * @param doc test doc
	 * @return class label
	 */
	public int classify(String doc){

		int label = 0;
		int vSize = vocabulary.size();
		double[] score = new double[numClasses];

		int trainingDocsSize =  (docList.get(0).size() + docList.get(1).size());
		for(int i=0;i<score.length;i++) {

			score[i]= Math.log(classDocCounts[i]*1.0/(trainingDocsSize));

		}
		String[] tokens = doc.split(" ");
		for(int i=0;i<numClasses;i++) {
			for(String token:tokens) {
				if(condProb[i].containsKey(token)){
					score[i] += Math.log(condProb[i].get(token));

				}
				else{
					score[i] += Math.log(1.0/(classTokenCounts[i]+vSize));
				}
			}
		}

		double maxScore = score[0];
		for(int i=1;i<score.length;i++) {
			if (score[i] > maxScore) {
				label = i;
				maxScore = score[i];
			}

		}

		return label;
	}


	/**
	 * Load the training documents
	 * @param trainDataFolder
	 */
	public void preprocess(String trainDataFolder)
	{

		File folder = new File(trainDataFolder);

		String[] listOfFiles = folder.list();
		int index = 0;

		for (int i = 0; i < listOfFiles.length; i++){
			File subFolder = new File("data/train/" + listOfFiles[i]);
			File[] filesSubFolder = subFolder.listFiles();
			ArrayList<String> subFolderDocs = new  ArrayList<String>();

			//Add subfolder(pos,neg)  files to arayList subFolderDocs
			for( int j=0; j < filesSubFolder.length; j++){

				Scanner scan;
				try {
					scan = new  Scanner(filesSubFolder[j]);

					String allLines = new String();
					while(scan.hasNextLine()) {
						allLines += scan.nextLine().toLowerCase();
					}

					subFolderDocs.add(allLines);

					//forming trainingClasses array based on training data
					if("neg".equals(listOfFiles[i])){
						trainingClasses.add(0);
					}else{

						trainingClasses.add(1); 
					}
					index = index + 1;

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}

			docList.add(subFolderDocs);
		}

	}

	/**
	 *  Classify a set of testing documents and report the accuracy
	 * @param testDataFolder fold that contains the testing documents
	 * @return classification accuracy
	 */
	public double classifyAll(String testDataFolder)
	{

		File folder = new File(testDataFolder);
		String[] listOfFiles = folder.list();
		int index = 0;

		ArrayList<Integer> classifyLabel = new  ArrayList<Integer>();
		ArrayList<Integer> actualClass = new ArrayList<Integer>();
		int totalDocs = 0;

		for (int i = 0; i < listOfFiles.length; i++){
			File subFolder = new File("data/test/" + listOfFiles[i]);
			File[] filesSubFolder = subFolder.listFiles();

			//Add subfolder(pos,neg)  files to arayList subFolderDocs
			totalDocs += filesSubFolder.length;

			for( int j=0; j < filesSubFolder.length; j++){

				Scanner scan;
				try {
					scan = new  Scanner(filesSubFolder[j]);

					String allLines = new String();
					while(scan.hasNextLine()) {
						allLines += scan.nextLine().toLowerCase();
					}

					//call classify method
					int classLabel = classify(allLines);

					//add label to arrayList
					classifyLabel.add(index, classLabel);

					//forming trainingClasses array based on training data
					if(listOfFiles[i].equals("neg")){
						actualClass.add(index,0);

					}else{
						actualClass.add(index, 1); 

					}
					index = index + 1;

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		//identify correctly classified instances
		int correctlyClassified = 0;
		for(int value = 0; value < classifyLabel.size(); value++){
			if(classifyLabel.get(value) == actualClass.get(value)){
				correctlyClassified++;        		 
			}
		}

		System.out.println("Correctly Classified Documents " + correctlyClassified + " out of " + totalDocs);
		//Accuracy of correctly identified documents
		Double accuracy = (correctlyClassified * 1.0)/totalDocs;

		return accuracy;
	}


	public static void main(String[] args)
	{		
		NBClassifier nb = new NBClassifier("data/train/");
		File filetest = new File("data/test/pos/cv899_16014.txt");
		Scanner scan;
		try {
			scan = new  Scanner(filetest);

			String allLines = new String();

			while(scan.hasNextLine()) {
				allLines += scan.nextLine().toLowerCase();
			}

			System.out.println("Class label: " + nb.classify(allLines));
			System.out.println("Accuracy of Classification: " + nb.classifyAll("data/test"));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
