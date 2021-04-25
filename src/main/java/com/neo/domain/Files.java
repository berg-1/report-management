package com.neo.domain;

import java.io.Serializable;

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
public class Files implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String filename;

    private String type;

    private byte[] data;

}
