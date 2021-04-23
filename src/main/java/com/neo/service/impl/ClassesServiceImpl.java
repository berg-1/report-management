package com.neo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neo.domain.Classes;
import com.neo.service.ClassesService;
import com.neo.mapper.ClassesMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class ClassesServiceImpl extends ServiceImpl<ClassesMapper, Classes>
        implements ClassesService {

}
