package com.tanhua.dubbo.server.api;

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

}