package com.horowitz.mickey.data;

import java.util.Map;

public interface IContractAnalysys {

  public Map<String, Map<String, Need>> collectNeeds();

  public void calcALLNeeds();

}