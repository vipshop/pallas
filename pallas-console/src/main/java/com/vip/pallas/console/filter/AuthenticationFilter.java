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

package com.vip.pallas.console.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vip.pallas.console.vo.base.ErrorResponse;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.utils.JsonUtil;

@Component
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
    
    @Autowired
    private AbstractAuthProcessor authProcessor;
    
    private static AbstractAuthProcessor staticAuthProcessor;
    
    @PostConstruct
    public void init(){
    	staticAuthProcessor = authProcessor;
    }
    
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			staticAuthProcessor.process((HttpServletRequest)request, (HttpServletResponse)response);
			chain.doFilter(request, response);
		} catch (BusinessLevelException e) {
			LOGGER.error(e.toString(), e);
			PrintWriter writer = response.getWriter();
			try {
				writer.print(JsonUtil.toJson(new ErrorResponse(e.getErrorCode(), e.getMessage())));
			} catch (Exception e1) {
				LOGGER.error(e.toString(), e);
			} finally {
				writer.flush();
			}
		}
	}

    @Override
    public void destroy() {
    }
}