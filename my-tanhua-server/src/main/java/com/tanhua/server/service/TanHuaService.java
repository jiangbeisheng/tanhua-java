package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.common.enums.SexEnum;
import com.tanhua.common.pojo.Question;
import com.tanhua.common.pojo.User;
import com.tanhua.common.pojo.UserInfo;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.api.HuanXinApi;
import com.tanhua.dubbo.server.api.UserLikeApi;
import com.tanhua.dubbo.server.api.UserLocationApi;
import com.tanhua.dubbo.server.api.VisitorsApi;
import com.tanhua.dubbo.server.enums.HuanXinMessageType;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.dubbo.server.vo.UserLocationVo;
import com.tanhua.server.vo.NearUserVo;
import com.tanhua.server.vo.TodayBest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TanHuaService {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RecommendUserService recommendUserService;

    @Autowired
    private QuestionService questionService;

    @Reference(version = "1.0.0")
    private HuanXinApi huanXinApi;

    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;

    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;

    @Value("${tanhua.default.recommend.users}")
    private String defaultRecommendUsers ;

    @Reference(version = "1.0.0")
    private UserLikeApi userLikeApi;

    @Autowired
    private IMService imService;

    public TodayBest queryUserInfo(Long userId) {

        UserInfo userInfo = this.userInfoService.queryUserInfoByUserId(userId);
        if(ObjectUtil.isEmpty(userInfo)){
            return null;
        }

        TodayBest todayBest = new TodayBest();
        todayBest.setId(userId);
        todayBest.setAge(userInfo.getAge());
        todayBest.setGender(userInfo.getSex().name().toLowerCase());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setTags(Convert.toStrArray(StrUtil.split(userInfo.getTags(),',')));
        todayBest.setAvatar(userInfo.getLogo());

        //缘分值
        User user = UserThreadLocal.get();
        todayBest.setFateValue(this.recommendUserService.queryScore(userId, user.getId()).longValue());

        //记录来访用户
        this.visitorsApi.saveVisitor(userId, user.getId(), "个人主页");

        return todayBest;
    }

    public String queryQuestion(Long userId) {
        Question question = this.questionService.queryQuestion(userId);
        if(ObjectUtil.isNotEmpty(question)){
            return question.getTxt();
        }
        //默认的问题
        return "你的爱好是什么？";
    }

    public Boolean replyQuestion(Long userId, String reply) {
        User user = UserThreadLocal.get();
        UserInfo userInfo = this.userInfoService.queryUserInfoByUserId(user.getId());

        //构建消息内容
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", user.getId());
        msg.put("huanXinId", "HX_" + user.getId());
        msg.put("nickname", userInfo.getNickName());
        msg.put("strangerQuestion", this.queryQuestion(userId));
        msg.put("reply", reply);

        //发送环信消息
        return this.huanXinApi.sendMsgFromAdmin("HX_" + userId,
                HuanXinMessageType.TXT, JSONUtil.toJsonStr(msg));
    }

    public List<NearUserVo> queryNearUser(String gender, String distance) {

        //查询当前用户的位置
        User user = UserThreadLocal.get();
        UserLocationVo userLocationVo = this.userLocationApi.queryByUserId(user.getId());
        if(ObjectUtil.isEmpty(userLocationVo)){
            return ListUtil.empty();
        }

        PageInfo<UserLocationVo> pageInfo = this.userLocationApi.queryUserFromLocation(userLocationVo.getLongitude(),
                userLocationVo.getLatitude(),
                Convert.toDouble(distance),
                1,
                50
        );

        List<UserLocationVo> records = pageInfo.getRecords();
        if(CollUtil.isEmpty(records)){
            return ListUtil.empty();
        }

        //构造筛选条件
        List<Object> userIdList = CollUtil.getFieldValues(records, "userId");
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIdList);
        if(StrUtil.equalsIgnoreCase(gender, "man")){
            queryWrapper.eq("sex", SexEnum.MAN);
        }else if(StrUtil.equalsIgnoreCase(gender, "woman")){
            queryWrapper.eq("sex", SexEnum.WOMAN);
        }

        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(queryWrapper);

        List<NearUserVo> result = new ArrayList<>();
        for (UserLocationVo locationVo : records) {
            //排除自己
            if(ObjectUtil.equals(locationVo.getUserId(), user.getId())){
                continue;
            }

            for (UserInfo userInfo : userInfoList) {
                if(ObjectUtil.equals(locationVo.getUserId(), userInfo.getUserId())){

                    NearUserVo nearUserVo = new NearUserVo();
                    nearUserVo.setUserId(userInfo.getUserId());
                    nearUserVo.setAvatar(userInfo.getLogo());
                    nearUserVo.setNickname(userInfo.getNickName());
                    result.add(nearUserVo);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 查询推荐卡片列表，从推荐列表中随机选取10个用户
     *
     * @return
     */
    public List<TodayBest> queryCardsList() {

        User user = UserThreadLocal.get();
        int count = 50;

        //查询到的50条数据，并不是用来直接展现，需要从这50条数据中随机返回一些数据
        List<RecommendUser> recommendUserList = this.recommendUserService.queryCardList(user.getId(), count);
        if (CollUtil.isEmpty(recommendUserList)) {
            recommendUserList = new ArrayList<>();
            //默认推荐列表
            List<String> list = StrUtil.split(defaultRecommendUsers, ',');
            for (String userId : list) {
                RecommendUser recommendUser = new RecommendUser();

                recommendUser.setToUserId(user.getId());
                recommendUser.setUserId(Convert.toLong(userId));
                recommendUserList.add(recommendUser);
            }
        }

        //计算展现的数量，默认展现10个
        int showCount = Math.min(10, recommendUserList.size());
        List<RecommendUser> result = new ArrayList<>();
        for (int i = 0; i < showCount; i++) {
            //TODO 可能重复
            int index = RandomUtil.randomInt(0, recommendUserList.size());
            RecommendUser recommendUser = recommendUserList.get(index);
            result.add(recommendUser);
        }

        List<Object> userIdList = CollUtil.getFieldValues(result, "userId");
        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoByUserIdList(userIdList);
        List<TodayBest> todayBests = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            TodayBest todayBest = new TodayBest();
            todayBest.setId(userInfo.getUserId());
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(userInfo.getSex().name().toLowerCase());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(Convert.toStrArray(StrUtil.split(userInfo.getTags(), ',')));
            todayBest.setFateValue(0L);

            todayBests.add(todayBest);
        }

        return todayBests;
    }



    public Boolean likeUser(Long likeUserId) {
        User user = UserThreadLocal.get();
        Boolean result = this.userLikeApi.likeUser(user.getId(), likeUserId);
        if (!result) {
            return false;
        }

        if (this.userLikeApi.isMutualLike(user.getId(), likeUserId)) {
            //相互喜欢成为好友
            this.imService.contactUser(likeUserId);
        }
        return true;
    }

    public Boolean notLikeUser(Long likeUserId) {
        User user = UserThreadLocal.get();
        return this.userLikeApi.notLikeUser(user.getId(), likeUserId);
    }
}

