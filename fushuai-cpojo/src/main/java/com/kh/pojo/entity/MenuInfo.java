/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: MenuInfo
 * Author:   康鸿
 * Date:     2019/8/6 17:05
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
@Table(name = "base_menu")
public class MenuInfo extends BaseAuditable {

    @Column(name = "menuName")
    private String menuName;

    @Column(name = "parentId")
    private Long parentId;

    @Column(name = "leval")
    private int leval;

    @Column(name = "url")
    private String url;

    @Transient
    private List<MenuInfo> menuInfoList;
    @Transient
    private Long []ids;
}