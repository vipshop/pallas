/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

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
