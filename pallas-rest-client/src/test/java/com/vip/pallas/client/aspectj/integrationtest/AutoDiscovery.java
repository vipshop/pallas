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

package com.vip.pallas.client.aspectj.integrationtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @startuml
 * group every 10 minutes
 * 		autonumber "<font color=blue><b>"
 * 		pallas_client->pallas_client: getClientIp
 * 		pallas_client->pallas_console: getEsClusterAndPsIpListByTokenAndIp
 * 		pallas_console->pallas_console: getEsClusterByToken
 * 		pallas_console->pallas_console: getPsIpListByIp(the same dc first)
 * 		pallas_console-->pallas_client: return esClusterAndPsIpList
 * 		pallas_client->pallas_client: updateCacheIfRequired
 * 		pallas_client->pallas_client: createNewRestClientIfRequired
 * 		autonumber stop
 * end
 * pallas_client->pallas_search: performRequestWithTokenAndDomain
 * pallas_search->ES_cluster: performRequest
 * ES_cluster-->pallas_search: return result
 * pallas_search-->pallas_client: return result
 * @enduml
 * @author chembo
 *
 */


public class AutoDiscovery {
public static void main(String[] args) {
	List<Integer> l = new ArrayList<>();
	l.add(0);
	l.add(1);
	l.add(2);
	l.add(3);
	l.add(4);
	l.add(5);
	Collections.rotate(l, 0);
	System.out.println(l);
}     
}