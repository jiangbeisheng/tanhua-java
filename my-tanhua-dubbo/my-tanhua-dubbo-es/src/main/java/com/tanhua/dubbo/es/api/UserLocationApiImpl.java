package com.tanhua.dubbo.es.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.api.UserLocationApi;
import com.tanhua.dubbo.server.pojo.UserLocation;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.dubbo.server.vo.UserLocationVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service(version = "1.0.0")
@Slf4j
public class UserLocationApiImpl implements UserLocationApi {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 初始化索引库
     *
     */
    @PostConstruct
    public void initIndex(){
        //判断索引库是否存在，如果不存在，需要创建
        if(!this.elasticsearchTemplate.indexExists("tanhua2")){
            this.elasticsearchTemplate.createIndex(UserLocation.class);
        }

        //判断表是否存在，如果不存在，需要创建
        if(!this.elasticsearchTemplate.typeExists("tanhua2", "user_location")){
            this.elasticsearchTemplate.putMapping(UserLocation.class);
        }
    }

    @Override
    public Boolean updateUserLocation(Long userId, Double longitude, Double latitude, String address) {
        //查询个人的地理位置数据，如果不存在，需要新增，如果是存在数据，更新数据

        try {
            GetQuery getQuery = new GetQuery();
            getQuery.setId(String.valueOf(userId));
            UserLocation userLocation = this.elasticsearchTemplate.queryForObject(getQuery, UserLocation.class);
            if(ObjectUtil.isEmpty(userLocation)){
                //新增数据
                userLocation = new UserLocation();
                userLocation.setUserId(userId);
                userLocation.setAddress(address);
                userLocation.setCreated(System.currentTimeMillis());
                userLocation.setUpdated(userLocation.getCreated());
                userLocation.setLastUpdated(userLocation.getCreated());
                userLocation.setLocation(new GeoPoint(latitude, longitude));

                IndexQuery indexQuery = new IndexQueryBuilder().withObject(userLocation).build();

                //保存数据到ES中
                this.elasticsearchTemplate.index(indexQuery);
            }else {
                //更新数据

                //更新的字段
                Map<String,Object> map = new HashMap<>();
                map.put("location", new GeoPoint(latitude, longitude));
                map.put("updated", System.currentTimeMillis());
                map.put("lastUpdated", userLocation.getUpdated());
                map.put("address", address);

                UpdateRequest updateRequest = new UpdateRequest();
                updateRequest.doc(map);

                UpdateQuery updateQuery = new UpdateQueryBuilder()
                        .withId(String.valueOf(userId))
                        .withClass(UserLocation.class)
                        .withUpdateRequest(updateRequest).build();

                //更新数据
                this.elasticsearchTemplate.update(updateQuery);
            }

            return true;
        } catch (Exception e) {
            log.error("更新地理位置失败~ userId = " + userId + ", longitude = " + longitude + ", latitude = " + latitude + ", address = " + address, e);
        }

        return false;
    }

    /**
     * 查询用户的位置信息
     *
     * @param userId
     * @return
     */
    @Override
    public UserLocationVo queryByUserId(Long userId) {

        GetQuery getQuery = new GetQuery();
        getQuery.setId(String.valueOf(userId));
        UserLocation userLocation = this.elasticsearchTemplate.queryForObject(getQuery, UserLocation.class);
        if(ObjectUtil.isNotEmpty(userLocation)){
            return UserLocationVo.format(userLocation);
        }

        return null;
    }

    /**
     * 根据位置搜索
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @param distance  距离(米)
     * @param page      页数
     * @param pageSize  页面大小
     */
    @Override
    public PageInfo<UserLocationVo> queryUserFromLocation(Double longitude, Double latitude, Double distance, Integer page, Integer pageSize) {
        PageInfo<UserLocationVo> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);

        String fieldName = "location";

        //实现了SearchQuery接口，构造分页、排序
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();

        //分页
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        searchQueryBuilder.withPageable(pageRequest);

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //以一个点为中心，指定范围查询
        GeoDistanceQueryBuilder geoDistanceQueryBuilder = new GeoDistanceQueryBuilder(fieldName);
        //中心点
        geoDistanceQueryBuilder.point(new GeoPoint(latitude, longitude));
        //距离（画圆的半径）单位：公里
        geoDistanceQueryBuilder.distance(distance / 1000, DistanceUnit.KILOMETERS);

        boolQueryBuilder.must(geoDistanceQueryBuilder);
        searchQueryBuilder.withQuery(boolQueryBuilder);

        //排序，由近到远排序
        GeoDistanceSortBuilder geoDistanceSortBuilder = new GeoDistanceSortBuilder(fieldName, latitude, longitude);
        geoDistanceSortBuilder.order(SortOrder.ASC); //正序排序
        geoDistanceSortBuilder.unit(DistanceUnit.KILOMETERS); //设置单位
        searchQueryBuilder.withSort(geoDistanceSortBuilder);

        AggregatedPage<UserLocation> aggregatedPage = this.elasticsearchTemplate.queryForPage(searchQueryBuilder.build(), UserLocation.class);
        if(CollUtil.isEmpty(aggregatedPage.getContent())){
            return pageInfo;
        }

        pageInfo.setRecords(UserLocationVo.formatToList(aggregatedPage.getContent()));

        return pageInfo;
    }
}

