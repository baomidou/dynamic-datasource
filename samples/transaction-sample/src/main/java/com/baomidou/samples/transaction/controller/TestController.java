package com.baomidou.samples.transaction.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.transaction.manager.TestManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Hccake 2020/9/3
 * @version 1.0
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/")
public class TestController {
    private final TestManager testManager;

    @DS("master")
    @GetMapping("test")
    public Map<String, List<?>> test(@RequestParam(name = "error", defaultValue = "false") boolean thorwError) {
        try {
            testManager.insertAllDb(thorwError);
        } catch (Exception e) {
            log.error("========抛出异常回滚==========");
        }
        return testManager.selectAllDb();
    }

    @GetMapping("truncate")
    public String truncate() {
        testManager.truncateAll();
        return "all table truncate";
    }

    @GetMapping("getAll")
    public Map<String, List<?>> getAll() {
        return testManager.selectAllDb();
    }

}
