package com.neo.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Berg
 */
@Getter
@Setter
@AllArgsConstructor
public class Node {
    /**
     * 节点的id
     */
    private String nodeId;
    /**
     * 父节点的id
     */
    private String pid;
    /**
     * 文本数据
     */
    private String text;
    /**
     * 链接
     */
    private String href;
}
