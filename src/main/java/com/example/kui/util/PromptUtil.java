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

    private final Yaml yaml =  new Yaml();

    public PromptUtil(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getPrompt(PromptKey promptKey) {
        try{
            Resource resource = resourceLoader.getResource("classpath:" + promptKey.getFile());
            InputStream inputStream = resource.getInputStream();
            Map<String,Object> data  = yaml.load(inputStream);

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
            throw new RuntimeException(e);
        }
    }
}
