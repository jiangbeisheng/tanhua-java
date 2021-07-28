package com.tanhua.server.controller;

import com.tanhua.server.service.TanHuaService;
import com.tanhua.server.vo.NearUserVo;
import com.tanhua.server.vo.TodayBest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("tanhua")
@RestController
public class TanHuaController {

    @Autowired
    private TanHuaService tanHuaService;

    /**
     * 查询个人主页的个人信息
     *
     * @param userId
     * @return
     */
    @GetMapping("{id}/personalInfo")
    public ResponseEntity<TodayBest> queryUserInfo(@PathVariable("id") Long userId) {
        try {
            TodayBest todayBest = this.tanHuaService.queryUserInfo(userId);
            return ResponseEntity.ok(todayBest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询陌生人问题
     *
     * @param userId
     * @return
     */
    @GetMapping("strangerQuestions")
    public ResponseEntity<String> queryQuestion(@RequestParam("userId") Long userId) {
        try {
            String question = this.tanHuaService.queryQuestion(userId);
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 回复陌生人问题
     *
     * @return
     */
    @PostMapping("strangerQuestions")
    public ResponseEntity<Void> replyQuestion(@RequestBody Map<String, Object> param) {
        try {
            Long userId = Long.valueOf(param.get("userId").toString());
            String reply = param.get("reply").toString();
            Boolean result = this.tanHuaService.replyQuestion(userId, reply);
            if (result) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 搜附近
     *
     * @param gender
     * @param distance
     * @return
     */
    @GetMapping("search")
    public ResponseEntity<List<NearUserVo>> queryNearUser(@RequestParam(value = "gender", required = false) String gender,
                                                          @RequestParam(value = "distance", defaultValue = "2000") String distance) {
        try {
            List<NearUserVo> list = this.tanHuaService.queryNearUser(gender, distance);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 探花
     *
     * @return
     */
    @GetMapping("cards")
    public ResponseEntity<List<TodayBest>> queryCardsList() {
        try {
            List<TodayBest> list = this.tanHuaService.queryCardsList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 喜欢
     *
     * @param likeUserId
     * @return
     */
    @GetMapping("{id}/love")
    public ResponseEntity<Void> likeUser(@PathVariable("id") Long likeUserId) {
        try {
            this.tanHuaService.likeUser(likeUserId);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 不喜欢
     *
     * @param likeUserId
     * @return
     */
    @GetMapping("{id}/unlove")
    public ResponseEntity<Void> notLikeUser(@PathVariable("id") Long likeUserId) {
        try {
            this.tanHuaService.notLikeUser(likeUserId);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}

