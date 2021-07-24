package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Publish;
import com.tanhua.dubbo.server.vo.PageInfo;

public interface QuanZiApi {
    /**
     * 查询好友动态
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Publish> queryPublishList(Long userId,Integer page,Integer pageSize);

    /**
     * 发布动态
     *
     * @param publish
     * @return
     */
    String savePublish(Publish publish);

    /**
     * 查询推荐动态
     *
     * @param userId 用户id
     * @param page 当前页数
     * @param pageSize 每一页查询的数据条数
     * @return
     */
    PageInfo<Publish> queryRecommendPublishList(Long userId, Integer page, Integer pageSize);

    /**
     * 点赞
     *
     * @param id
     * @return
     */
    Publish queryPublishById(String id);

    /**
     *点赞
     *
     * @param userId
     * @param publishId
     * @return
     */
    Boolean likeComment(Long userId,String publishId);

    /**
     * 取消点赞
     *
     * @param userId
     * @param PublishId
     * @return
     */
    Boolean disLikeComment(Long userId,String PublishId);

    /**
     * 查询点赞数
     *
     * @param publishId
     * @return
     */
    Long queryLikeComment(String publishId);

    /**
     * 查询用户是否点赞该动态
     *
     * @param userId
     * @param publishId
     * @return
     */
    Boolean queryUserIsLike(Long userId,String publishId);
}
