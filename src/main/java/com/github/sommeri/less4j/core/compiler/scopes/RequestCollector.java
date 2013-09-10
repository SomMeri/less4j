package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.compiler.scopes.LocalScope.VariableRequest;
import com.github.sommeri.less4j.utils.debugonly.DebugSysout;

//FIXME: clean up (!!!!)
@Deprecated
public class RequestCollector {

  private final List<VariableRequest> variableRequests = new ArrayList<VariableRequest>();
  private final List<String> mixinRequests = new ArrayList<String>();
  private boolean wasMixinRequest = false;

  public void printAllCollected() {
    //if (!variableRequests.isEmpty() || wasMixinRequest)
      DebugSysout.println(variableRequests + " " + wasMixinRequest+ " " +mixinRequests);
  }

  public void wasMixinRequest(String name) {
    wasMixinRequest = true;
    mixinRequests.add(name);
  }

  @Deprecated //FIXME: (!!) mis nomer, cares only about requests
  public boolean usedScope() {
    return wasMixinRequest;
  }

  public List<VariableRequest> getCollectedVariables() {
    return variableRequests;
  }

  public void add(VariableRequest variableRequest) {
    variableRequests.add(variableRequest);
  }
  
}

