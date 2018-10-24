package com.tcc.core.utils;

import com.google.gson.Gson;

/**
 * description
 * 
 * @author xuyi 2018年10月10日
 */
public class JsonUtils {

	private static Gson gson = new Gson();

	public static <T> T parseJson(String json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}

	public static String toJson(Object obj) {
		return gson.toJson(obj);
	}
}
