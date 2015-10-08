package com.horowitz.bigbusiness.model;

public interface Deserializable {
  
  void postDeserialize(Object[] transientObjects) throws Exception;
  
}
