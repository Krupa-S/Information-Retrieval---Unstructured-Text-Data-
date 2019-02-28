import java.util.ArrayList;
/**
 * 
 * Krupa Shah
 */

public class PositionalIndex {
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<Doc>> docLists;

	/**
	 * Construct a positional index 
	 * @param docs List of input strings or file names
	 * 
	 */
	public PositionalIndex(String[] docs)
	{
		myDocs = docs;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<Doc>>();
		ArrayList<Doc> docList;
		for(int i=0;i<myDocs.length;i++){
			String[] tokens = myDocs[i].split(" ");
			String eachWord;
			for(int j=0;j<tokens.length;j++){
				eachWord = tokens[j];
				if(!termList.contains(eachWord)){
					termList.add(eachWord);
					docList = new ArrayList<Doc>();
					Doc docObj = new Doc(i,j);
					docList.add(docObj);
					docLists.add(docList);
				}
				else{ 
					//If eachWord already exists in termList  
					int index = termList.indexOf(eachWord);
					docList = docLists.get(index);
					int k=0;                  //To track the position for updating  docList
					boolean flag = false;    // 
					
					//List of posting is searched to find document id.
					//On finding the match, the position to document object 
					//and docList is set with document Object at position k
					for(Doc docObj:docList)
					{
						if(docObj.docId==i)
						{
							docObj.insertPosition(j);
							docList.set(k, docObj);
							flag = true;
							break;
						}
						k++;
					}
					//if no match, add a new document id along with the position number
					if(!flag)
					{
						Doc docObj = new Doc(i,j);
						docList.add(docObj);
					}
				}
			}
		}
	}

	/**
	 * Return the string representation of a positional index
	 */
	public String toString()
	{
		String matrixString = new String();
		ArrayList<Doc> docList;
		for(int i=0;i<termList.size();i++){
			matrixString += String.format("%-15s", termList.get(i));
			docList = docLists.get(i);
			for(int j=0;j<docList.size();j++)
			{
				matrixString += docList.get(j)+ "\t";
			}
			matrixString += "\n";
		}
		return matrixString;
	}

	/**
	 * 
	 * @param post1 first postings
	 * @param post2 second postings
	 * @return merged result of two postings
	 */
	public ArrayList<Doc> intersect(ArrayList<Doc> post1, ArrayList<Doc> post2)
	{
		ArrayList<Doc> mergedList = new ArrayList<Doc>();
		int docid1=0, docid2=0;
		while(docid1<post1.size()&&docid2<post2.size()){
			//if both terms appear in the same document
			if(post1.get(docid1).docId==post2.get(docid2).docId){
				//get the position information for both terms
				ArrayList<Integer> pp1 = post1.get(docid1).positionList;
				ArrayList<Integer> pp2 = post2.get(docid2).positionList;
				//ArrayList<Integer> mergePositionList = new ArrayList<Integer>();
				Doc merge_doid = null;
				int pid1 =0, pid2=0;
				while(pid1<pp1.size()){
					boolean match = false;
					while(pid2<pp2.size()){
						//if the two terms appear together, we find a match
						if(Math.abs(pp1.get(pid1)-pp2.get(pid2))<=1){
							match = true;

							//Create Doc object for  doc ID and postion found in doc ID 
							merge_doid = new Doc(post1.get(docid1).docId,pp2.get(pid2));
							System.out.println("Postion 2:" + pp2.get(pid2) );
							System.out.println(" Doc ID: " + post1.get(docid1).docId );
							//mergedList..add(post1.get(docid1).docId);
							break;
						}
						else if(pp2.get(pid2)>pp1.get(pid1))
							break;
						pid2++;
					}
					if(match) //if a match if found, the search for the current document can be stopped
						break;
					pid1++;
				}
				System.out.println(merge_doid);
				//Adding Doc to Merged List
				mergedList.add(merge_doid);

				docid1++;
				docid2++;
			}
			else if(post1.get(docid1).docId<post2.get(docid2).docId)
				docid1++;
			else
				docid2++;
		}	
		return mergedList;

	}

	/**
	 * 
	 * @param query a phrase query that consists of any number of terms in the sequential order
	 * @return ids of documents that contain the phrase
	 */
	public ArrayList<Doc> phraseQuery(String[] query)
	{
		ArrayList<Doc> tempMergedList = new ArrayList<Doc>(); 
		ArrayList<Doc> tempMergedList2 = new ArrayList<Doc>();
		ArrayList<Doc> post2 = new ArrayList<Doc>();
		ArrayList<Doc> post1 = new ArrayList<Doc>();

		System.out.println("indexof" + termList.indexOf(query[1]));
		if(query.length >= 2 && termList.indexOf(query[0]) != -1 && termList.indexOf(query[1]) != -1 ){
		post1 = docLists.get(termList.indexOf(query[0]));
		post2 = docLists.get(termList.indexOf(query[1]));
		}
		else{
			return null;
		}
		tempMergedList = intersect(post1,post2);
		int x=2;
		int size = query.length;
		while(x < size){
			if(termList.indexOf(query[x]) != -1){
			ArrayList<Doc> temppost = docLists.get(termList.indexOf(query[x++]));
			tempMergedList2 = intersect(tempMergedList,temppost);
			tempMergedList = tempMergedList2;
			}else{
				return null;
			}
		}
		return tempMergedList;
	}


	public static void main(String[] args)
	{
		String[] docs = {"data warehousing over big data",
						"dimensional data warehouse over big data",
						"nlp before text mining",
						"nlp before text classification"};

		PositionalIndex pi = new PositionalIndex(docs);
		System.out.print(pi);

		//Phrase search
		ArrayList<Doc> result = pi.phraseQuery(args);
		System.out.println("============== Query Result==================");
		if(result!=null)
		{
			for(Doc i:result)
			{
				System.out.println("Document: " + docs[i.docId]);
				System.out.println("DocID:<Position Of Last Word>- " + i);
			}
		}
		else{
			System.out.println("No match!");
		}

		
	}
}

/**
 * 
 * Document class that contains the document id and the position list
 */
class Doc{
	int docId;
	ArrayList<Integer> positionList;
	public Doc(int did)
	{
		docId = did;
		positionList = new ArrayList<Integer>();
	}
	public Doc(int did, int position)
	{
		docId = did;
		positionList = new ArrayList<Integer>();
		positionList.add(new Integer(position));
	}


	public void insertPosition(int position)
	{
		positionList.add(new Integer(position));
	}

	public String toString()
	{
		String docIdString = ""+docId + ":<";
		for(Integer pos:positionList)
			docIdString += pos + ",";
		docIdString = docIdString.substring(0,docIdString.length()-1) + ">";
		return docIdString;		
	}
}
