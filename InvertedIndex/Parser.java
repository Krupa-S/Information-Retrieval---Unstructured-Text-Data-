import java.io.*;
import java.util.*;

public class Parser {
	private String[] myDocs;
	private File[] listOfFiles;
	String  thisLine = null;
	ArrayList stopWords = new ArrayList();

	int count = 0;    //Count for stopwords

	public Parser(String folderName) {
		try{
			/* Read Stop words */
			FileReader fr = new FileReader("stopwords.txt");
			BufferedReader br = new BufferedReader(fr);
			while ((thisLine = br.readLine()) != null){
				stopWords.add(thisLine.trim());

				count = count+1;
			}  
			Collections.sort(stopWords);

		}
		catch(IOException io)
		{
			io.printStackTrace();
		}

		File folder = new File(folderName);
		listOfFiles = folder.listFiles();
		myDocs = new String[listOfFiles.length];
		for(int i=0;i<listOfFiles.length;i++) {
			myDocs[i] = listOfFiles[i].getName();

		}

	}
	public int searchStopWord(String key) {
		int lo =0;
		int hi = (stopWords.size()) -1;

		while(lo <= hi) {
			int mid = lo + (hi - lo)/2;
			int result = key.compareTo((stopWords.get(mid)).toString());
			if(result <0) hi = mid - 1;
			else if(result >0) lo = mid + 1;
			else return mid;
		}
		return -1;
	}

	public ArrayList<String> parse(File fileName) throws IOException {
		String[] tokens = null;
		ArrayList<String> stemms = new ArrayList<String>();

		Scanner scan = new Scanner(fileName);
		String allLines = new String();

		while(scan.hasNextLine()) {
			allLines += scan.nextLine().toLowerCase();
		}

		tokens = allLines.split("[ ',\\;&.\\:?!$%()#\"\\/\\+\\-\\*[0-9]]+");
		//Stemming
		Stemmer st = new Stemmer();
		for(String token:tokens) {
			if(searchStopWord(token) <0) {
				st.add(token.toCharArray(), token.length());
				st.stem();

				stemms.add(st.toString());
				st = new Stemmer();
			}
		}

		return stemms;
	}

	public File getFile(int i) {
		return listOfFiles[i];
	}


	public String[] getDocuments() {
		String[] myDocs = new String[5];
		String docString = " ";

		for (int i=0;i<5;i++){
			try {
				File file = getFile(i);
				ArrayList<String> stemmed = parse(file);
				for(String st:stemmed) {
					docString = docString + st + " ";

				}
				myDocs[i] = docString;
				docString = " ";
			}
			catch(IOException ioe) {
			}

		}
		
		return myDocs;

	}
}