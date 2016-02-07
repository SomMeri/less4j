package com.github.sommeri.less4j.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JSONUtils {

  public static String getString(JsonObject object, String propertyName) {
    if (object.has(propertyName))
      return object.get(propertyName).getAsString();

    return null;
  }

  public static List<String> getStringList(JsonObject object, String propertyName) {
    if (object.has(propertyName))
      return toStringList(object.get(propertyName).getAsJsonArray());

    return null;
  }

  public static List<String> toStringList(JsonArray jsonArray) {
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < jsonArray.size(); i++) {
      result.add(jsonArray.get(i).isJsonNull() ? null : jsonArray.get(i).getAsString());
    }
    return result;
  }

}
