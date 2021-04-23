package com.neo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neo.domain.Report;
import com.neo.service.ReportService;
import com.neo.mapper.ReportMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report>
        implements ReportService {

}
