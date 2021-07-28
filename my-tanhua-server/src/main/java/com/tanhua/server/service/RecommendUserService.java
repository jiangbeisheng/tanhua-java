package com.tanhua.server.service;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.dubbo.config.annotation.Reference;

import com.tanhua.dubbo.server.api.RecommendUserApi;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.vo.TodayBest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 负责与dubbo服务进行交互
 */
@Service
public class RecommendUserService {

    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;

    public TodayBest queryTodayBast(Long userId) {
        RecommendUser recommendUser = this.recommendUserApi.queryWithMaxScore(userId);
        if (null == recommendUser) {
            return null;
        }
        TodayBest todayBest = new TodayBest();
        todayBest.setId(recommendUser.getUserId());
        double score = Math.floor(recommendUser.getScore());
        todayBest.setFateValue((long) score);

        return todayBest;
    }

    public PageInfo<RecommendUser> queryRecommendUserList(Long id, Integer page, Integer pagesize) {
        return this.recommendUserApi.queryPageInfo(id, page, pagesize);
    }


    /**
     * 查询推荐好友的缘分值
     *
     * @param userId
     * @param toUserId
     * @return
     */
    public Double queryScore(Long userId, Long toUserId) {
        Double score = this.recommendUserApi.queryScore(userId, toUserId);
        if (ObjectUtil.isNotEmpty(score)) {
            return score;
        }
        //默认值
        return 98d;
    }

    /**
     * 查询探花卡片列表
     *
     * @param id
     * @param count
     * @return
     */
    public List<RecommendUser> queryCardList(Long id, int count) {
        return this.recommendUserApi.queryCardList(id, count);
    }
}
