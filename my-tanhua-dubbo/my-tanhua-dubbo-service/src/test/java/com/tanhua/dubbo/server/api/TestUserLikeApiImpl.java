package com.tanhua.dubbo.server.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestUserLikeApiImpl {

    @Autowired
    private UserLikeApi userLikeApi;

    @Test
    public void testQueryCounts(){
        System.out.println(this.userLikeApi.queryMutualLikeCount(1L));
        System.out.println(this.userLikeApi.queryFanCount(1L));
        System.out.println(this.userLikeApi.queryLikeCount(1L));
    }

}

