package com.horowitz.mickey.data;

import java.util.Map;

public class ContractAnalysisAll extends ContractAnalysis implements IContractAnalysys {

  
  /* (non-Javadoc)
   * @see com.horowitz.mickey.data.IContractAnalysys#collectNeeds()
   */
  @Override
  public Map<String, Map<String, Need>> collectNeeds() {
    return super.collectCurrentNeedsALL();
  }
}
