package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: HanXu
 * on 2021/8/11
 * Class description: 服务健康检查
 */
@RestController
public class HsController {

    @RequestMapping("hs")
    public String hs () {
        return "OK";
    }

}
