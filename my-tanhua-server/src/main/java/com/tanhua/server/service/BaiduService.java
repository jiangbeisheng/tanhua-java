package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.common.pojo.User;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.api.UserLocationApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BaiduService {

    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;

    public Boolean updateLocation(Double longitude, Double latitude, String address) {
        User user = UserThreadLocal.get();
        try {
            return this.userLocationApi.updateUserLocation(user.getId(), longitude, latitude, address);
        } catch (Exception e) {
            log.error("更新地理位置失败~ userId = " + user.getId() + ", longitude = " + longitude + ", latitude = " + latitude + ", address = " + address, e);
        }
        return false;
    }

}