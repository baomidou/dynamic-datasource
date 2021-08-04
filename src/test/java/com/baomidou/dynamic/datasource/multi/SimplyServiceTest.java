package com.baomidou.dynamic.datasource.multi;

import com.baomidou.dynamic.datasource.sample.service.SampleService;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test Multi Datasource Simply
 * <p>
 *
 * @author junjun
 */
@RunWith(SpringRunner.class)
@MybatisPlusTest
public class SimplyServiceTest {

    @Autowired
    private SampleService sampleService;

    /**
     * {@linkplain com.baomidou.dynamic.datasource.annotation.DS @DS} Annotation Service
     *
     */
    @Test
    public void annotationServiceTest() {
        sampleService.annotationService();
    }

    /**
     * {@linkplain com.baomidou.dynamic.datasource.annotation.DS @DS} Annotation Mapper
     *
     */
    @Test
    public void annotationMapperTest() {
        sampleService.annotationMapper();
    }
}
