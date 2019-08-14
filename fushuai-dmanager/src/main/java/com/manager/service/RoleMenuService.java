package com.manager.service;
/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: RoleMenuService
 * Author:   康鸿
 * Date:     2019/8/12 20:41
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */


import com.manager.dao.RoleMenuDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/12
 * @since 1.0.0
 * Description
 */
@Service
@Transactional
public class RoleMenuService {
    @Autowired
    private RoleMenuDao roleMenuDao;

    public void deleteByRoleId(Long roleId) throws Exception {
        roleMenuDao.deleteByRoleId(roleId);
    }
}