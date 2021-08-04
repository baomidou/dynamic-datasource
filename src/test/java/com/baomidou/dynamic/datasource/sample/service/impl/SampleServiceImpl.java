package com.baomidou.dynamic.datasource.sample.service.impl;

import com.baomidou.dynamic.datasource.annotation.Slave;
import com.baomidou.dynamic.datasource.sample.entity.Sample;
import com.baomidou.dynamic.datasource.sample.mapper.AnnotationSampleMapper;
import com.baomidou.dynamic.datasource.sample.mapper.SampleMapper;
import com.baomidou.dynamic.datasource.sample.service.SampleService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Sample Service
 * <p>
 *
 * @author junjun
 */
@Service
@Slave
public class SampleServiceImpl implements SampleService {

    @Autowired
    private SampleMapper sampleMapper;

    @Autowired
    private AnnotationSampleMapper annotationSampleMapper;

    @Override
    public void annotationService() {
        sampleMapper.selectCount(Wrappers.<Sample>emptyWrapper());
    }

    @Override
    public void annotationMapper() {
        annotationSampleMapper.selectCount(Wrappers.<Sample>emptyWrapper());
    }
}
