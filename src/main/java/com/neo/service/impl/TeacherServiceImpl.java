package com.neo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neo.domain.Teacher;
import com.neo.service.TeacherService;
import com.neo.mapper.TeacherMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher>
        implements TeacherService {

}
