package com.example.kui.util;

import com.example.kui.common.enums.PromptKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Service
public class PromptUtil {

    private final ResourceLoader resourceLoader;

    public PromptUtil(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getPrompt(PromptKey promptKey) {
        InputStream inputStream = null;
        try{
            Resource resource = resourceLoader.getResource("classpath:" + promptKey.getFile());
            inputStream = resource.getInputStream();

            // 每次创建新实例，避免并发问题
            Yaml yaml = new Yaml();
            Map<String,Object> data  = yaml.load(inputStream);

            // 检查数据是否为空
            if(data == null || data.isEmpty()){
                throw new IllegalStateException("YAML文件为空或格式错误: " + promptKey.getFile());
            }

            String[] keys = promptKey.getKey().split("\\.");
            Object value = data;
            for(String key:keys){
                if(value instanceof Map){
                    value = ((Map<String,Object>)value).get(key);
                }else{
                    return null;
                }
            }
            return value != null ? value.toString() : "";
        }catch (Exception e){
            throw new RuntimeException("加载Prompt失败: " + promptKey.getFile() + " -> " + promptKey.getKey(), e);
        }finally {
            // 确保关闭InputStream
            if(inputStream != null){
                try{
                    inputStream.close();
                }catch(Exception ignored){}
            }
        }
    }
}
