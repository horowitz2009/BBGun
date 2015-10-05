package com.horowitz.bigbusiness.model;

import com.horowitz.commons.ImageData;

public class BasicElement {
	private String _name;

	public BasicElement(String name) {
		super();
		_name = name;
	}

	private ImageData _labelImage;
	private ImageData _pictureImage;

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public ImageData getLabelImage() {
		return _labelImage;
	}

	public void setLabelImage(ImageData labelImage) {
		_labelImage = labelImage;
	}

	public ImageData getPictureImage() {
		return _pictureImage;
	}

	public void setPictureImage(ImageData pictureImage) {
		_pictureImage = pictureImage;
	}

}
