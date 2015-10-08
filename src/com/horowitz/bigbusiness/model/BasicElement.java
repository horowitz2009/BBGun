package com.horowitz.bigbusiness.model;

import java.io.Serializable;

import com.horowitz.commons.ImageData;

public class BasicElement implements Cloneable, Serializable, Deserializable {

  private static final long serialVersionUID = -1189852183233999384L;

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

  @Override
  public Object clone() throws CloneNotSupportedException {
    // !!!!!!!!!!!!!!!!
    // _labelImage and _pictureImage refs will not be cloned
    // This is what I want (for now)
    // //////////////////////////////////////////////////////

    return super.clone();
  }

  @Override
	  public void postDeserialize(Object[] transientObjects) throws Exception {
  	  if (_labelImage != null)
  		  _labelImage.postDeserialize(transientObjects);
      if (_pictureImage != null)
        _pictureImage.postDeserialize(transientObjects);
	  }

}
