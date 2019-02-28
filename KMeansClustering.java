 import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;




public class KMeansClustering
{
	int numDocs;
	int numClusters;
	int vSize;
	Doc2[] docList;
	HashMap<String, Integer> termIdMap;
	
	ArrayList<Doc2>[] clusters;
	Doc2[] centroids;
	public Clustering2(int numC)
	{
		numClusters = numC;
		clusters = new ArrayList[numClusters];
		centroids = new Doc2[numClusters];
		termIdMap = new HashMap<String, Integer>();
	}
	
	/**
	 * Load the documents to build the vector representations
	 * @param docs
	 */
	public void preprocess(String[] docs){
		numDocs = docs.length;
		docList = new Doc2[numDocs];
		int termId = 0;
		
		//collect the term counts, build term id map and the idf counts
		int docId = 0;
		for(String doc:docs){
			String[] tokens = doc.split(" ");
			Doc2 docObj = new Doc2(docId);
			for(String token: tokens){
				if(!termIdMap.containsKey(token)){
					termIdMap.put(token, termId);
					docObj.termIds.add(termId);
					docObj.termWeights.add(1.0);					
					termId++;
				}
				else{
					Integer tid = termIdMap.get(token);
					int index = docObj.termIds.indexOf(tid);
					if (index >0){
						double tw = docObj.termWeights.get(index);
						docObj.termWeights.add(index, tw+1);
					}
					else{
						docObj.termIds.add(termIdMap.get(token));
						docObj.termWeights.add(1.0);
					}
				}
			}
			docList[docId] = docObj;
			docId++;
		}
		vSize = termId;
		
		//compute the tf-idf weights of documents
		for(Doc2 doc: docList){
			double docLength = 0;
			double[] termVec = new double[vSize];
			for(int i=0;i<doc.termIds.size();i++){
				Integer tid = doc.termIds.get(i);
				double tfidf = (1+Math.log(doc.termWeights.get(i)));//Math.log(numDocs/idfMap.get(tid));				
				doc.termWeights.set(i, tfidf);
				docLength += Math.pow(tfidf, 2);
			}
			
			//normalize the doc vector			
			for(int i=0;i<doc.termIds.size();i++){
				double tw = doc.termWeights.get(i);
				doc.termWeights.set(i, tw/docLength);
				
				termVec[doc.termIds.get(i)] = tw/docLength;
			}
			doc.termVec = termVec;
			
		}
	}
	
	/**
	 * Cluster the documents
	 * For kmeans clustering, use the first and the ninth documents as the initial centroids
	 */
	public void cluster()
  {
    HashMap<Doc2, ArrayList<Doc2>> clusters = new HashMap();
    ArrayList<Doc2> centroids = new ArrayList();
    Double[] tempCosineSimVector = new Double[2];
    Boolean stop = Boolean.valueOf(false);
    
    ArrayList<Doc2> clusterDocs1 = new ArrayList();
    ArrayList<Doc2> clusterDocs2 = new ArrayList();
    
    centroids.add(docList[0]);
    centroids.add(docList[8]);
    
    clusters.put((Doc2)centroids.get(0), clusterDocs1);
    
    clusters.put((Doc2)centroids.get(1), clusterDocs2);
    
    for (int i = 1; i < docList.length - 1; i++)
    {
      for (int j = 0; j < centroids.size(); j++)
      {
        tempCosineSimVector[j] = cosineSimilarity(docList[i], (Doc2)centroids.get(j));
        System.out.println("qqq: " + i + "sim: " + tempCosineSimVector[j]);
      }
      
      int centroidIndex = 0;
      Double maxSimilarity = tempCosineSimVector[0];
      

      for (int p = 0; p < tempCosineSimVector.length; p++) {
        if (tempCosineSimVector[p].doubleValue() > maxSimilarity.doubleValue()) {
          maxSimilarity = tempCosineSimVector[p];
          centroidIndex = p;
        }
      }
      
      if (centroidIndex == 0)
      {
        clusterDocs1.add(docList[i]);
      }
      else
      {
        clusterDocs2.add(docList[i]);
      }
    }
    

    clusters.put((Doc2)centroids.get(0), clusterDocs1);
    clusters.put((Doc2)centroids.get(1), clusterDocs2);
    
    printClusters(clusters, centroids);

      Double totalWeight = 0.0;
      Double totalDocWeight = 0.0;
      
      Double total = 0.0;
          	
    	//recompute centroid
    	
    	for(int x = 0; x < clusters.size(); x++){
    		ArrayList<Doc2> tempDocs = clusters.get(centroids.get(x));
    		ArrayList<Doc2> newCentroid = new ArrayList();
    		double[] myCentroid = new double[tempDocs.get(0).termVec.length];
    		
    		for(Doc2 d : tempDocs){
    			
    			for(int y = 0; y < d.termVec.length; y++){
    				myCentroid[y] += d.termVec[y];
    			}
    		}
    		
    		total = totalWeight / tempDocs.size();
    		System.out.println(" total "  +total);
    	}
    	
    	//Reassign documents to cluster
    	
    	clusters.put((Doc2)centroids.get(0), clusterDocs1);
        
        clusters.put((Doc2)centroids.get(1), clusterDocs2);
        

        for (int i = 1; i < docList.length - 1; i++)
        {
          for (int j = 0; j < centroids.size(); j++)
          {
            tempCosineSimVector[j] = cosineSimilarity(docList[i], (Doc2)centroids.get(j));
            System.out.println("qqq: " + i + "sim: " + tempCosineSimVector[j]);
          }
          
          int centroidIndex = 0;
          Double maxSimilarity = tempCosineSimVector[0];
          

          for (int p = 0; p < tempCosineSimVector.length; p++) {
            if (tempCosineSimVector[p].doubleValue() > maxSimilarity.doubleValue()) {
              maxSimilarity = tempCosineSimVector[p];
              centroidIndex = p;
            }
          }
          

          if (centroidIndex == 0)
          {
            clusterDocs1.add(docList[i]);
          }
          else
          {
            clusterDocs2.add(docList[i]);
          }
        }
        

        clusters.put((Doc2)centroids.get(0), clusterDocs1);
        clusters.put((Doc2)centroids.get(1), clusterDocs2);
        
        printClusters(clusters, centroids);
	
  }
  
	/**
	 * Compute cosine similarity between the documents
	 */
  public Double cosineSimilarity(Doc2 listDoc, Doc2 centroidDoc)
  {
	  double num=0.0;
		double freq1=0;
		double freq2=0;

		Double dotProduct = 0.0;
		double d1 = 0.0;
		double d2 = 0.0;
		Double cosineSimilarity = 0.0;

		for(int j = 0; j < listDoc.termIds.size(); j++){
			if(listDoc.termWeights.get(j) != 0 && listDoc.termWeights != null){
			if(centroidDoc.termIds.contains(listDoc.termIds.get(j))){
				freq1 = listDoc.termWeights.get(j);
				freq2 = centroidDoc.termWeights.get(j);
				dotProduct += freq1 * freq2;
				d1 += Math.pow(freq1, 2);
				d2 += Math.pow(freq2, 2);

			}else{

				freq1 = listDoc.termWeights.get(j);
				d1 += Math.pow(freq1, 2);
			}
			}
		}

		for(int k = 0; k < centroidDoc.termIds.size(); k++){
			if(!listDoc.termIds.contains(centroidDoc.termIds.get(k))){
				freq2 = centroidDoc.termWeights.get(k);
				d2 += Math.pow(freq2, 2); 
			}
		}

		cosineSimilarity = (double) (dotProduct / (double) (Math.sqrt(d1) * Math.sqrt(d2)));
		return cosineSimilarity;

  }
  

  public void printClusters(HashMap<Doc2, ArrayList<Doc2>> cluster, ArrayList<Doc2> centroids)
  {
	  for (int i = 0; i < cluster.size(); i++) {
	      ArrayList<Doc2> docIds = (ArrayList)cluster.get(centroids.get(i));
	      System.out.println("Cluster " + i + " :");
	      for (Doc2 doc : docIds) {
	        System.out.println(" " + doc.docId + " ");
	      }
	    }
  }
  
  public static void main(String[] args){
		String[] docs = {"hot chocolate cocoa beans",
				 "cocoa ghana africa",
				 "beans harvest ghana",
				 "cocoa butter",
				 "butter truffles",
				 "sweet chocolate can",
				 "brazil sweet sugar can",
				 "suger can brazil",
				 "sweet cake icing",
				 "cake black forest"
				};
		Clustering2 c = new Clustering2(2);
		c.preprocess(docs);
		System.out.println("Vector space representation:");
		for(int i=0;i<c.docList.length;i++){
			System.out.println(c.docList[i]);
		}
		
		c.cluster();
		
		/*
		 * Expected result:
		 * Cluster: 0
			0	1	2	3	4	
		   Cluster: 1
			5	6	7	8	9	
		 */
	}
}

/**
* 
* Document id class that contains the document id and the term weight in tf-idf
*/
class Doc2{
	int docId;
	ArrayList<Integer> termIds;
	ArrayList<Double> termWeights;
	double[] termVec;
	public Doc2(){
		
	}
	public Doc2(int id){
		docId = id;
		termIds = new ArrayList<Integer>();
		termWeights = new ArrayList<Double>();
	}
	public void setTermVec(double[] vec){
		termVec = vec;
	}
 
	public String toString()    
	{
		String docString = "[";
		for(int i=0;i<termVec.length;i++){
			docString += termVec[i] + ",";
		}
		return docString+"]";
	}
	
}
