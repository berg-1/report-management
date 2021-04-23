package com.neo.service.impl;

import com.neo.domain.Files;
import com.neo.mapper.FilesMapper;
import com.neo.service.FilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Berg
 * @since 2021-04-21
 */
@Service
public class FilesServiceImpl extends ServiceImpl<FilesMapper, Files> implements FilesService {

}
