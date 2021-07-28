package com.tanhua.dubbo.server.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.client.result.DeleteResult;
import com.tanhua.dubbo.server.enums.IdType;
import com.tanhua.dubbo.server.pojo.FollowUser;
import com.tanhua.dubbo.server.pojo.Publish;
import com.tanhua.dubbo.server.pojo.Video;
import com.tanhua.dubbo.server.service.IdService;
import com.tanhua.dubbo.server.vo.PageInfo;
import lombok.extern.slf4j.Slf4j;
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

@Service(version = "1.0.0")
@Slf4j
public class VideoApiImpl implements VideoApi {

    @Autowired
    private IdService idService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    private static final String VIDEO_FOLLOW_USER_KEY_PREFIX="VIDEO_FOLLOW_USER_";

    /**
     * 发布小视频
     *
     * @param video
     * @return
     */
    @Override
    public String saveVideo(Video video) {
        try {
            //校验
            if (!ObjectUtil.isAllNotEmpty(video.getUserId(), video.getPicUrl(), video.getVideoUrl())) {
                return null;
            }

            //设置id
            video.setId(ObjectId.get());
            video.setVid(this.idService.createId(IdType.VIDEO));

            //发布时间
            video.setCreated(System.currentTimeMillis());

            //保存到mongodb
            Video save = this.mongoTemplate.save(video);
            System.out.println(save);
            return video.getId().toHexString();
        } catch (Exception e) {
            log.error("发布小视频失败 video =" + video, e);
            return null;
        }
    }

    /**
     * 查询小视频列表,优先展现推荐到视频,如果没有推荐到视频或已经查询完成,就需要查询系统视频数据
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Video> queryVideoList(Long userId, Integer page, Integer pageSize) {
        PageInfo<Video> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);

        //从redis中获取
        String redisKey = "QUANZI_VIDEO_RECOMMEND_" + userId;
        String redisData = this.redisTemplate.opsForValue().get(redisKey);
        List<Long> vids = new ArrayList<>();
        int recommendCount =0;
        if (StrUtil.isNotEmpty(redisData)) {
            //手动分页查询数据
            List<String> vidList = StrUtil.split(redisData, ',');
            //计算分页
            //[0,10]
            int[] startEnd = PageUtil.transToStartEnd(page - 1, pageSize);
            int startIndex = startEnd[0]; //开始
            int endIndex = Math.min(startEnd[1], vidList.size()); //结束
            for (int i = startIndex; i < endIndex; i++) {
                vids.add(Convert.toLong(vidList.get(i)));
            }
            recommendCount = vidList.size();
        }

        if (CollUtil.isEmpty(vids)) {
            //没有推荐或前面推荐已经查询完毕,查询系统到视频数据
            //计算前面的推荐视频页数
            int totalPage = PageUtil.totalPage(recommendCount, pageSize);
            PageRequest pageRequest = PageRequest.of(page - totalPage-1, pageSize, Sort.by(Sort.Order.desc("created")));
            Query query = new Query().with(pageRequest);
            List<Video> videoList = this.mongoTemplate.find(query, Video.class);
            pageInfo.setRecords(videoList);
            return pageInfo;
        }

        //根据vid查询对应的视频数据了
        Query query = Query.query(Criteria.where("vid").in(vids));
        List<Video> videoList = this.mongoTemplate.find(query, Video.class);
        pageInfo.setRecords(videoList);
        return pageInfo;
    }

    @Override
    public Video queryVideoById(String videoId) {
        return this.mongoTemplate.findById(new ObjectId(videoId), Video.class);
    }

    @Override
    public Boolean followUser(Long userId, Long followUserId) {
        if (!ObjectUtil.isAllNotEmpty(userId,followUserId)){
            return false;
        }

        try {
            //需要将用户的关注列表 保存到redis中,方便后续到查询
            //使用redis的hash结构
            if (this.isFollowUser(userId,followUserId)){
                return false;
            }

            FollowUser followUser = new FollowUser();
            followUser.setId(ObjectId.get());
            followUser.setUserId(userId);
            followUser.setFollowUserId(followUserId);
            followUser.setCreated(System.currentTimeMillis());

            this.mongoTemplate.save(followUser);

            //保存数据到redis
            String redisKey = this.getVideoFollowUserKey(userId);
            String hashKey = String.valueOf(followUserId);

            this.redisTemplate.opsForHash().put(redisKey,hashKey,"1");
        return true;
        } catch (Exception e) {
            log.error("关注用户失败 userId="+userId+",followUserId="+followUserId,e);
        }
        return false;
    }

    @Override
    public Boolean disFollowUser(Long userId, Long followUserId) {
        if (!ObjectUtil.isAllNotEmpty(userId,followUserId)){
            return false;
        }
        if (!this.isFollowUser(userId,followUserId)){
            return false;
        }

        //取消关注,删除关注数据
       Query query= Query.query(Criteria.where("userId").is(userId)
            .and("followUserId").is(followUserId)
        );
        DeleteResult result = this.mongoTemplate.remove(query, FollowUser.class);
        if (result.getDeletedCount()>0){
            //同时,删除redis中的数据
            String redisKey = this.getVideoFollowUserKey(userId);
            String hashKey = String.valueOf(followUserId);
            this.redisTemplate.opsForHash().delete(redisKey,hashKey);
            return true;
        }

        return false;
    }

    @Override
    public Boolean isFollowUser(Long userId, Long followUserId) {
        //保存数据到redis
        String redisKey = this.getVideoFollowUserKey(userId);
        String hashKey = String.valueOf(followUserId);
        return this.redisTemplate.opsForHash().hasKey(redisKey,hashKey);
    }

    private String getVideoFollowUserKey(Long userId){
        return VIDEO_FOLLOW_USER_KEY_PREFIX +userId;
    }
}
