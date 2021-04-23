package com.neo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Berg
 */
@Getter
@AllArgsConstructor
public class LargeFileException extends Exception {
    private final Long maxSize;
}
