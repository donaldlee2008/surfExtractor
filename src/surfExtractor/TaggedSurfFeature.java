package surfExtractor;

import java.io.File;

import boofcv.struct.feature.SurfFeature;

/**
 * @author Hugo
 * 
 */
public class TaggedSurfFeature {
	/**
	 * Where the image that this Feature
	 */
	private String imageAbsPath;
	/**
	 * What folder the image is(class name)
	 */
	private String folder;
	/**
	 * The feature we are encapsulating with this class
	 */
	private SurfFeature theFeature;

	/**
	 * @param imageAbsPath - linked image absolute path
	 * @param folder - image folder/class name
	 * @param theFeature - SurfFeature object to be tagged
	 */
	public TaggedSurfFeature(String imageAbsPath, String folder, SurfFeature theFeature) {
		this.imageAbsPath = imageAbsPath;
		this.folder = folder;
		this.theFeature = theFeature;
	}

	/**
	 * @return linked image absolute path
	 */
	public String getImageAbsPath() {
		return this.imageAbsPath;
	}

	/**
	 * @return image class/folder name
	 */
	public String getFolderName() {
		return this.folder;
	}

	/**
	 * @return raw SurfFeature from BoofCV library
	 */
	public SurfFeature getFeature() {
		return this.theFeature;
	}

	/**
	 * @return File object of the linked Image
	 */
	public File getFile() {
		return new File(this.imageAbsPath);
	}
}
