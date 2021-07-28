package com.tanhua.dubbo.server.api;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@Service(version = "1.0.0") //声明这是一个dubbo服务
public class RecommendUserApiImpl implements RecommendUserApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserLikeApi userLikeApi;

    @Override
    public RecommendUser queryWithMaxScore(Long userId) {
        //查询用户按照得分降序排序，并显示1条
       Query query= Query.query(Criteria.where("toUserId").is(userId))
                .with(Sort.by(Sort.Order.desc("score")))
                .limit(1);
        return this.mongoTemplate.findOne(query,RecommendUser.class);
    }

    @Override
    public PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize) {
        //分页并且排序参数
        PageRequest pageRequest = PageRequest.of(pageNum-1, pageSize,Sort.by(Sort.Order.desc("score")));
        //查询参数
        Query query =Query.query(Criteria.where("toUserId").is(userId))
                .with(pageRequest);
        List<RecommendUser> recommendUserList = this.mongoTemplate.find(query, RecommendUser.class);
        //暂时不提供数据总数
        return new PageInfo<>(0,pageNum,pageSize,recommendUserList);
    }

    @Override
    public Double queryScore(Long userId, Long toUserId) {
        Query query = Query.query(Criteria.where("toUserId").is(toUserId)
                .and("userId").is(userId));
        RecommendUser recommendUser = this.mongoTemplate.findOne(query, RecommendUser.class);
        if (null != recommendUser) {
            return recommendUser.getScore();
        }
        return null;
    }

    @Override
    public List<RecommendUser> queryCardList(Long userId, Integer count) {
        //设置分页以及排序，按照得分倒序排序
        PageRequest pageRequest = PageRequest.of(0, count, Sort.by(Sort.Order.desc("score")));

        //排除已喜欢或不喜欢的用户
        List<Long> userIds = new ArrayList<>();
        //查询喜欢列表
        userIds.addAll(this.userLikeApi.queryLikeList(userId));

        //查询不喜欢列表
        userIds.addAll(this.userLikeApi.queryNotLikeList(userId));

        //构造查询条件
        Criteria criteria = Criteria.where("toUserId").is(userId);
        if(CollUtil.isNotEmpty(userIds)){
            //加入到查询条件中，排除这些用户
            criteria.andOperator(Criteria.where("userId").nin(userIds));
        }

        Query query = Query.query(criteria).with(pageRequest);
        List<RecommendUser> recommendUserList = this.mongoTemplate.find(query, RecommendUser.class);
        return recommendUserList;
    }
}
