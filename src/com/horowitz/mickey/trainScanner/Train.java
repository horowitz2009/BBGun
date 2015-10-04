package com.horowitz.mickey.trainScanner;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Train {
  private transient BufferedImage _fullImage;
  private String                  _fullImageFileName;

  private transient BufferedImage _scanImage;
  private String                  _scanImageFileName;

  private transient BufferedImage _additionalInfo;
  // private String _additionalInfoFileName;

  private transient BufferedImage _additionalInfoShort;
  private String                  _additionalInfoShortFileName;
  private String                  _additionalInfoFileName;

  private List<String>            _contractors = new ArrayList<String>();

  private boolean                 _idle;
  private long                    _timeToSendNext;

  public BufferedImage getAdditionalInfo() {
    return _additionalInfo;
  }

  public void setAdditionalInfo(BufferedImage additionalInfo) {
    _additionalInfo = additionalInfo;
  }

  public Train(BufferedImage fullImage, BufferedImage scanImage) {
    super();

    _fullImage = fullImage;
    _scanImage = scanImage;
  }

  public BufferedImage getFullImage() {
    return _fullImage;
  }

  public void setFullImage(BufferedImage fullImage) {
    _fullImage = fullImage;
  }

  public BufferedImage getScanImage() {
    return _scanImage;
  }

  public void setScanImage(BufferedImage scanImage) {
    _scanImage = scanImage;
  }

  public void setContractors(List<String> contractors) {
    _contractors = contractors;
  }

  public List<String> getContractors() {
    if (_contractors == null)
      _contractors = new ArrayList<>();
    return _contractors;
  }

  public BufferedImage getAdditionalInfoShort() {
    return _additionalInfoShort;
  }

  public void setAdditionalInfoShort(BufferedImage additionalInfoShort) {
    _additionalInfoShort = additionalInfoShort;
  }

  public void setIdle(boolean isIdle) {
    _idle = isIdle;
  }

  public boolean isIdle() {
    return _idle;
  }

  public void setTimeToSendNext(long time) {
    _timeToSendNext = time;
  }

  public long getTimeToSendNext() {
    return _timeToSendNext;
  }

  public String getFullImageFileName() {
    return _fullImageFileName;
  }

  public void setFullImageFilename(String fullImageFileName) {
    _fullImageFileName = fullImageFileName;
  }

  public String getAdditionalInfoFileName() {
    return _additionalInfoFileName;
  }

  public String getAdditionalInfoShortFileName() {
    return _additionalInfoShortFileName;
  }

  public void setAdditionalInfoShortFilename(String additionalInfoShortFileName) {
    _additionalInfoShortFileName = additionalInfoShortFileName;
  }

  public void setAdditionalInfoFilename(String additionalInfoFileName) {
    _additionalInfoFileName = additionalInfoFileName;
  }

  public String getScanImageFileName() {
    return _scanImageFileName;
  }

  public void setScanImageFilename(String scanImageFileName) {
    _scanImageFileName = scanImageFileName;
  }

  public void mergeWith(Train train) {
    if (getAdditionalInfoFileName() == null || getAdditionalInfoFileName().length() == 0) {
      setAdditionalInfoFilename(train.getAdditionalInfoFileName());
    }
    if (getAdditionalInfoShortFileName() == null || getAdditionalInfoShortFileName().length() == 0) {
      setAdditionalInfoShortFilename(train.getAdditionalInfoShortFileName());
    }
    if (getContractors() != null && getContractors().isEmpty()) {
      setContractors(train.getContractors());
    } else if (getContractors() != null && !getContractors().isEmpty()) {
      getContractors().addAll(train.getContractors());
    }
    setIdle(train.isIdle());
  }

}
