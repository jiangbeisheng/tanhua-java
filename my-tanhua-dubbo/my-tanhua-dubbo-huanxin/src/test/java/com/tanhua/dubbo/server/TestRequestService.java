package com.tanhua.dubbo.server;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.tanhua.dubbo.server.config.HuanXinConfig;
import com.tanhua.dubbo.server.service.RequestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRequestService {

    @Autowired
    private RequestService requestService;

    @Autowired
    private HuanXinConfig huanXinConfig;

    @Test
    public void testQueryHuanXinUser() {
        String targetUrl = this.huanXinConfig.getUrl()
                + this.huanXinConfig.getOrgName() + "/"
                + this.huanXinConfig.getAppName() + "/users/1";
        HttpResponse response = this.requestService.execute(targetUrl, null, Method.GET);
        System.out.println(response);

    }
}