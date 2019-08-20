/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: UserInfo
 * Author:   康鸿
 * Date:     2019/8/5 17:29
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.kh.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kh.pojo.base.BaseAuditable;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/5
 * @since 1.0.0
 * Description
 */
@Data
@Entity
@Table(name = "base_user")
public class UserInfo extends BaseAuditable {

    @Column(name = "userName")
    private String userName;

    @Column(name = "loginName")
    private String loginName;

    @Column(name = "password")
    private String password;

    @Column(name = "tel")
    private String tel;

    @Column(name = "sex")
    private int sex;

    @Column(name = "parentId")
    private Long parentId;

    @Transient
    private String roleName;

    @Column(name = "email")
    private String email;

    @Column(name = "path")
    private String path;


    //实体类中使用了@Table注解后，想要添加表中不存在的字段，
    // 就要使用@Transient这个注解了。
    @Transient
    private Map<String, String> authmap;

    @Transient
    private List<MenuInfo> listMenuInfo;

    @Transient
    private RoleInfo roleInfo;
}