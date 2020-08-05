package com.controller;

import com.service.GeoTiffService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Api(value = "将一幅影像按瓦片进行读取，并返回给客户端，使用web 墨卡托投影输出")
public class TilesController {

    @Autowired
    GeoTiffService geoTiffService;

    @CrossOrigin(origins = "*", maxAge = 3600, methods = {RequestMethod.GET})
    //@ResponseBody
    @GetMapping("/img/{z}/{x}/{y}")
    @ApiOperation(value = "获取瓦片", notes = "测试实时生成瓦片")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "type", value = "层1名", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "x", value = "行号", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "y", value = "列号", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "z", value = "级别", required = true, dataType = "Integer")
    })
    public void spatial(@PathVariable int x, @PathVariable int y, @PathVariable int z) throws IOException {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setContentType("image/jpeg");

        geoTiffService.getTile(x, y, z, response);
    }

}
