package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupListRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.remote.service.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.util.RandomUtil;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    private final ShortLinkRemoteService shortLinkRemoteService;

    public void saveGroup(String groupName) {
//        String gid;
//        do{
//            gid = RandomUtil.generateRandomString(6);
//        }while (!hasGid(gid));
//        GroupDO groupDO = new GroupDO();
//        groupDO.setGid(gid);
//        groupDO.setUsername(UserContext.getUsername());
//        groupDO.setName(groupName);
//        groupDO.setSortOrder(0);
//        baseMapper.insert(groupDO);
        saveGroup(UserContext.getUsername(),groupName);
    }

    @Override
    public void saveGroup(String userName, String groupName) {
        String gid;
        do{
            gid = RandomUtil.generateRandomString(6);
        }while (!hasGid(userName,gid));
        GroupDO groupDO = new GroupDO();
        groupDO.setGid(gid);
        groupDO.setUsername(userName);
        groupDO.setName(groupName);
        groupDO.setSortOrder(0);
        baseMapper.insert(groupDO);
    }

    @Override
    public List<ShortLinkGroupListRespDTO> listGroup() {
        //todo
        List<GroupDO> list = lambdaQuery().eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime)
                .list();
        if (list==null) {
            return Collections.emptyList();
        }
        List<String> gids = list.stream().map(GroupDO::getGid).toList();
        Result<List<ShortLinkGroupCountQueryRespDTO>> shortLinkGroupCountResult = shortLinkRemoteService.listGroupShortLinkCount(gids);
        List<ShortLinkGroupCountQueryRespDTO> shortLinkGroupCount = shortLinkGroupCountResult.getData();

        Map<String, Integer> countMap = (shortLinkGroupCount == null || shortLinkGroupCount.isEmpty())
                ? Collections.emptyMap()
                : shortLinkGroupCount.stream()
                .collect(Collectors.toMap(
                        ShortLinkGroupCountQueryRespDTO::getGid,
                        ShortLinkGroupCountQueryRespDTO::getShortLinkGroupCount
                ));
        List<ShortLinkGroupListRespDTO> shortLinkGroupListRespDTOS = BeanUtil.copyToList(list, ShortLinkGroupListRespDTO.class);
        shortLinkGroupListRespDTOS.forEach(group ->
                group.setShortLinkCount(
                        countMap.getOrDefault(group.getGid(), 0)
                )
        );
        return shortLinkGroupListRespDTOS;
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestparam) {
        lambdaUpdate()
                .eq(GroupDO::getGid,requestparam.getGid())
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .eq(GroupDO::getDelFlag,0)
                .set(GroupDO::getName,requestparam.getName())
                .update();
    }

    @Override
    public void deleteGroup(String gid) {
        lambdaUpdate()
                .eq(GroupDO::getGid,gid)
                .set(GroupDO::getDelFlag,1)
                .update();
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        for (ShortLinkGroupSortReqDTO shortLinkGroupSortReqDTO : requestParam) {
            lambdaUpdate().eq(GroupDO::getGid,shortLinkGroupSortReqDTO.getGid())
                    .eq(GroupDO::getUsername,UserContext.getUsername())
                    .eq(GroupDO::getDelFlag,0)
                    .set(GroupDO::getSortOrder,shortLinkGroupSortReqDTO.getSortOrder())
                    .update();
        }

        Object o = new Object();
        o.hashCode();
    }

    private boolean hasGid(String userName,String gid) {
        GroupDO one = lambdaQuery().eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, userName)
                .one();
        return one == null;
    }
}
