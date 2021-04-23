package com.neo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neo.domain.Comments;
import com.neo.service.CommentsService;
import com.neo.mapper.CommentsMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments>
        implements CommentsService {

}
