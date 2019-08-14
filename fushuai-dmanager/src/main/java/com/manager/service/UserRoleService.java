/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: UserRoleService
 * Author:   康鸿
 * Date:     2019/8/9 20:31
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.manager.service;

import com.kh.pojo.entity.UserRoleInfo;
import com.manager.dao.UserRoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/9
 * @since 1.0.0
 * Description
 */
@Service
@Transactional
public class UserRoleService {
    @Autowired
    private UserRoleDao userRoleDao;


    //用户绑定角色
    public int adduserRole(UserRoleInfo ur) {
        userRoleDao.save(ur);
        return 200;
    }

    //查找
    public UserRoleInfo selectUserRole(Long userid) {
        UserRoleInfo userRoleInfo = userRoleDao.selectUserByUserId(userid);
        return userRoleInfo;
    }

    //通过角色id查找
    public UserRoleInfo selectUserRoleByRoleId(Long roleId) {
        UserRoleInfo userRoleInfo = userRoleDao.findByRoleId(roleId);
        return userRoleInfo;
    }


    //通过用户id删除
    public void deleteUserRoleByUserId(Long userId) throws Exception {
        userRoleDao.deleteByUserId(userId);
    }

}