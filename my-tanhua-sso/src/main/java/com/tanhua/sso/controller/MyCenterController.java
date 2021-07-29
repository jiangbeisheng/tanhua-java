package com.tanhua.sso.controller;

import com.tanhua.sso.service.MyCenterService;
import com.tanhua.sso.vo.ErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("users")
public class MyCenterController {

    @Autowired
    private UserInfoController userInfoController;
    @Autowired
    private MyCenterService myCenterService;

    /**
     * 上传头像
     *
     * @param file
     * @param token
     * @return
     */
    @PostMapping("header")
    public ResponseEntity<Object> saveLogo(@RequestParam("headPhoto") MultipartFile file, @RequestHeader("Authorization") String token) {
        return this.userInfoController.saveUserLogo(file, token);
    }


    /**
     * 发送短信验证码
     *
     * @return
     */
    @PostMapping("phone/sendVerificationCode")
    public ResponseEntity<Void> sendVerificationCode(@RequestHeader("Authorization") String token) {
        try {
            boolean bool = this.myCenterService.sendVerificationCode(token);
            if (bool) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 校验验证码
     *
     * @param param
     * @param token
     * @return
     */
    @PostMapping("phone/checkVerificationCode")
    public ResponseEntity<Map<String, Object>> checkVerificationCode(@RequestBody Map<String, String> param,
                                                                     @RequestHeader("Authorization") String token) {
        try {
            String code = param.get("verificationCode");
            Boolean bool = this.myCenterService.checkVerificationCode(code, token);
            Map<String, Object> result = new HashMap<>();
            result.put("verification", bool);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 保存新手机号
     *
     * @return
     */
    @PostMapping("phone")
    public ResponseEntity<Void> updatePhone(@RequestBody Map<String, String> param,
                                            @RequestHeader("Authorization") String token) {
        try {
            String newPhone = param.get("phone");
            boolean bool = this.myCenterService.updatePhone(token, newPhone);
            if (bool) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}