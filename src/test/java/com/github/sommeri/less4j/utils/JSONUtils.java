package com.github.sommeri.less4j.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

  public static String getString(JSONObject object, String propertyName) throws JSONException {
    if (object.has(propertyName))
      return object.getString(propertyName);

    return null;
  }

  public static List<String> getStringList(JSONObject object, String propertyName) throws JSONException {
    if (object.has(propertyName))
      return toStringList(object.getJSONArray(propertyName));

    return null;
  }

  public static List<String> toStringList(JSONArray jsonArray) throws JSONException {
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < jsonArray.length(); i++) {
      result.add(jsonArray.isNull(i) ? null : jsonArray.getString(i));
    }
    return result;
  }

}
