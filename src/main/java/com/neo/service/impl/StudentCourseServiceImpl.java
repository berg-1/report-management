package com.neo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neo.domain.StudentCourse;
import com.neo.mapper.StudentCourseMapper;
import com.neo.service.StudentCourseService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class StudentCourseServiceImpl extends ServiceImpl<StudentCourseMapper, StudentCourse>
        implements StudentCourseService {

}
