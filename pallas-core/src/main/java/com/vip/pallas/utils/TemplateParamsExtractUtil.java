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

import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.service.impl.PerformanceScriptServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vip.pallas.mybatis.entity.SearchTemplate.MACRO_END_FLAG;
import static com.vip.pallas.mybatis.entity.SearchTemplate.MACRO_START_FLAG;

/**
 * Created by owen.li on 27/5/2017
 *
 * 该类用于抽取index search template 中所用到的mustache 的所有带{{ }} 的变量，采用递归搜索
 * 主要规则是：
 * 1. 遇到 {{# 或者 {{^ 开头，则搜索该变量的 }} 位置，截取后递归子串，结果汇总赋予该变量并加到变量列表
 * 2. 遇到 {{ 直接带变量名，则认为是简单变量，直接增加到变量列表
 * 3. 遇到 特定字符串 "toJson" 变量就认为里面包的是数组变量
 * 4. 遇到 特定字符串 如 "list"，"and","or" 变量就认为里面包的是数组变量，例如 a.b.list.c 则认为list 是一个数组，而里面迭代的是一个object，其有c属性
 *
 * 遇到找不到闭标签位置，则把该变量加上"错误："放到参数列表，整个递归过程结束
 */
public class TemplateParamsExtractUtil {

    private static final Logger logger = LoggerFactory.getLogger(TemplateParamsExtractUtil.class);

    private static final Pattern PATTERN = Pattern.compile(MACRO_START_FLAG + "[\\w]*" + MACRO_END_FLAG);


    public static Map<String, Object> getParams(String originContent, List<SearchTemplate> allFiles) {
        String content = "{\n\"template\":\"" + renderMacrosThenFormat(originContent, allFiles) + "\"\n}";
        Map<String, Object> params = new LinkedHashMap<>();
        walk(content, 0, content.length()-1, "", params, null);
        return params;

    }

    public static String renderESTemplateBody(String originContent, List<SearchTemplate> allFiles) {
        return "{\n\"template\":\"" + renderMacrosThenFormat(originContent, allFiles) + "\"\n}";
    }

    public static String renderMacrosThenFormat(String originContent, List<SearchTemplate> allFiles) {
        String content = originContent;

        //替换 macro 如果有的话
        if(content.contains(MACRO_START_FLAG)) {
            Matcher matchers = PATTERN.matcher(content);
            Map<String, String> usingMacros = new HashMap<>();
            while (matchers.find()) {
                String flag = matchers.group();
                allFiles.stream()
                        .filter((SearchTemplate x) -> getMacroFlag(x.getTemplateName()).equals(flag))
                        .findAny()
                        .ifPresent((SearchTemplate x) -> usingMacros.put(flag, x.getContent()));
            }
            if(!usingMacros.isEmpty()) {
                for(Map.Entry<String, String> en : usingMacros.entrySet()) {
                    String macroValue = en.getValue();
                    content = content.replaceAll(en.getKey(), macroValue != null ? macroValue : "");
                }
            }
        }

        // 替换非法字符
        content = content.replaceAll("\n"," ")
                .replaceAll("\r"," ")
                .replaceAll("\t"," ")
                //.replaceAll("\\{\\{\\{}", "{ {{")
                .replaceAll("\"", "\\\\\"");

        Map<String, Object> params = new LinkedHashMap<>();
        Set<String> nestedParams = new HashSet<>();
        walk(content, 0, content.length()-1, "", params, nestedParams);

        if (nestedParams.isEmpty()) {
        	return content;
        }
        
        for (String nested : nestedParams) {
            logger.info("found nested parameter: {}",  nested);
            if (nested.contains(".list.")) {
                content = content.replaceAll(nested, nested.substring(nested.lastIndexOf(".list.")+6));
            } else if (nested.contains(".and.")) {
                content = content.replaceAll(nested, nested.substring(nested.lastIndexOf(".and.")+5));
            } else if (nested.contains(".or.")) {
                content = content.replaceAll(nested, nested.substring(nested.lastIndexOf(".or.")+4));
            }

        }
        return content;
    }

    private static String getMacroFlag(String templateId) {
        return MACRO_START_FLAG + templateId + MACRO_END_FLAG;
    }

    static void walk(String content, int start, int end, String pTag, Map<String, Object> paramMap, Set<String> nestedParameters) {
        int tagStart = content.indexOf("{{", start);

        while(tagStart != -1 && tagStart < end) {
            int tagStartOffset = content.indexOf("}}", tagStart);
            String var = content.substring(tagStart+2, tagStartOffset);
            // System.out.println("found variable:" + var);

            if (var.startsWith("#") || var.startsWith("^")) {
                //一个变量scope的开始
                var = var.substring(1);
                int tagEnd = findEndTag(content, var, tagStartOffset);
                if (tagEnd == -1) {
                    //遇到一个没有闭标签的tag，认为模板文件发生错误
                    setValue(paramMap, nestedParameters, "错误:" + var, null);
                    return;
                }
                if ("toJson".equals(var)) {
                    String tagNameToBeListed = content.substring(tagStartOffset+2, tagEnd);
                    setValue(paramMap, nestedParameters, tagNameToBeListed, new Object[0]);

                } else {
                    setValue(paramMap, nestedParameters, var, null);
                    walk(content, tagStartOffset + 2, tagEnd, var, paramMap, nestedParameters);
                }
                tagStart = content.indexOf("{{", tagEnd+3);

            } else if (var.startsWith("!") || var.startsWith(">")) {
                //skip:  !是注释， >是分块

            } else {
                //直接就是一个变量, 标记一下直接开始扫下一个变量
                setValue(paramMap, nestedParameters, var, null);
                tagStart = content.indexOf("{{", tagStartOffset);
            }
        }
    }

    private static int findEndTag(String content, String tagName, int start) {
        String endTagName = "{{/" + tagName + "}}";
        int offStart = start, offEnd = start, anotherSameTag;

        while(true) {
            offEnd = content.indexOf(endTagName, offEnd);
            anotherSameTag = content.indexOf("{{#" + tagName + "}}", offStart);
            if(anotherSameTag == -1) {
                anotherSameTag = content.indexOf("{{^" + tagName + "}}", offStart);
            }
            if (anotherSameTag == -1 || anotherSameTag > offEnd) {
                return offEnd;
            }
            offStart = anotherSameTag + 3;
            offEnd = offEnd + 3;
        }
    }

    /**
     * 为参数列表设置值，如果name的模式是a.b.c = 1 则参数列表结果是
     * a : {
     *     b : {
     *         c : 1
     *     }
     * }
     * 如果name的模式是a.b.list.c = 1 则参数列表结果是
     * a : {
     *     b : {
     *         list : [
     *           {c:1}
     *         ]
     *     }
     * }
     * @param paramMap
     * @param name
     * @param value
     */
    @SuppressWarnings("unchecked")
    private static void setValue(Map<String, Object> paramMap, Set<String> nestedParameters, String name, Object value) {
        if("null".equals(name) || ".".equals(name)) {
            return;
        }
        String[] names = name.split("\\.");
        if (names.length == 1) {
            paramMap.put(name, value);
            return;
        }
        Map<String, Object> cursor = paramMap;
        boolean foundNested = false;
        for (int i = 0; i < names.length-1; i++) {
            String key = names[i];
            Object v = cursor.get(key);
            if (v != null) {
                if (v instanceof Map) {
                    cursor = (Map<String, Object>) v;
                } else if (v instanceof List) {
                    foundNested = true;
                    cursor = ((List<Map<String, Object>>) v).get(0);
                }

            } else if ("list".equals(key) || "and".equals(key) || "or".equals(key)){
                foundNested = true;
                List<Map<String, Object>> list = new ArrayList<>();
                list.add(new LinkedHashMap<>());
                cursor.put(key, list);
                cursor = ((List<Map<String, Object>>) cursor.get(key)).get(0);
            } else {
                cursor.put(key, new LinkedHashMap<String, Object>());
                cursor = (Map<String, Object>) cursor.get(key);
            }
        }
        if(names.length > 0){
            cursor.put(names[names.length-1], value);
        }
        if (foundNested && nestedParameters != null) {
            nestedParameters.add(name);
        }
    }

//    public static void main(String[] args) {
//        String s = "{{#merchandise}}\n" +
//                "        {\n" +
//                "          \"bool\":{\n" +
//                "            \"should\":[\n" +
//                "\t\t\t  {{#merchandise.list}}\n" +
//                "\t\t\t  {\n" +
//                "                \"bool\":{\n" +
//                "                  \"must\":[\n" +
//                "                    {\n" +
//                "                      \"term\":{\n" + "{{.}}" +
//                "                        \"merchandise_v_spu_id\":\"{{merchandise.list.merchandise_v_spu_id}}\"\n" +
//                "                      }\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                      \"term\":{\n" +
//                "                        \"merchandise_sn\":\"{{merchandise.list.merchandise_sn}}\"\n" +
//                "                      }\n" +
//                "                    }\n" +
//                "                  ]\n" +
//                "                }\n" +
//                "              }\n" +
//                "\t\t\t  {{^merchandise.list.last}},{{/merchandise.list.last}}\t\t\t  \n" +
//                "\t\t\t  {{/merchandise.list}}\n" +
//                "            ]\n" +
//                "          }\n" +
//                "        },\n" +
//                "\t\t{{/merchandise}}";
//        Map<String, Object> m = new LinkedHashMap<>();
//        walk(s, 0 , s.length()-1, "", m, new HashSet<>());
//        System.out.println(m);
//    }

}