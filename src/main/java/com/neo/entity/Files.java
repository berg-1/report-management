package com.neo.entity;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Berg
 * @since 2021-04-21
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode()
@ApiModel(value = "Files对象")
public class Files implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String filename;

    private String type;

    private byte[] data;

}
