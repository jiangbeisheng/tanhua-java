package com.tanhua.common.pojo;

import com.tanhua.common.enums.SexEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo extends BasePojo{

    private Long id;
    private Long userId; //用户id
    private String nickName;
    private String logo;
    private String tags;
    private SexEnum sex;
    private Integer age;
    private String edu;
    private String city;
    private String birthday;
    private String coverPic;
    private String industry;
    private String income;
    private String marriage;
}
