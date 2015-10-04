/*
 * $Id: ImageManager.java,v 1.23 2006/07/11 15:20:54 HRISTOV Exp $
 *
 * Copyright (c) 2005
 */
package com.horowitz.mickey;

import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * This is class for accessing and caching images. Use
 * {@link ImageManager#getImage(String) getImage(String name)} to load an image
 * from <code>common</code>. Use {@link ImageManager#getImage(Class, String)
 * getImage(Class requesterClass, String name)} to load an image from
 * <code>images</code> subfolder to package of specified
 * <code>requesterClass</code>.
 *
 * @author Zhivko Hristov
 * @version $Revision: 1.23 $, $Date: 2006/07/11 15:20:54 $
 */
public final class ImageManager {
	private static final Map<String, ImageIcon>	imagesMap	= new Hashtable<String, ImageIcon>();

	/**
	 * The name of the 'Copy' image
	 */
	public static final String	                COPY	    = "copy.png";	                      //$NON-NLS-1$

	/**
	 * Creates a new object of type ImageManager.
	 */
	private ImageManager() {
		super();
	}

	/**
	 * Returns an <code>ImageIcon</code> with specified <code>name</code> and
	 * specified location, given by the <code>requesterClass</code>. The location
	 * is made of the package folder of the requesterClass, followed by the value
	 * of <code>IMAGES_FOLDER</code> constant.
	 *
	 * @param requesterClass
	 *          the class requesting for the image
	 * @param name
	 *          the name the image is searched by
	 *
	 * @return the image if found or <code>null</code> if not found.
	 */
	public static ImageIcon getImage(String name) {
		ImageIcon image = getFromCache(name);
		if (image == null) {
			image = createImage(ImageManager.class, name);
			cacheImage(image, name);
		}

		//    System.out.println("getImage(" //$NON-NLS-1$
		//      + name + ") - return value=" + image); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return image;
	}

	/**
	 * TODO
	 *
	 * @param name
	 *          TODO
	 *
	 * @return TODO
	 */
	public static URL getImageURL(String name) {
		final URL url = ImageManager.class.getClassLoader().getResource(name);
		return url;
	}

	/**
	 * Tries to find an image with specified <code>name</code>. If already cached,
	 * returns the image, else returns <code>null</code>.
	 *
	 * @param name
	 *          the name of the image being searched in cache.
	 *
	 * @return the image if found or <code>null</code> if not found
	 */
	private static ImageIcon getFromCache(String name) {
		return imagesMap.get(name);
	}

	/**
	 * Saves the image in the hash map. If the image is <code>null</code>, it
	 * doesn't save anything.
	 *
	 * @param image
	 *          the image
	 * @param name
	 *          the name of the image, by which the image can be located later
	 */
	private static void cacheImage(ImageIcon image, String name) {
		if (image != null) {
			// System.out.println("cacheImage(ImageIcon image=" + image
			//        + ", String name=" //$NON-NLS-1$ //$NON-NLS-2$
			//        + name + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			imagesMap.put(name, image);
		}
	}

	/**
	 * Tries to load <code>ImageIcon</code> by given <code>requesterClass</code>
	 * and <code>name</code>.
	 *
	 * @param requesterClass
	 *          the class requesting for the image
	 * @param name
	 *          the name the image is searched by
	 *
	 * @return ImageIcon the image loaded or null
	 */
	private static ImageIcon createImage(Class requesterClass, String name) {
		ImageIcon result = null;

		final URL url = requesterClass.getClassLoader().getResource(name);
		if (url != null) {
			result = new ImageIcon(url);
		}
		return result;
	}
}
