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

package com.vip.pallas.console.exception;

import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.vip.pallas.console.vo.base.ErrorResponse;

@ControllerAdvice
public class ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

    @org.springframework.web.bind.annotation.ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handle(Exception e) {
        String message = e.getMessage();
        LOGGER.error(message, e);
        if (e instanceof ConstraintViolationException) {
            message = ((ConstraintViolationException) e).getConstraintViolations().stream()
                    .map(cv -> cv == null ? "null" : cv.getMessage()).collect(Collectors.joining(", "));
        } else if (e instanceof MethodArgumentNotValidException) {
            BindingResult br = ((MethodArgumentNotValidException) e).getBindingResult();
            StringBuilder sb = new StringBuilder();
            if (null != br) {
                for (ObjectError error : br.getAllErrors()) {
                    sb.append(error.getDefaultMessage());
                }
            }
            
            if (0 < sb.length()) {
                message = sb.toString();
            }
        }
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }
}