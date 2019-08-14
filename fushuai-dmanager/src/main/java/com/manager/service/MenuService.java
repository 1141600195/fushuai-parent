
package com.manager.service;

import java.util.List; /**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: MenuService
 * Author:   康鸿
 * Date:     2019/8/12 15:06
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */


import com.kh.pojo.entity.MenuInfo;
import com.manager.dao.MenuDao;
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
public class MenuService {
    @Autowired
    private MenuDao MenuDao;

    //获得权限列表
    public List<MenuInfo> getForMenuInfo(Long mid) {
        List<MenuInfo> firstMenuInfo = MenuDao.findByParentId(mid);
        if (firstMenuInfo != null) {

            for (MenuInfo menuInfo : firstMenuInfo) {
                //如果等级小于等于三 则继续查
                if (menuInfo.getLeval() <= 3) {
                    menuInfo.setMenuInfoList(getForMenuInfo(menuInfo.getId()));
                }
            }
        }
        return firstMenuInfo;
    }
}