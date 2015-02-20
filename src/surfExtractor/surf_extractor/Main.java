package surfExtractor.surf_extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import surfExtractor.image_set.ImageSet;
import configuration.*;

import org.apache.log4j.Logger;

import surfExtractor.clustering.Cluster;
import surfExtractor.clustering.Clustering;
import surfExtractor.exporter.Exporter;
import surfExtractor.bow_classifier.Bow;

/**
 * @author Hugo
 * 
 *         Main class
 */
public class Main {
	private final static Logger LOGGER = Logger.getLogger(Main.class);

	/**
	 * @param args
	 *            - Run configuration parameters
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) {
		Configuration.addNewValidParameter("imageset.path", true);
		Configuration.addNewValidParameter("imageset.relation", true);
		Configuration.addNewValidParameter("random.seed", false);
		Configuration.addNewValidParameter("arff.relation", false);
		Configuration.addNewValidParameter("arff.path", true);
		Configuration.addNewValidParameter("kmeans.iteration", true);
		Configuration.addNewValidParameter("kmeans.kvalue", true);
		Configuration.addNewValidParameter("cluster.save_path", false);
		Configuration.addNewValidParameter("cluster.load_path", false);

		Configuration.addNewValidParameter("surf.radius", false);
		Configuration.addNewValidParameter("surf.threshold", false);
		Configuration.addNewValidParameter("surf.ignoreborder", false);
		Configuration.addNewValidParameter("surf.strictrule", false);
		Configuration.addNewValidParameter("surf.maxfeaturesperscale", false);
		Configuration.addNewValidParameter("surf.initialsamplerate", false);
		Configuration.addNewValidParameter("surf.initialsize", false);
		Configuration.addNewValidParameter("surf.numberscalesperoctave", false);
		Configuration.addNewValidParameter("surf.numberofoctaves", false);

		Configuration.addNewValidCommand("auto.imageset.relation");
		Configuration.addNewValidCommand("auto.arff.relation");
		Configuration.addNewValidCommand("auto.file.name");

		Configuration.setConfiguration("random.seed", "1");

		Configuration.readFromRunArgs(args);

		try {
			// Check if we have enought parameters to start
			Configuration.verifyArgs();
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}

		/*
		 * Moved gui to another project if (Configuration.getCommand("gui") ==
		 * true) { // Open UserInterface, and wait input from user
		 * UserInterface.initialize(); UserInterface.start();
		 * UserInterface.hold(); UserInterface.setConfiguration(); }
		 */

		// Print loaded configuration
		Configuration.debugParameters();

		// Time extraction process started
		long start = System.currentTimeMillis();

		try {
			Main m = new Main();
			m.run();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		long duration = System.currentTimeMillis() - start;

		// Goes through the process of ending extraction
		// Moved interface to another project
		// UserInterface.done();
		LOGGER.info("Duration of the process: " + (duration / 1000) + " seconds.");
	}

	public Main() {

	}

	/**
	 * Main class object
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */

	public void run() {

		// Load images from ImageSet
		ImageSet is;
		try {
			is = new ImageSet(Configuration.getConfiguration("imageset.path"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}
		if (Configuration.getCommand("auto.imageset.relation")) {
			is.setRelation(Configuration.getConfiguration("imageset.relation") + "-" + Configuration.getConfiguration("surf.radius") + "-" + Configuration.getConfiguration("surf.threshold") + "-" + Configuration.getConfiguration("surf.maxfeaturesperscale") + "-" + Configuration.getConfiguration("surf.initialsamplerate") + "-" + Configuration.getConfiguration("surf.initialsize") + "-" + Configuration.getConfiguration("surf.numberscalesperoctave") + "-" + Configuration.getConfiguration("surf.numberofoctaves"));
			LOGGER.info("Setting imageset.relation automatically");
		} else {
			LOGGER.info("Setting image.relation manually");
			is.setRelation(Configuration.getConfiguration("imageset.relation"));
		}

		// Create clustering object
		Clustering clustering = new Clustering(is, Integer.valueOf(Configuration.getConfiguration("kmeans.kvalue")), Integer.valueOf(Configuration.getConfiguration("kmeans.iteration")));
		clustering.setSeed(Integer.parseInt(Configuration.getConfiguration("random.seed")));

		// Set Dataset 'name'
		if (Configuration.getCommand("auto.arff.relation")) {
			LOGGER.info("Setting arff.relation automatically");
			Configuration.setConfiguration("arff.relation", Configuration.getConfiguration("imageset.relation") + "-" + Configuration.getConfiguration("surf.radius") + "-" + Configuration.getConfiguration("surf.threshold") + "-" + Configuration.getConfiguration("surf.maxfeaturesperscale") + "-" + Configuration.getConfiguration("surf.initialsamplerate") + "-" + Configuration.getConfiguration("surf.initialsize") + "-" + Configuration.getConfiguration("surf.numberscalesperoctave") + "-" + Configuration.getConfiguration("surf.numberofoctaves"));
		} else {
			LOGGER.info("arff.relation is set manually");
		}

		// Create SURF Feature extractor objects
		SurfExtractor surfExtractor = new SurfExtractor();

		Configuration.addNewValidParameter("surf.radius", false);
		Configuration.addNewValidParameter("surf.threshold", false);
		Configuration.addNewValidParameter("surf.ignoreborder", false);
		Configuration.addNewValidParameter("surf.strictrule", false);
		Configuration.addNewValidParameter("surf.maxfeaturesperscale", false);
		Configuration.addNewValidParameter("surf.initialsamplerate", false);
		Configuration.addNewValidParameter("surf.initialsize", false);
		Configuration.addNewValidParameter("surf.numberscalesperoctave", false);
		Configuration.addNewValidParameter("surf.numberofoctaves", false);

		if (Configuration.getConfiguration("surf.radius") != null)
		surfExtractor.setRadius(Integer.valueOf(Configuration.getConfiguration("surf.radius")));
		
		if (Configuration.getConfiguration("surf.threshold") != null)
		surfExtractor.setThreshold(Float.valueOf(Configuration.getConfiguration("surf.threshold")));
		
		if (Configuration.getConfiguration("surf.ignoreborder") != null)
		surfExtractor.setIgnoreBorder(Integer.valueOf(Configuration.getConfiguration("surf.ignoreborder")));
		
		if (Configuration.getConfiguration("surf.strictrule") != null)
		surfExtractor.setStrictRule(Boolean.valueOf(Configuration.getConfiguration("surf.strictrule")));
		
		if (Configuration.getConfiguration("surf.maxfeaturesperscale") != null)
		surfExtractor.setMaxFeaturesPerScale(Integer.valueOf(Configuration.getConfiguration("surf.maxfeaturesperscale")));
		
		if (Configuration.getConfiguration("surf.initialsamplerate") != null)
		surfExtractor.setInitialSampleRate(Integer.valueOf(Configuration.getConfiguration("surf.initialsamplerate")));
		
		if (Configuration.getConfiguration("surf.initialsize") != null)
		surfExtractor.setInitialSize(Integer.valueOf(Configuration.getConfiguration("surf.initialsize")));
		
		if (Configuration.getConfiguration("surf.numberscalesperoctave") != null)
		surfExtractor.setNumberScalesPerOctave(Integer.valueOf(Configuration.getConfiguration("surf.numberscalesperoctave")));
		
		if (Configuration.getConfiguration("surf.numberofoctaves") != null)
		surfExtractor.setNumberOfOctaves(Integer.valueOf(Configuration.getConfiguration("surf.numberofoctaves")));

		// Load images from ImageSet
		is.getImageClasses();

		// Use surfExtractor to extract SURF features
		surfExtractor.extractImageSet(is);

		// Debug feature number for each image
		/*
		 * for (ImageClass ic : is.getImageClasses()) { for (Image i :
		 * ic.getImages()) { LOGGER.info(i.getFeatures().size() +
		 * " SURF features detected for: " + i.getFile().getName()); } }
		 */

		// Cluster all features
		if (Configuration.getConfiguration("cluster.load_path") != null) {
			try {
				clustering.loadClustersFromFile(new File(Configuration.getConfiguration("cluster.load_path")));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		} else {
			clustering.cluster();
		}

		// Export clusters
		if (Configuration.getConfiguration("cluster.save_path") != null) {
			LOGGER.info("Saving clusters to file");
			try {
				clustering.saveClustersToFile(new File(Configuration.getConfiguration("clusters.save_path")));
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
				return;
			}
		}

		// Return final clusters
		ArrayList<Cluster> featureCluster = clustering.getClusters();

		// Load Bag Of Words classifier
		Bow bow = new Bow(is, featureCluster);

		// Compute frequency histograms
		bow.computeHistograms();

		// Return frequency histograms
		// ArrayList<Histogram> h = bow.getHistograms();

		// Debug histograms
		/*
		 * LOGGER.info("Debugging image histograms"); for (Histogram hh : h) {
		 * LOGGER.info("Histogram: " + histogramToString(hh)); }
		 */

		// Write experimental arff
		Exporter exporter = new Exporter(is, bow);
		exporter.addCommentLine("Starting parameter debugging");

		HashMap<String, String> config = Configuration.getConfig();
		Iterator it = config.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			exporter.addCommentLine(pair.getKey() + " -> " + pair.getValue());
		}
		try {
			if (Configuration.getCommand("auto.file.name")) {
				LOGGER.info("Automatically setting file name");
				exporter.generateArffFile(Configuration.getConfiguration("arff.path") + Configuration.getConfiguration("imageset.relation") + "-" + Configuration.getConfiguration("surf.radius") + "-" + Configuration.getConfiguration("surf.threshold") + "-" + Configuration.getConfiguration("surf.maxfeaturesperscale") + "-" + Configuration.getConfiguration("surf.initialsamplerate") + "-" + Configuration.getConfiguration("surf.initialsize") + "-" + Configuration.getConfiguration("surf.numberscalesperoctave") + "-" + Configuration.getConfiguration("surf.numberofoctaves") + ".arff");
			} else {
				LOGGER.info("Using manual file name");
				exporter.generateArffFile(Configuration.getConfiguration("arff.path"));
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 
	 * @param imagesetPath
	 * @param kmeansK
	 * @param kmeansIterations
	 * @param arffRelation
	 * @param arffPath
	 */
	public void generateArff(String imagesetPath, int kmeansK, int kmeansIterations, String arffRelation, String arffPath) {

		// Load images from ImageSet
		ImageSet is = null;
		try {
			is = new ImageSet(imagesetPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		// Create clustering object
		Clustering clustering = new Clustering(is, kmeansK, kmeansIterations);

		// Set Dataset 'name'
		is.setRelation(arffRelation);

		// Create SURF Feature extractor objects
		SurfExtractor surfExtractor = new SurfExtractor();

		// Load images from ImageSet
		is.getImageClasses();

		// Use surfExtractor to extract SURF features
		surfExtractor.extractImageSet(is);

		// Debug feature number for each image
		/*
		 * for (ImageClass ic : is.getImageClasses()) { for (Image i :
		 * ic.getImages()) { LOGGER.info(i.getFeatures().size() +
		 * " SURF features detected for: " + i.getFile().getName()); } }
		 */

		// Cluster all features
		clustering.cluster();

		// Return final clusters
		ArrayList<Cluster> featureCluster = clustering.getClusters();

		// Load Bag Of Words classifier
		Bow bow = new Bow(is, featureCluster);

		// Compute frequency histograms
		bow.computeHistograms();

		// Return frequency histograms
		// ArrayList<Histogram> h = bow.getHistograms();

		// Debug histograms
		/*
		 * LOGGER.info("Debugging image histograms"); for (Histogram hh : h) {
		 * LOGGER.info("Histogram: " + histogramToString(hh)); }
		 */

		// Write experimental arff
		Exporter exporter = new Exporter(is, bow);
		exporter.addCommentLine("Starting parameter debugging");
		while (Configuration.getConfig().values().iterator().hasNext()) {
			String s = Configuration.getConfig().values().iterator().next();
			exporter.addCommentLine(s);
		}
		try {
			if (Configuration.getCommand("auto.file.name")) {
				LOGGER.info("Automatically setting file name");
				exporter.generateArffFile(arffPath + Configuration.getConfiguration("imageset.relation") + "-" + Configuration.getConfiguration("surf.radius") + "-" + Configuration.getConfiguration("surf.threshold") + "-" + Configuration.getConfiguration("surf.maxfeaturesperscale") + "-" + Configuration.getConfiguration("surf.initialsamplerate") + "-" + Configuration.getConfiguration("surf.initialsize") + "-" + Configuration.getConfiguration("surf.numberscalesperoctave") + "-" + Configuration.getConfiguration("surf.numberofoctaves"));
			} else {
				LOGGER.info("Using manual file name");
				exporter.generateArffFile(arffPath);
			}

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
	}
}
