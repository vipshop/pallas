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

package com.vip.pallas.console.bean;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vip.pallas.console.filter.AuthenticationFilter;
import com.vip.pallas.console.filter.CorsFilter;

@Configuration
public class PallasFilterRegister {

	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
		FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter());
		registration.addUrlPatterns("/*");
		registration.setOrder(0);
		return registration;
	}
	
	@Bean
	public FilterRegistrationBean<AuthenticationFilter> authenticationFilterRegistration() {
		FilterRegistrationBean<AuthenticationFilter> registration = new FilterRegistrationBean<>(new AuthenticationFilter());
		registration.addUrlPatterns("*.json");
		registration.setOrder(1);
		return registration;
	}

}