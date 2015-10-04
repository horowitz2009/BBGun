package com.horowitz.mickey.data;

import java.util.Map;

public class ContractAnalysisMin extends ContractAnalysis implements IContractAnalysys {

  public Map<String, Map<String, Need>> collectNeeds() {
    return super.collectCurrentNeeds();
  }
}
