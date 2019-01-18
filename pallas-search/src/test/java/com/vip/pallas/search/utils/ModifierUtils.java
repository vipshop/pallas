package com.vip.pallas.search.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ModifierUtils {

	public static void setFinalStatic(Field field, Object newValue, Object instance) throws Exception {
		field.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		
		if(Modifier.isFinal(field.getModifiers())){
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		} else if(Modifier.isVolatile(field.getModifiers())){ 
			modifiersField.setInt(field, field.getModifiers() | Modifier.VOLATILE);
		}
		field.set(instance, newValue);
	}
	
	
}
