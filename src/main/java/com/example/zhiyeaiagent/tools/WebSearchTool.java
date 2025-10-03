package com.example.zhiyeaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网页搜索工具
 */
public class WebSearchTool {
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";
    private final String apiKey;
    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }
    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        Map<String, Object> paramMap=new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");
        try{
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            //取出返回结果的前5条
            JSONObject jsonObject = new JSONObject(response);
            //提取 original_results
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            List<Object> objects = organicResults.subList(0,5);
            //拼接搜索结果成字符串
            String result= objects.stream().map(obj->{
                JSONObject tmpjsonObject = (JSONObject) obj;
                return tmpjsonObject.toString();
            }).collect(Collectors.joining(","));//用逗号连接
            return result;
        }catch (Exception e){
            return "Error searching Baidu :"+e.getMessage();
        }
    }
}
