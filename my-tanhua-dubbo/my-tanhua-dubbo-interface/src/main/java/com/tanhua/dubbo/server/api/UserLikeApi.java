package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.UserLike;
import com.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

public interface UserLikeApi {

    /**
     * 喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean likeUser(Long userId, Long likeUserId);

    /**
     * 不喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean notLikeUser(Long userId, Long likeUserId);

    /**
     * 是否喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean isLike(Long userId, Long likeUserId);

    /**
     * 是否不喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean isNotLike(Long userId, Long likeUserId);


    /**
     * 是否相互喜欢
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean isMutualLike(Long userId, Long likeUserId);


    /**
     * 查询喜欢列表
     *
     * @param userId
     * @return
     */
    List<Long> queryLikeList(Long userId);

    /**
     * 查询不喜欢列表
     *
     * @param userId
     * @return
     */
    List<Long> queryNotLikeList(Long userId);

    //com.tanhua.dubbo.server.api.UserLikeApi

    /**
     * 相互喜欢的数量
     *
     * @return
     */
    Long queryMutualLikeCount(Long userId);

    /**
     * 喜欢数
     *
     * @return
     */
    Long queryLikeCount(Long userId);

    /**
     * 粉丝数
     *
     * @return
     */
    Long queryFanCount(Long userId);

    //com.tanhua.dubbo.server.api.UserLikeApi

    /**
     * 查询相互喜欢列表
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<UserLike> queryMutualLikeList(Long userId, Integer page, Integer pageSize);

    /**
     * 查询我喜欢的列表
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<UserLike> queryLikeList(Long userId, Integer page, Integer pageSize);

    /**
     * 查询粉丝列表
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<UserLike> queryFanList(Long userId, Integer page, Integer pageSize);

}