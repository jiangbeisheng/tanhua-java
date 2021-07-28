package com.tanhua.server.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tanhua.server.service.MyCenterService;
import com.tanhua.server.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("users")
@RestController
@Slf4j
public class MyCenterController {

    @Autowired
    private MyCenterService myCenterService;

    /**
     * 根据用户id查询用户信息
     *
     * @param userId 用户id，如果为空，表示查询当前登录人的信息
     * @return
     */
    @GetMapping
    public ResponseEntity<UserInfoVo> queryUserInfoByUserId(@RequestParam(value = "userID", required = false) Long userId) {
        try {
            UserInfoVo userInfoVo = this.myCenterService.queryUserInfoByUserId(userId);
            if (ObjectUtil.isNotEmpty(userInfoVo)) {
                return ResponseEntity.ok(userInfoVo);
            }
        } catch (Exception e) {
            log.error("根据用户id查询用户信息出错~ userId = " + userId, e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 更新用户信息
     *
     * @param userInfoVo
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserInfoVo userInfoVo){
        try {
            Boolean bool = this.myCenterService.updateUserInfo(userInfoVo);
            if(bool){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}