package com.baomidou.dynamic.datasource.multi;

import com.baomidou.dynamic.datasource.sample.entity.Sample;
import com.baomidou.dynamic.datasource.sample.mapper.SampleMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;


/**
 * Test Mapper
 * <p>
 *
 * @author junjun
 */
@MybatisPlusTest
@RunWith(SpringRunner.class)
public class SimplyMapperTest {

    @Autowired
    private SampleMapper sampleMapper;

    @Test
    public void simplyTest() {
        Sample sample = new Sample();
        sample.setId(1L);
        sample.setName("JunJun");
        int row = sampleMapper.insert(sample);
        assertThat(row).isEqualTo(1);

        Integer count = sampleMapper.selectCount(Wrappers.<Sample>emptyWrapper());
        assertThat(count).isEqualTo(1);
    }

}
