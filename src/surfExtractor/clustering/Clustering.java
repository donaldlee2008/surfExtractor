package surfExtractor.clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import surfExtractor.image_set.Image;
import surfExtractor.image_set.ImageClass;
import surfExtractor.image_set.ImageSet;
import configuration.*;
import surfExtractor.misc.Utils;

import org.apache.log4j.Logger;

import surfExtractor.surf_extractor.TaggedSurfFeature;
import surfExtractor.user_interface.UserInterface;

/**
 * @author Hugo
 * 
 */
public class Clustering {

	private ImageSet imageSet;

	private ArrayList<TaggedSurfFeature> featurePool = new ArrayList<TaggedSurfFeature>();

	private ArrayList<Cluster> clusters = new ArrayList<Cluster>();

	private int clusterNum;

	private int iterations;

	private final static Logger LOGGER = Logger.getLogger(Clustering.class);


	/**
	 * @param is
	 *            - ImageSet's features being clustered
	 * @param clusterNum
	 *            - k-means algorithm cluster number
	 * @param iterations
	 *            - Number of time the algorithm will recalculate clusters'
	 *            centroids
	 */
	public Clustering(ImageSet is, int clusterNum, int iterations) {
		this.imageSet = is;
		this.clusterNum = clusterNum;
		this.iterations = iterations;

		LOGGER.info("Clustering ImageSet: " + is.getRelation() + " with " + clusterNum + " and " + iterations + " iterations");

	}

	/**
	 * Start clustering process
	 */
	public void cluster() {
		LOGGER.info("Filling feature pool");
		for (ImageClass ic : this.imageSet.getImageClasses()) {
			for (Image i : ic.getImages()) {
				for (TaggedSurfFeature f : i.getTaggedFeatures()) {
					this.featurePool.add(f);

				}
			}
		}
		LOGGER.info("Feature pool filled with " + this.featurePool.size() + " features");
		LOGGER.info("Generating random initial clusters");
		generateRandomClusters();
		LOGGER.info("Assigning first children");
		assignChildren();
		LOGGER.info("Iterations started");
		// debugClusters();
		for (int i = 0; i < iterations - 1; i++) {
			// UserInterface.featureClusteringProgress.setValue(i+1);
			recalculateCentroids();
			assignChildren();
			// debugClusters();
		}
	}

	/**
	 * Debug amount of childs(features) inside each cluster
	 */
	private void debugClusters() {
		for (Cluster c : this.clusters) {
			LOGGER.info("Cluster information: - " + c.getChildrenCount() + " childs inside. Centroid: ");
		}
		LOGGER.info("Clusters debugged");
	}

	private String centroidToString(double[] centroid) {
		String s = "";
		for (int i = 0; i < centroid.length; i++) {
			s += centroid[i];
			if (i != centroid.length - 1) {
				s += ", ";
			}
		}
		return s;
	}

	/**
	 * @return Cluster list
	 */
	public ArrayList<Cluster> getClusters() {
		return this.clusters;
	}

	/**
	 * Tells every cluster to recalculate it's centroid
	 */
	private void recalculateCentroids() {
		ArrayList<Double> deltas = new ArrayList<Double>();
		for (Cluster c : clusters) {
			deltas.add(c.recalculateCentroidWithDifference());
		}
		double min = deltas.get(0);
		double max = deltas.get(0);
		double sum = 0;
		for (Double d : deltas) {
			if (d < min)
				min = d;
			if (d > max)
				max = d;
			sum += d;
		}
		sum /= deltas.size();

		LOGGER.info("The minimum delta centroid was: " + min);
		LOGGER.info("The maximum delta centroid was: " + max);
		LOGGER.info("The average delta centroid was: " + sum);

	}

	/**
	 * Generates the initial random clusters
	 */
	private void generateRandomClusters() {
		Random rand = new Random(Integer.valueOf(Configuration.getConfiguration("random.seed")));
		for (int i = 0; i < this.clusterNum; i++) {
			this.clusters.add(new Cluster(this.featurePool.get(Math.round(rand.nextFloat() * this.featurePool.size())).getFeature().value));
		}
		int i = 1;
		for (Cluster c : this.clusters) {
			c.setId(i++);
		}
	}

	/**
	 * Assign a the closest cluster to each feature inside the feature pool
	 */
	private void assignChildren() {
		for (Cluster c : clusters) {
			c.clearChildren();
		}
		for (TaggedSurfFeature f : this.featurePool) {
			getClosestCluster(f).addChild(f);
		}
	}

	/**
	 * @param f
	 *            - the feature to seek closest cluster
	 * @return the closest cluster
	 */
	private Cluster getClosestCluster(TaggedSurfFeature f) {
		Cluster closestCluster = null;
		double closestDistance = 0;
		for (Cluster c : clusters) {
			if (closestCluster == null) {
				closestCluster = c;
				closestDistance = Utils.getFeatureDistanceFromCluster(f.getFeature(), c);
			} else {
				double d = Utils.getFeatureDistanceFromCluster(f.getFeature(), c);
				if (d < closestDistance) {
					closestCluster = c;
					closestDistance = d;
				}
			}
		}

		return closestCluster;
	}

	/**
	 * @return //TODO
	 */
	public int getClusterAmount() {
		return this.clusterNum;
	}

	public void saveClustersToFile(File path) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		for (Cluster c : this.clusters) {
			writer.println(c.getId() + ":" + centroidToString(c.getCentroid()));
		}

		writer.close();
	}

	public static ArrayList<Cluster> loadClustersFromFile(File path) throws IOException {
		ArrayList<Cluster> loadedClusters = new ArrayList<Cluster>();
		BufferedReader reader = new BufferedReader(new FileReader(path.getAbsolutePath()));
		String line = null;
		String[] parts;
		while((line = reader.readLine()) != null){
			if(line.equals("")) continue;
			parts = line.split(":");
			Cluster cluster = new Cluster(getCentroidFromString(parts[1]));
			cluster.setId(Integer.valueOf(parts[0]));
			loadedClusters.add(cluster);
		}
			
			
		return loadedClusters;
	}

	private static double[] getCentroidFromString(String string) {
		double[] centroid;
		String[] parts = string.split(", ");
		centroid = new double[parts.length];
		for(int i = 0; i < centroid.length; i++) {
			centroid[i] = Double.valueOf(parts[i]);
		}
		return centroid;
	}
}
