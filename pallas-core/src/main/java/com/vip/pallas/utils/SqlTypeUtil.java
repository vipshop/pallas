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

package com.vip.pallas.utils;

import java.util.HashMap;
import java.util.Map;

public class SqlTypeUtil {

	private static Map<Integer, String> typeMap = new HashMap<>();

	static {
		typeMap.put(java.sql.Types.BIT, "BIT");
		typeMap.put(java.sql.Types.TINYINT, "TINYINT");
		typeMap.put(java.sql.Types.SMALLINT, "SMALLINT");
		typeMap.put(java.sql.Types.INTEGER, "INTEGER");
		typeMap.put(java.sql.Types.BIGINT, "BIGINT");
		typeMap.put(java.sql.Types.FLOAT, "FLOAT");
		typeMap.put(java.sql.Types.REAL, "REAL");
		typeMap.put(java.sql.Types.DOUBLE, "DOUBLE");
		typeMap.put(java.sql.Types.NUMERIC, "NUMERIC");
		typeMap.put(java.sql.Types.DECIMAL, "DECIMAL");
		typeMap.put(java.sql.Types.CHAR, "CHAR");
		typeMap.put(java.sql.Types.VARCHAR, "VARCHAR");
		typeMap.put(java.sql.Types.LONGVARCHAR, "LONGVARCHAR");
		typeMap.put(java.sql.Types.DATE, "DATE");
		typeMap.put(java.sql.Types.TIME, "TIME");
		typeMap.put(java.sql.Types.TIMESTAMP, "TIMESTAMP");
		typeMap.put(java.sql.Types.BINARY, "BINARY");
		typeMap.put(java.sql.Types.VARBINARY, "VARBINARY");
		typeMap.put(java.sql.Types.LONGVARBINARY, "LONGVARBINARY");
		typeMap.put(java.sql.Types.NULL, "NULL");
		typeMap.put(java.sql.Types.OTHER, "OTHER");

		typeMap.put(java.sql.Types.JAVA_OBJECT, "JAVA_OBJECT");
		typeMap.put(java.sql.Types.DISTINCT, "DISTINCT");
		typeMap.put(java.sql.Types.STRUCT, "STRUCT");
		typeMap.put(java.sql.Types.ARRAY, "ARRAY");
		typeMap.put(java.sql.Types.BLOB, "BLOB");

		typeMap.put(java.sql.Types.CLOB, "CLOB");
		typeMap.put(java.sql.Types.REF, "REF");
		typeMap.put(java.sql.Types.DATALINK, "DATALINK");
		typeMap.put(java.sql.Types.BOOLEAN, "BOOLEAN");
		typeMap.put(java.sql.Types.ROWID, "ROWID");
		typeMap.put(java.sql.Types.NCHAR, "NCHAR");
		typeMap.put(java.sql.Types.NVARCHAR, "NVARCHAR");
		typeMap.put(java.sql.Types.LONGNVARCHAR, "LONGNVARCHAR");

		typeMap.put(java.sql.Types.NCLOB, "NCLOB");
		typeMap.put(java.sql.Types.SQLXML, "SQLXML");
		typeMap.put(java.sql.Types.REF_CURSOR, "REF_CURSOR");
		typeMap.put(java.sql.Types.TIME_WITH_TIMEZONE, "TIME_WITH_TIMEZONE");
		typeMap.put(java.sql.Types.TIMESTAMP_WITH_TIMEZONE, "TIMESTAMP_WITH_TIMEZONE");

	}

	public static String getTypeByValue(int type) {
		String typeName = typeMap.get(type);
		if (typeName == null) {
			return "UNKNOWN";
		}
		return typeName;
	}

}