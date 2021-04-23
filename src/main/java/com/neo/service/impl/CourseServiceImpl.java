package com.neo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neo.domain.Course;
import com.neo.mapper.CourseMapper;
import com.neo.service.CourseService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
        implements CourseService {

}
