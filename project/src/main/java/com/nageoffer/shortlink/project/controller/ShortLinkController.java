package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @PostMapping("/api/shortlink/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestparam) {
        return Results.success(shortLinkService.createShortLink(requestparam));
    }

    @GetMapping("/api/shortlink/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> Page(ShortLinkPageReqDTO requestparam) {
        return Results.success(shortLinkService.page(requestparam));
    }

    @GetMapping("/api/shortlink/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> countByGids(@RequestParam List<String> gids) {
        // SELECT gid, COUNT(*) FROM t_link WHERE gid IN (...) AND del_flag = 0 GROUP BY gid
        return Results.success(shortLinkService.countByGids(gids));
    }
}
