/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: RoleServer
 * Author:   康鸿
 * Date:     2019/8/9 11:01
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.manager.service;

import com.kh.pojo.entity.RoleInfo;
import com.kh.pojo.entity.RoleMenuInfo;
import com.kh.pojo.entity.UserInfo;
import com.manager.dao.MenuDao;
import com.manager.dao.RoleDao;
import com.manager.dao.RoleMenuDao;
import com.manager.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class RoleService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private RoleDao roleDao;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private RoleMenuDao roleMenuDao;


    public Object roleList(Integer page, Integer pageSize, String roleName) {

        //查询
        StringBuffer stringBuffer = new StringBuffer("select * from base_role where 1=1");
        //总条数
        StringBuffer stringBufferCount = new StringBuffer("select count(1) from base_role where 1=1");

        if (roleName != null && !roleName.equals("")) {
            //区间查询
            stringBuffer.append(" and roleName like concat('%','" + roleName + "','%')");
            //总条数
            stringBufferCount.append(" and roleName like concat('%','" + roleName + "','%')");
        }
        stringBuffer.append(" limit " + (page - 1) * pageSize + "," + pageSize);

        System.out.println("stringBuffer=" + stringBuffer);
        System.out.println("stringBufferCount=" + stringBufferCount);
        //清一下缓存
        entityManager.clear();
        //查列表
        Query nativeQuery = entityManager.createNativeQuery(stringBuffer.toString(), RoleInfo.class);


        //总条数
        Query nativeQueryCount = entityManager.createNativeQuery(stringBufferCount.toString());

        //查用户
        List<RoleInfo> resultList = nativeQuery.getResultList();
        if (resultList != null && resultList.size() > 0) {
            for (RoleInfo roleInfo : resultList) {
                List<UserInfo> userInfos = userDao.forUserInfoByUserId(roleInfo.getId());
                roleInfo.setUserInfos(userInfos);
                roleInfo.setMenuInfoList(menuDao.getRoleMenuInfo(roleInfo.getId()));

            }
        }

        Map<String, Object> map = new HashMap<String, Object>();


        //用户信息
        map.put("content", resultList);
        map.put("totalElements", nativeQueryCount.getSingleResult());
        return map;

    }


    //角色删除
    public void deleteRoleById(Long id) throws Exception {
        roleDao.deleteById(id);
    }

    //角色添加
    public void addRole(RoleInfo r)throws Exception {
        roleDao.save(r);
    }

    //角色添加
    public List<RoleInfo> bdrolelist() {
        List<RoleInfo> bdrolelist = roleDao.findAll();
        return bdrolelist;
    }

}