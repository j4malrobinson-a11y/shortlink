package com.nageoffer.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.util.RandomUtil;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    public void saveGroup(String groupName) {
        String gid;
        do{
            gid = RandomUtil.generateRandomString(6);
        }while (!hasGid(gid));
        GroupDO groupDO = new GroupDO();
        groupDO.setGid(gid);
        groupDO.setName(groupName);
        baseMapper.insert(groupDO);
    }

    private boolean hasGid(String gid) {
        GroupDO one = lambdaQuery().eq(GroupDO::getGid, gid)
                //todo 设置用户名
                .eq(GroupDO::getUsername, null)
                .one();
        return one == null;
    }
}
