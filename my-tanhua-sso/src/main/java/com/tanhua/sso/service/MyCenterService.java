//com.tanhua.sso.service.MyCenterService

package com.tanhua.sso.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tanhua.common.pojo.User;
import com.tanhua.sso.vo.ErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MyCenterService {

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public Boolean sendVerificationCode(String token) {
        //校验token
        User user = this.userService.queryUserByToken(token);
        if(ObjectUtil.isEmpty(user)){
            return false;
        }

        ErrorResult errorResult = this.smsService.sendCheckCode(user.getMobile());
        return errorResult == null;
    }


    public Boolean checkVerificationCode(String code, String token) {
        //校验token
        User user = this.userService.queryUserByToken(token);
        if(ObjectUtil.isEmpty(user)){
            return false;
        }

        //校验验证码，先查询redis中的验证码
        String redisKey = "CHECK_CODE_" + user.getMobile();
        String value =  this.redisTemplate.opsForValue().get(redisKey);

        if(StrUtil.equals(code, value)){
            //将验证码删除
            this.redisTemplate.delete(redisKey);
            return true;
        }

        return false;
    }

    public Boolean updatePhone(String token, String newPhone) {
        //校验token
        User user = this.userService.queryUserByToken(token);
        if(ObjectUtil.isEmpty(user)){
            return false;
        }
        Boolean result = this.userService.updatePhone(user.getId(), newPhone);
        if(result){
            String redisKey = "TANHUA_USER_MOBILE_" + user.getId();
            this.redisTemplate.delete(redisKey);
        }

        return result;
    }
}

