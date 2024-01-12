package com.kotlin.spring.management.utils.FileUtils

enum class FileIOStatus {
    SUCCESS,
    INVALID_PATH,
    INVALID_FILENAME,
    PATH_DOSE_NOT_EXIST,
    FILE_DOSE_NOT_EXIST,
    EMPTY_FILE,
    SECURITY_ISSUE,
    FILE_ALREADY_EXISTS,
    IO_EXCEPTION,
    UNKNOWN_ERROR,
    NO_EXTENSION;

}