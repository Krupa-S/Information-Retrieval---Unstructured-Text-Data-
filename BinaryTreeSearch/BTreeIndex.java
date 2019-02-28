import java.util.*;
/*
 * @author Krupa Shah
 * 
 */

public class BTreeIndex {
	String[] myDocs;
	BinaryTree termList;
	BTNode root;


	/**
	 * Construct binary search tree to store the term dictionary 
	 * @param docs List of input strings
	 * 
	 */
	public BTreeIndex(String[] docs)
	{
		//Tokenize the docs to get dictionary terms 
		myDocs=docs;
		termList= new BinaryTree();
		ArrayList<Integer> docList;
		for(int i=0;i<myDocs.length;i++){
			String[] words = myDocs[i].split("[ ',\\;&.\\:?!$%()#\"\\/\\+\\-\\*[0-9]]+");
			for(String word:words){
				docList=new ArrayList<Integer>();
				docList.add(i);


				//Create a tree node for token word
				BTNode treeNode = new BTNode(word.toLowerCase(), docList);

				//Insert created node to tree
				//If root node is already created, add tree node as child; else make tree node as root node
				if(root != null){
					//Add Child
					termList.add(root, treeNode);

				}
				else{
					//make treeNode as Root node 
					root = treeNode;					
				}

			} 

		}

	}

	/**
	 * Single keyword search
	 * @param query the query string
	 * @return doclists that contain the term
	 */
	public ArrayList<Integer> search(String query)
	{
		BTNode node = termList.search(root, query);
		if(node==null)
			return null;
		return node.docLists;
	}

	/**
	 * conjunctive query search
	 * @param query the set of query terms
	 * @return doclists that contain all the query terms
	 */
	public ArrayList<Integer> search(String[] query)
	{
		ArrayList<Integer> result = search(query[0]);
		int termId = 1;
		while(termId<query.length)
		{
			ArrayList<Integer> result1 = search(query[termId]);
			result = merge(result,result1);
			termId++;
		}		
		return result;
	}

	/**
	 * 
	 * @param wildcard the wildcard query, e.g., ho (so that home can be located)
	 * @return a list of ids of documents that contain terms matching the wild card
	 */
	public ArrayList<Integer> wildCardSearch(String wildcard)
	{
		//TO BE COMPLETED
		//ArrayList<String> wildcardResult = new ArrayList<String>();
		ArrayList<Integer> allDocs = new ArrayList<Integer>();
		ArrayList<Integer> tempDocs = new ArrayList<Integer>();
        
		//Search wildcard term in binary tree
		ArrayList<BTNode> wildcardResult = termList.wildCardSearch(root,wildcard.toLowerCase());

		//Get documents for searched terms 
		for(int j=0;j<wildcardResult.size();j++){
			System.out.println("Wilcard Term is found in: " + wildcardResult.get(j).term);
			tempDocs = search(wildcardResult.get(j).term);
			System.out.println("Documents for " + wildcardResult.get(j).term + ": " + tempDocs );
			
			//merge documents for the searched terms
			allDocs.removeAll(tempDocs);
			allDocs.addAll(tempDocs);

		}
        
		Collections.sort(allDocs);

		return allDocs;
	}


	private ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2)
	{
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
		int id1 = 0, id2=0;
		
		if (l1 == null || l2 == null ){
			return null;
		}
		while((!l1.isEmpty() || !l2.isEmpty()) && id1<l1.size() && id2<l2.size()){
			if(l1.get(id1).intValue()==l2.get(id2).intValue()){
				mergedList.add(l1.get(id1));
				id1++;
				id2++;
			}
			else if(l1.get(id1)<l2.get(id2))
				id1++;
			else
				id2++;
		}
		return mergedList;
	}


	/**
	 * Test cases
	 * @param args commandline input
	 */
	public static void main(String[] args)
	{
		String[] docs = {"text warehousing over big data",
						 "dimensional data warehouse over big data",
						 "nlp before text mining",
						 "nlp before text classification"};
		//TO BE COMPLETED with testcases

		//Create object of binarytreeIndex
		System.out.println("====================Created Binary Tree Index============================\n");
		BTreeIndex b1=new BTreeIndex(docs);
		BinaryTree bt = new BinaryTree();
		System.out.println();
		System.out.println("====================Dictionary Term and Documents============================\n");
		bt.printInOrder(b1.root);
		System.out.println();


		//Search query of single search term
		String[] searchQuery={"before"};
		System.out.println("==================== Single Search Term Query ================================\n");
		System.out.println("Search Term=> " + searchQuery[0]);

		ArrayList<Integer> queryDocs = b1.search(searchQuery);
		if (queryDocs != null &&  !(queryDocs.isEmpty())){
			System.out.println("Documents for search term=> " + queryDocs);
		}
		else{
			System.out.println("Search term not found");	
		}
		System.out.println();


		//Conjunctive Query search
		String[] searchQueryConjunctive={"nlp","mining"};
		System.out.println("==================== Counjunctive Search Query ================================\n");
		System.out.println( "Conjunctive Search Query=> " +  searchQueryConjunctive[0]+" AND "+searchQueryConjunctive[1]);

		ArrayList<Integer> queryDocsConjunctive = b1.search(searchQueryConjunctive);
		if (queryDocsConjunctive != null &&  !(queryDocsConjunctive.isEmpty())){
			System.out.println("Documents for conjunctive search query=> " + queryDocsConjunctive);
		}
		else{
			System.out.println("Search Query not found");	
		}
		System.out.println();

		//Wildcard search query 

		System.out.println("==================================== Wildcard Search (searchterm*i) =========================\n");
		ArrayList<Integer> searchQueryWildsearch =  b1.wildCardSearch("war");
		if (searchQueryWildsearch != null &&  !(searchQueryWildsearch.isEmpty()) ){
			System.out.println("Documents containing wildcard search term=> "+ searchQueryWildsearch);
		}else{
			System.out.println("No documents found for wildcard search query");	
		}
		System.out.println();



	}
}