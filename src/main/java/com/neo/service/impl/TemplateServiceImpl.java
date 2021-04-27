package com.neo.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neo.domain.Template;
import com.neo.service.TemplateService;
import com.neo.mapper.TemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, Template>
        implements TemplateService {
}
