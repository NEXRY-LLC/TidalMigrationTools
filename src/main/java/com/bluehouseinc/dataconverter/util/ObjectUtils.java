package com.bluehouseinc.dataconverter.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;

public abstract class ObjectUtils {

	public static <K, V> Map<K, V> filterByKey(Map<K, V> map, Predicate<K> predicate) {
		return map.entrySet().stream().filter(entry -> predicate.test(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public static <K, V> Map<K, V> filterValueKey(Map<K, V> map, Predicate<V> predicate) {
		return map.entrySet().stream().filter(entry -> predicate.test(entry.getValue())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public static <J> List<J> toListByType(Collection<J> collection, Class<J> type) {
		return collection.stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
	}

	/**
	 * Returns every <J> object in the supplied collection , the Node is recursive
	 * returning all objects.
	 *
	 * @param <J>
	 * @param collection
	 * @param type
	 * @return
	 */
	public static <J extends BaseJobOrGroupObject> List<J> toFlatListByType(Collection<J> collection, Class<J> type) {
		return toFlatStream(collection).filter(type::isInstance).map(type::cast).collect(Collectors.toList());
	}

	/**
	 * Why does ? work but not J for the Class reference?
	 *
	 * @param <J>
	 * @param collection
	 * @param type
	 * @return
	 */
	public static <J extends BaseJobOrGroupObject> List<J> toFlatListByUnknownType(Collection<J> collection, Class<?> type) {
		return toFlatStream(collection).filter(f -> type.isInstance(f)).collect(Collectors.toList());
	}

	/**
	 * Returns a flat stream of all jobs / groups all levels deep.
	 *
	 * @param collection
	 * @return
	 */
	public static <E extends BaseJobOrGroupObject> Stream<E> toFlatStream(Collection<E> collection) {
		Stream<E> result = collection.stream().flatMap(parent -> {
			if ((parent.getChildren().size() > 0)) {
				return Stream.concat(Stream.of(parent), toFlatStream(parent.getChildren()));
			} else {
				return Stream.of(parent);
			}
		});

		return result;
	}

	public static void copyMatchingFields(Object fromObj, Object toObj) throws IllegalAccessException {
		copyMatchingFieldsSkipField(fromObj, toObj, null);
	}

	/**
	 * Use reflection to shallow copy simple type fields with matching names from
	 * one object to another
	 *
	 * @param fromObj the object to copy from
	 * @param toObj   the object to copy to
	 * @param idname  the field name to skip.
	 */
	public static void copyMatchingFieldsSkipField(Object fromObj, Object toObj, String idname) throws IllegalAccessException {
		if (fromObj == null || toObj == null)
			throw new NullPointerException("Source and destination objects must be non-null");

		List<Field> fromF = getAllKnownFields(fromObj);
		List<Field> toF = getAllKnownFields(toObj);

		for (Field f : fromF) {
			Field t = toF.stream().filter(m -> m.getType().equals(f.getType()) && m.getName().equals(f.getName())).findFirst().orElse(null);

			if ((t == null) || Modifier.isFinal(t.getModifiers())) {
				continue;
			} // Skip Final fields

			t.setAccessible(true);
			f.setAccessible(true);

			if (idname == null) {
				t.set(toObj, f.get(fromObj));
			} else if (t.getName().equals(idname)) {
				// Dont copy this field because we match
				continue;
			} else {
				t.set(toObj, f.get(fromObj));
			}
		}

	}

	public static List<Field> getAllKnownFields(Object obj) {
		List<Field> fields = new ArrayList<>();

		if (obj != null) {
			Class<?> cls = obj.getClass();

			while (cls != null) {
				Field[] dfields = cls.getDeclaredFields();
				for (Field dfield : dfields) {
					fields.add(dfield);
				}
				cls = cls.getSuperclass();
			}
		}
		return fields;
	}
	
	public static String replaceWithData(String name, String mapdata) {
		return replaceWithData(name,mapdata,false);
	}
	
	public static String replaceWithData(String name, String mapdata, boolean nullifnotfound) {
		if (mapdata == null || name == null) {
			return name;
		}

		List<String> data = new ArrayList<>();

		data = Arrays.asList(mapdata.split("\\s*,\\s*"));

		for (String f : data) {
			String[] nv = f.split("=");
			if (nv.length == 2) {
				if (nv[0].equalsIgnoreCase(name)) {
					//log.debug("doReplace [" + name + "] to Value[" + nv[1] + "]");
					return nv[1];

				}
			}
		}
		
		if(nullifnotfound) {
			return null;
		}
		
		return name;
	}
}
