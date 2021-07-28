package com.tanhua.server.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.common.pojo.User;
import com.tanhua.common.pojo.UserInfo;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.RecommendUserQueryParam;
import com.tanhua.server.vo.TodayBest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class TodayBestService {

    @Autowired
    private UserService userService;

    @Autowired
    private RecommendUserService recommendUserService;

    @Autowired
    private UserInfoService userInfoService;

    @Value("${tanhua.sso.default.user}")
    private Long defaultUser;

    /**
     * 查询今日佳人
     *
     * @return
     */
    public TodayBest queryTodayBast() {
        //无需校验直接获取
        User user = UserThreadLocal.get();
        //查询推荐用户（今日佳人）
        TodayBest todayBest = this.recommendUserService.queryTodayBast(user.getId());
        if (null == todayBest) {
            //给出默认的推荐用户
            todayBest = new TodayBest();
            todayBest.setId(defaultUser);
            todayBest.setFateValue(80L); //固定值
        }

        //补全个人信息
        UserInfo userInfo = this.userInfoService.queryUserInfoByUserId(todayBest.getId());
        if (null == userInfo) {
            return null;
        }
        todayBest.setAvatar(userInfo.getLogo());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
        if (null==userInfo.getSex()){
            todayBest.setGender("unknown");
        }else {
            todayBest.setGender(userInfo.getSex().getValue()==1? "man":"woman");
        }
        todayBest.setAge(userInfo.getAge());
        return todayBest;
    }

    /**
     * 查询推荐用户列表
     * @param queryParam
     * @return
     */
    public PageResult queryRecommendation( RecommendUserQueryParam queryParam) {
        //校验token是否有效，通过sso的接口进行校验
        User user =UserThreadLocal.get();

        PageResult pageResult = new PageResult();
        pageResult.setPage(queryParam.getPage());
        pageResult.setPagesize(queryParam.getPagesize());

        PageInfo<RecommendUser> pageInfo= this.recommendUserService.queryRecommendUserList(user.getId(),queryParam.getPage(),queryParam.getPagesize());
        List<RecommendUser> records = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(records)){
            //没有查询到推荐的用户列表
            return pageResult;
        }

        //收集推荐用户的id
        Set<Long> userIds = new HashSet<>();
        for (RecommendUser record : records) {
            userIds.add(record.getUserId());
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        //用户id参数
        queryWrapper.in("user_id",userIds);
        if (StringUtils.isNotEmpty(queryParam.getGender())){
            //需要性别参数查询
//            queryWrapper.eq("sex",StringUtils.equals(queryParam.getGender(),"man")?1:2);
        }

        if (StringUtils.isNotEmpty(queryParam.getCity())){
            //需要城市参数查询
//            queryWrapper.like("city",queryParam.getCity());
        }

        if (queryParam.getAge()!=null){
            //设置年龄条件，小于等于
//            queryWrapper.le("age",queryParam.getAge());
        }

        List<UserInfo> userInfoList=this.userInfoService.queryUserInfoList(queryWrapper);
        if (CollectionUtils.isEmpty(userInfoList)){
            return pageResult;
        }

        ArrayList<TodayBest> todayBests = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            TodayBest todayBest = new TodayBest();

            todayBest.setId(userInfo.getId());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(),','));
            if (null==userInfo.getSex()){
                todayBest.setGender("unknown");
            }else {
                todayBest.setGender(userInfo.getSex().getValue()==1? "man":"woman");
            }
            todayBest.setAge(userInfo.getAge());

            //缘分值
            for (RecommendUser record : records) {
                if (record.getUserId().longValue()==userInfo.getUserId().longValue()){
                    double score = Math.floor(record.getScore());
                    todayBest.setFateValue(Double.valueOf(score).longValue());
                    break;
                }
            }

            todayBests.add(todayBest);
        }

        //按照缘分值进行倒序排序
        Collections.sort(todayBests,(o1,o2)->new Long(o2.getFateValue()- o1.getFateValue()).intValue());
        pageResult.setItems(todayBests);
        return pageResult;
    }
}
