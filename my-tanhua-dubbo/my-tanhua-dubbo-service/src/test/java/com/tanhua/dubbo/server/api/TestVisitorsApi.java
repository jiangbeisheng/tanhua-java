package com.tanhua.dubbo.server.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestVisitorsApi {

    @Autowired
    private VisitorsApi visitorsApi;

    @Test
    public void testSaveVisitor(){
        this.visitorsApi.saveVisitor(1L, 2L, "个人主页");
        this.visitorsApi.saveVisitor(1L, 3L, "个人主页");
        this.visitorsApi.saveVisitor(1L, 2L, "个人主页");
    }

    @Test
    public void testQueryMyVisitor(){
        this.visitorsApi.queryMyVisitor(1L)
                .forEach(visitors -> System.out.println(visitors));
    }

}

