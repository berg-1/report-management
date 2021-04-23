package com.neo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neo.domain.Student;
import com.neo.service.StudentService;
import com.neo.mapper.StudentMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>
        implements StudentService {

}
