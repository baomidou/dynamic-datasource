package com.baomidou.dynamic.datasource.sample.mapper;

import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.dynamic.datasource.sample.entity.Sample;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper with {@code master} Annotation
 * <p>
 *
 * @author junjun
 */
@Master
@Mapper
public interface AnnotationSampleMapper extends BaseMapper<Sample> {
}
