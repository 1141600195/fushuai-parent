/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: RoleMenuInfo
 * Author:   康鸿
 * Date:     2019/8/12 17:10
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.kh.pojo.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/12
 * @since 1.0.0
 * Description 
 */
@Data
@Entity
@Table(name = "base_role_menu")
public class RoleMenuInfo {
    @Id
    @Column(name = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Column(name = "roleId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;

    @Column(name = "menuId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long menuId;
}