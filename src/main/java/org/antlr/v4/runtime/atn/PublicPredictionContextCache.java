package org.antlr.v4.runtime.atn;

import java.util.Map;

public class PublicPredictionContextCache {
  private PredictionContextCache cache;

  public PublicPredictionContextCache(PredictionContextCache cache) {
    super();
    this.cache = cache;
  }

  public PredictionContextCache getCache() {
    return cache;
  }

  public void setCache(PredictionContextCache cache) {
    this.cache = cache;
  } 
  
  public Map<PredictionContext, PredictionContext> getMap() {
    return cache.cache;
  }
}
