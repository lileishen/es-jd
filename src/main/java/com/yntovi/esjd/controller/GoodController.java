package com.yntovi.esjd.controller;

import com.yntovi.esjd.servcice.GoodService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class GoodController {
    @Resource
 private GoodService goodService;
    @GetMapping("/parse/{keyword}")
    public  Boolean createIndex(@PathVariable String keyword) throws IOException {
        Boolean result = goodService.parseGood(keyword);
        return result;
    }
    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public  List<Map<String, Object>> createIndex(@PathVariable String keyword,@PathVariable int pageNo,@PathVariable int pageSize) throws IOException {
        List<Map<String, Object>> maps = goodService.searchPage(keyword, pageNo, pageSize);
        return maps;
    }
}
