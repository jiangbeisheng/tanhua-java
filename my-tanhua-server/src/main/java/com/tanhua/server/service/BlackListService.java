package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.common.mapper.BlackListMapper;
import com.tanhua.common.pojo.BlackList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class BlackListService {

    @Autowired
    private BlackListMapper blackListMapper;

    public IPage<BlackList> queryBlacklist(Long userId, Integer page, Integer pageSize) {
        QueryWrapper<BlackList> wrapper = new QueryWrapper<BlackList>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created");
        Page<BlackList> pager = new Page<>(page, pageSize);
        return this.blackListMapper.selectPage(pager, wrapper);
    }

    public Boolean delBlacklist(Long userId, Long blackUserId) {
        QueryWrapper<BlackList> wrapper = new QueryWrapper<BlackList>();
        wrapper.eq("user_id", userId).eq("black_user_id", blackUserId);
        return this.blackListMapper.delete(wrapper) > 0;
    }
}

