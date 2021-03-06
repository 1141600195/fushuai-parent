/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: RoleInfo
 * Author:   康鸿
 * Date:     2019/8/6 17:13
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.kh.pojo.entity;

import com.kh.pojo.base.BaseAuditable;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/6
 * @since 1.0.0
 * Description 
 */
@Entity
@Data
@Table(name = "base_role")
public class RoleInfo extends BaseAuditable {

    @Column(name = "roleName")
    private String roleName;
    @Column(name = "miaoShu")
    private String miaoShu;
    @Transient
    private List<UserInfo> userInfos;
    @Transient
    private List<MenuInfo> menuInfoList;
    @Transient
    private String ids[];
}