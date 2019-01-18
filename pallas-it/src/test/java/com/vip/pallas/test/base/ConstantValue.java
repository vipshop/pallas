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

package com.vip.pallas.test.base;

public interface ConstantValue {

    String dataSourceIp = "127.0.0.1";
    String dataSourcePort = "3306";
    String dataSourceDbName = "pallas_console";
    String dataSourceTableName = "user";
    String dataSourceUserName = "root";
    String dataSourcePassword = "123456";

//    String serverHttpAddress =  "127.0.0.1:9200,127.0.0.1:9210,127.0.0.1:9220";
//    String clintHttpAddress =  "127.0.0.1:9300,127.0.0.1:9310,127.0.0.1:9320";

    String serverHttpAddress =  "127.0.0.1:9200";
    String clintHttpAddress =  "127.0.0.1:9300";

    String accessiblePallasSearch = "pallas-search-master-SNAPSHOT";
}