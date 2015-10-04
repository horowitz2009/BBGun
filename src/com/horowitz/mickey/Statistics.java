package com.horowitz.mickey;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Statistics {
  private int  _expressTrainCount;
  private int  _freightTrainCount;
  private int  _refreshCount;
  private int  _refreshTimeoutCount;
  private int  _refreshMandatoryCount;
  private int  _refreshStuckCount;
  private long _startTime;
  private long _lastExpressTime;
  private long _lastActivityTime;
  private long _lastFreightTime;
  private long _lastRefreshTime;
  private long _lastTrainTime;

  private long _brownLetter;
  private long _redLetter;
  private long _whiteLetter;

  public Statistics() {
    super();
    reset();
  }

  public void reset() {
    _expressTrainCount = 0;
    _freightTrainCount = 0;
    _refreshCount = 0;
    _lastActivityTime = _startTime = System.currentTimeMillis();
    // _lastActivityTime +=1 * 30 * 60 * 1000;
    _lastTrainTime = _lastFreightTime = _lastExpressTime = _lastRefreshTime = _lastActivityTime;
    _refreshMandatoryCount = _refreshStuckCount = _refreshTimeoutCount = _refreshCount;
    _brownLetter = _redLetter = _whiteLetter = 0;
  }

  public void registerExpress() {
    _expressTrainCount++;
    _lastActivityTime = _lastExpressTime = System.currentTimeMillis();
  }

  public void registerTrain(boolean isExpress) {
    if (isExpress)
      registerExpress();
    else
      registerFreight();
    _lastActivityTime = _lastTrainTime = System.currentTimeMillis();
  }

  public void registerRefresh() {
    _refreshCount++;
    _lastActivityTime = _lastRefreshTime = System.currentTimeMillis();
  }

  public void registerTimeOutRefresh() {
    _refreshTimeoutCount++;
    registerRefresh();
  }

  public void registerBrownLetter() {
    _brownLetter++;
  }

  public long getBrownLetter() {
    return _brownLetter;
  }

  public long getRedLetter() {
    return _redLetter;
  }

  public long getWhiteLetter() {
    return _whiteLetter;
  }

  public void registerRedLetter() {
    _redLetter++;
  }

  public void registerWhiteLetter() {
    _whiteLetter++;
  }

  public void registerMandatoryRefresh() {
    _refreshMandatoryCount++;
    registerRefresh();
  }

  public void registerStuckRefresh() {
    _refreshStuckCount++;
    registerRefresh();
  }

  public void registerFreight() {
    _freightTrainCount++;
    _lastActivityTime = _lastFreightTime = System.currentTimeMillis();
  }

  public double getAverageTrainTime() {
    long time = _lastActivityTime - _startTime;
    double t = (double) time / 3600000;
    int cnt = _freightTrainCount + _expressTrainCount;
    double value = 0;
    if (t != 0)
      value = cnt / t;
    return value;
  }

  public double getAverageFreightTime() {
    long time = _lastActivityTime - _startTime;
    double t = (double) time / 3600000;
    double value = 0;
    if (t != 0)
      value = _freightTrainCount / t;
    return value;
  }

  public double getAverageRefreshTime() {
    long time = _lastActivityTime - _startTime;
    double t = (double) time / 3600000;
    double value = 0;
    if (t != 0)
      value = _refreshCount / t;
    return value;
  }

  public double getAverageExpressTime() {
    long time = _lastActivityTime - _startTime;
    double t = (double) time / 3600000;
    double value = 0;
    if (t != 0)
      value = _expressTrainCount / t;
    return value;
  }

  public String getAverageExpressTimeAsString() {
    return getAvgAsString(getAverageExpressTime());
  }

  public String getAverageFreightTimeAsString() {
    return getAvgAsString(getAverageFreightTime());
  }

  public String getAverageTrainTimeAsString() {
    return getAvgAsString(getAverageTrainTime());
  }

  public String getAverageRefreshTimeAsString() {
    return getAvgAsString(getAverageRefreshTime());
  }

  public int getExpressTrainCount() {
    return _expressTrainCount;
  }

  public int getFreightTrainCount() {
    return _freightTrainCount;
  }

  public int getTotalTrainCount() {
    return _expressTrainCount + _freightTrainCount;
  }

  public int getRefreshCount() {
    return _refreshCount;
  }

  public int getRefreshTimeoutCount() {
    return _refreshTimeoutCount;
  }

  public int getRefreshMandatoryCount() {
    return _refreshMandatoryCount;
  }

  public int getRefreshStuckCount() {
    return _refreshStuckCount;
  }

  public long getStartTime() {
    return _startTime;
  }

  public long getLastTrainTime() {
    return _lastTrainTime;
  }

  public long getLastFreightTime() {
    return _lastFreightTime;
  }

  public long getLastExpressTime() {
    return _lastExpressTime;
  }

  public long getLastRefreshTime() {
    return _lastRefreshTime;
  }

  public long getLastActivityTime() {
    return _lastExpressTime;
  }

  public String getLastActivityTimeAsString() {
    SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
    return sdf.format(new Date(_lastActivityTime));
  }

  public String getStartedTimeAsString() {
    SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
    return sdf.format(new Date(_startTime));
  }

  private String getAvgAsString(double value) {
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits(2);
    nf.setMinimumFractionDigits(0);
    return nf.format(value);
  }

  public void updateTime() {
    _lastActivityTime = System.currentTimeMillis();
  }

}