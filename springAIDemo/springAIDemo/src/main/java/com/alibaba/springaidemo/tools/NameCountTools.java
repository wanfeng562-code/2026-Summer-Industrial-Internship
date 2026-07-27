package com.alibaba.springaidemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 通过工具  查询数据库  执行自己的操作
 */
public class NameCountTools {

    @Tool(description = "长沙有多少个名字的数量")
    public String getNameCount(@ToolParam(description = "名字") String name){

        return "10个";
    }
}
