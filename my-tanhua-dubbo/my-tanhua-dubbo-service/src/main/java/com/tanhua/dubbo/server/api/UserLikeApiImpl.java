package com.tanhua.dubbo.server.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.pojo.UserLike;
import com.tanhua.dubbo.server.vo.PageInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service(version = "1.0.0")
public class UserLikeApiImpl implements UserLikeApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public static final String LIKE_REDIS_KEY_PREFIX = "USER_LIKE_";

    public static final String NOT_LIKE_REDIS_KEY_PREFIX = "USER_NOT_LIKE_";


    /**
     * 喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    @Override
    public Boolean likeUser(Long userId, Long likeUserId) {
        //判断该用户是否已经喜欢，如果已经喜欢就返回
        if(this.isLike(userId, likeUserId)){
            return false;
        }

        UserLike userLike = new UserLike();
        userLike.setId(ObjectId.get());
        userLike.setUserId(userId);
        userLike.setLikeUserId(likeUserId);
        userLike.setCreated(System.currentTimeMillis());

        //将数据存储到MongoDB
        this.mongoTemplate.save(userLike);

        //用户的喜欢数据保存到redis
        //用户1：key -> USER_LIKE_1 , value -> 2, "1"
        //用户1：key -> USER_LIKE_1 , value -> 3, "1"
        //用户2：key -> USER_LIKE_2 , value -> 4, "1"
        String redisKey = this.getLikeRedisKey(userId);
        String hashKey = String.valueOf(likeUserId);
        this.redisTemplate.opsForHash().put(redisKey, hashKey, "1");

        //判断，喜欢的用户是否在不喜欢的列表中，如果在，就需要删除数据
        if(this.isNotLike(userId, likeUserId)){
            redisKey = this.getNotLikeRedisKey(userId);
            this.redisTemplate.opsForHash().delete(redisKey, hashKey);
        }

        return true;
    }

    /**
     * 获取喜欢数据的redis key
     *
     * @param userId
     * @return
     */
    private String getLikeRedisKey(Long userId){
        return LIKE_REDIS_KEY_PREFIX + userId;
    }

    /**
     * 获取不喜欢数据的redis key
     *
     * @param userId
     * @return
     */
    private String getNotLikeRedisKey(Long userId){
        return NOT_LIKE_REDIS_KEY_PREFIX + userId;
    }

    /**
     * 是否喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    public Boolean isLike(Long userId, Long likeUserId){
        String redisKey = this.getLikeRedisKey(userId);
        String hashKey = String.valueOf(likeUserId);
        return this.redisTemplate.opsForHash().hasKey(redisKey, hashKey);
    }

    /**
     * 是否不喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    public Boolean isNotLike(Long userId, Long likeUserId){
        String redisKey = this.getNotLikeRedisKey(userId);
        String hashKey = String.valueOf(likeUserId);
        return this.redisTemplate.opsForHash().hasKey(redisKey, hashKey);
    }

    @Override
    public Boolean notLikeUser(Long userId, Long likeUserId) {
        //判断用户是否已经不喜欢，如果已经不喜欢，就返回
        if(this.isNotLike(userId, likeUserId)){
            return false;
        }

        //将用户保存到不喜欢列表中
        String redisKey = this.getNotLikeRedisKey(userId);
        String hashKey = String.valueOf(likeUserId);
        this.redisTemplate.opsForHash().put(redisKey, hashKey, "1");

        //判断用户是否在喜欢列表中，如果存在的话，需要删除数据
        if(this.isLike(userId, likeUserId)){
            //删除MongoDB数据
            Query query = Query.query(Criteria
                    .where("userId").is(userId)
                    .and("likeUserId").is(likeUserId)
            );
            this.mongoTemplate.remove(query, UserLike.class);

            //删除redis中的数据
            redisKey = this.getLikeRedisKey(userId);
            this.redisTemplate.opsForHash().delete(redisKey, hashKey);
        }

        return true;
    }

    @Override
    public Boolean isMutualLike(Long userId, Long likeUserId) {
        return this.isLike(userId, likeUserId)
                && this.isLike(likeUserId, userId);
    }

    @Override
    public List<Long> queryLikeList(Long userId) {
        // 查询redis
        String redisKey = this.getLikeRedisKey(userId);
        Set<Object> keys = this.redisTemplate.opsForHash().keys(redisKey);
        if(CollUtil.isEmpty(keys)){
            return ListUtil.empty();
        }

        List<Long> result = new ArrayList<>(keys.size());
        keys.forEach(o -> result.add(Convert.toLong(o)));
        return result;
    }

    @Override
    public List<Long> queryNotLikeList(Long userId) {
        // 查询redis
        String redisKey = this.getNotLikeRedisKey(userId);
        Set<Object> keys = this.redisTemplate.opsForHash().keys(redisKey);
        if(CollUtil.isEmpty(keys)){
            return ListUtil.empty();
        }

        List<Long> result = new ArrayList<>(keys.size());
        keys.forEach(o -> result.add(Convert.toLong(o)));
        return result;
    }

    //com.tanhua.dubbo.server.api.UserLikeApiImpl

    /**
     * 查询相互喜欢数
     * 实现2种方式：第一种：查询redis，第二种：查询MongoDB
     * 建议：优先使用redis查询，其次考虑使用Mongodb
     *
     * @param userId
     * @return
     */
    @Override
    public Long queryMutualLikeCount(Long userId) {
        //查询我的喜欢列表
        List<Long> likeUserIdList = this.queryLikeList(userId);

        Long count = 0L;
        for (Long likeUserId : likeUserIdList) {
            String redisKey = this.getLikeRedisKey(likeUserId);
            String hashKey = String.valueOf(userId);
            //“别人” 的喜欢列表中是否有 “我”
            if (this.redisTemplate.opsForHash().hasKey(redisKey, hashKey)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Long queryLikeCount(Long userId) {
        String redisKey = this.getLikeRedisKey(userId);
        return this.redisTemplate.opsForHash().size(redisKey);
    }

    @Override
    public Long queryFanCount(Long userId) {
        //无法通过redis查询完成，必须从Mongodb中查询
        Query query = Query.query(Criteria.where("likeUserId").is(userId));
        return this.mongoTemplate.count(query, UserLike.class);
    }


    @Override
    public PageInfo<UserLike> queryMutualLikeList(Long userId, Integer page, Integer pageSize) {
        //查询我的喜欢列表
        List<Long> userLikeIdList = this.queryLikeList(userId);

        //查询喜欢我的人
        Query query = Query.query(Criteria.where("userId").in(userLikeIdList)
                .and("likeUserId").is(userId)
        );

        return this.queryList(query, page, pageSize);
    }

    @Override
    public PageInfo<UserLike> queryLikeList(Long userId, Integer page, Integer pageSize) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        return this.queryList(query, page, pageSize);
    }

    @Override
    public PageInfo<UserLike> queryFanList(Long userId, Integer page, Integer pageSize) {
        Query query = Query.query(Criteria.where("likeUserId").is(userId));
        return this.queryList(query, page, pageSize);
    }

    private PageInfo<UserLike> queryList(Query query, Integer page, Integer pageSize) {
        //设置分页
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize,
                Sort.by(Sort.Order.desc("created")));
        query.with(pageRequest);

        List<UserLike> userLikeList = this.mongoTemplate.find(query, UserLike.class);

        PageInfo<UserLike> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(userLikeList);

        return pageInfo;
}}

