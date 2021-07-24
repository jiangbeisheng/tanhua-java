package com.tanhua.server.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.common.utils.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.concurrent.TimeUnit;

@ControllerAdvice
public class MyResponseBodyAdvice implements ResponseBodyAdvice {

    @Value("${tanhua.cache.enable}")
    private Boolean enable;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private static final ObjectMapper MAPPER= new ObjectMapper();

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        // 开关处于开启状态 是get请求 包含了@Cache注解
        return enable && methodParameter.hasMethodAnnotation(GetMapping.class)
                && methodParameter.hasMethodAnnotation(Cache.class);
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass,
                                  ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (null==o){
            return null;
        }

        String redisKey = null;
        try {
            String redisValue=null;
            if (o instanceof String){
                redisValue = (String) o;
            }else {
                redisValue= MAPPER.writeValueAsString(o);
            }
            redisKey = RedisCacheInterceptor.createRedisKey(((ServletServerHttpRequest) serverHttpRequest).getServletRequest());

            Cache cache = methodParameter.getMethodAnnotation(Cache.class);
            //缓存的时间单位是秒
            this.redisTemplate.opsForValue().set(redisKey,redisValue,Long.parseLong(cache.time()), TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return o;
    }
}
