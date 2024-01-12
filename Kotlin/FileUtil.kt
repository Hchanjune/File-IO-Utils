package com.kotlin.spring.management.utils.FileUtils

import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import com.kotlin.spring.management.utils.FileUtils.FileIOStatus.*

/**
 * Utility Static Class For File IO For Kotlin
 *
 * Requires [FileIOStatus] - enum Class For Status Return
 *
 * Version 1.0.0
 *
 * Basic File IO Function Utils
 *
 *
 * @author hc
 *
 * @since 24.01.11
 *
 */
object FileUtil {

    private val logger = LoggerFactory.getLogger(FileUtil::class.java)


    /**
     * 바이트 파일 저장
     *
     * 바이트코드로 이루어진 파일을 [targetDirectory]에 저장
     *
     * Saving ByteFile to [targetDirectory]
     *
     * @param targetDirectory 파일을 저장할 경로 - Directory which File will be saved
     * @param fileName 저장될 파일명 (확장자 포함) - FileName Including ExtensionString
     * @param bytes 바이트 (파일) - ByteFile to Save
     * @return [FileIOStatus]
     * @throws [Exception][IOException]
     * @author hc
     * @since 24.01.11
     *
     */
    fun saveByteFile(
        targetDirectory: String,
        directoryOverwriteFlag: Boolean,
        fileName: String,
        bytes: ByteArray,
        fileOverwriteFlag: Boolean
    ): FileIOStatus {
        // Validate Target Directory
        if (targetDirectory.trim().isEmpty()) return INVALID_PATH
        // Validate FileName
        if (fileName.trim().isEmpty()) return  INVALID_FILENAME
        // Validate File Size
        if (bytes.isEmpty()) return EMPTY_FILE

        // Directory Setting
        val directoryPath = Paths.get(targetDirectory).toAbsolutePath().normalize()
        val filePath = directoryPath.resolve(fileName).toAbsolutePath().normalize()

        // Directory Option
        if (directoryOverwriteFlag) {
            if (!Files.exists(directoryPath)) Files.createDirectory(directoryPath)
        } else {
            if (!Files.exists(directoryPath)) return PATH_DOSE_NOT_EXIST
        }

        // File Option
        return try {
            if (fileOverwriteFlag || !Files.exists(filePath)) {
                Files.write(filePath, bytes)
                SUCCESS
            } else {
                FILE_ALREADY_EXISTS
            }
        }catch (ioException: IOException){
            logger.error(ioException.message)
            ioException.printStackTrace()
            IO_EXCEPTION
        }catch (exception: Exception){
            logger.error(exception.message)
            exception.printStackTrace()
            UNKNOWN_ERROR
        }
    }

    /**
     * 멀티파트파일 파일 저장
     *
     * 멀티파트파일 형식 파일을 [targetDirectory]에 저장
     *
     * Saving MultipartFile Type File to [targetDirectory]
     *
     * @param targetDirectory 파일을 저장할 경로 - Directory which File will be saved
     * @param fileName 저장될 파일명 (확장자 포함) - FileName Including ExtensionString
     * @param multipartFile 파일 - MultipartFile to Save
     * @throws [Exception][IOException]
     * @return [FileIOStatus]
     * @author hc
     * @since 24.01.11
     */
    fun saveMultipartFile(
        targetDirectory: String,
        directoryOverwriteFlag: Boolean,
        fileName: String,
        multipartFile: MultipartFile,
        fileOverwriteFlag: Boolean
    ): FileIOStatus {
        // Validate Target Directory
        if (targetDirectory.trim().isEmpty()) return INVALID_PATH
        // Validate FileName
        if (fileName.trim().isEmpty()) return INVALID_FILENAME
        // Validate File Content
        if (multipartFile.isEmpty) return EMPTY_FILE

        // Directory Setting
        val directoryPath = Paths.get(targetDirectory).toAbsolutePath().normalize()
        val filePath = directoryPath.resolve(fileName).toAbsolutePath().normalize()

        // Directory Option
        if (directoryOverwriteFlag) {
            if (!Files.exists(directoryPath)) Files.createDirectory(directoryPath)
        } else {
            if (!Files.exists(directoryPath)) return PATH_DOSE_NOT_EXIST
        }

        // File Option
        return try {
            if (fileOverwriteFlag || !Files.exists(filePath)) {
                multipartFile.inputStream.use { inputStream ->
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
                }
                SUCCESS
            } else {
                FILE_ALREADY_EXISTS
            }
        } catch (ioException: IOException) {
            logger.error(ioException.message)
            ioException.printStackTrace()
            IO_EXCEPTION
        }catch (exception: Exception){
            logger.error(exception.message)
            exception.printStackTrace()
            UNKNOWN_ERROR
        }
    }

    /**
     * 파일 삭제 (확장자 옵션 ON/OFF 필수)
     *
     * -확장자 옵션 ON : 경로([targetDirectory])내의 파일명(확장자 포함)이 정확하게 일치하는 파일 삭제
     *
     * -확장자 옵션 OFF : 경로([targetDirectory])내의 파일명이 일치하는 모든 파일 삭제
     *
     * File Delete (Extension Check Option Must be Checked)
     *
     * -Extension Option ON : Deletes File Which has Exact Same Name With Including Extension In Directory([targetDirectory])
     *
     * -Extension Option ON : Deletes All Files In Certain Directory([targetDirectory]) Matches The Keyword
     *
     * @param targetDirectory : 파일이 저장된 경로
     * @param fileName : 삭제할 파일명
     * @param extensionOption : 확장자 옵션 true/false
     * @return [FileIOStatus]
     * @throws [Exception][IOException]
     * @author hc
     * @since 24.01.11
     *
     */
    fun deleteFile(
        targetDirectory: String,
        fileName: String,
        extensionOption: Boolean
    ): FileIOStatus {
        // Validate Target Directory
        if (targetDirectory.trim().isEmpty()) return INVALID_PATH
        // Validate FileName
        if (fileName.trim().isEmpty()) return INVALID_FILENAME
        // Directory Setting
        val directoryPath = Paths.get(targetDirectory).toAbsolutePath().normalize()
        val filePath = directoryPath.resolve(fileName).toAbsolutePath().normalize()

        return try {
            if (extensionOption){
                /** Delete The Only One File Exactly Matches the fileName */
                // Deletes File If It Exists
                if (!Files.deleteIfExists(filePath)) FILE_DOSE_NOT_EXIST else SUCCESS
            } else {
                // Delete All Files Including fileName
                val fileNameWithoutExtension = this.extractFilenameWithoutExtension(fileName)
                val fileList = Files.list(directoryPath)
                fileList.use {fileList ->
                    fileList.filter {
                        it.fileName.toString().startsWith(fileNameWithoutExtension)
                    }.forEach {
                        Files.deleteIfExists(it)
                    }
                }
                SUCCESS
            }
        } catch (ioException: IOException){
            logger.error(ioException.message)
            ioException.printStackTrace()
            IO_EXCEPTION
        } catch (exception: Exception){
            logger.error(exception.message)
            exception.printStackTrace()
            UNKNOWN_ERROR
        }
    }

    /**
     * 파일 확장자 추출
     *
     * 가장 마지막 Period 마크 기준 문자열을 분리하여 확장자 반환
     *
     * Excludes Extension String by last index of Period Mark
     *
     * @param fileName 확장자를 포함한 파일명 (fileName Including Extension)
     * @return [String] - 확장자 문자열 리턴 (Returns Extension String)
     * @author hc
     * @since 24.01.11
     * @exception [String] 파일에 확장자가 존재하지 않을시, 빈 문자열 반환 - returns empty String when There is No Extension
     *
     */
    fun extractFileExtension(fileName: String): String {
        val trimmedFileName = fileName.trim()
        val periodIndex = trimmedFileName.lastIndexOf('.')
        return if (periodIndex >= 0) {
            fileName.substring(periodIndex + 1).lowercase()
        } else ""
    }

    /**
     * 파일명 추출
     *
     * 가장 마지막 Period 마크 기준 문자열을 분리하여 파일명 반환
     *
     * Excludes fileName From Full Filename Including Extension
     *
     * @param fileName 확장자를 포함한 파일명 - fileName Including Extension
     * @return [String] - 확장자를 제거한 파일명 - fileName Excluded From Full Filename
     * @author hc
     * @since 24.01.11
     * @exception [String] 파일에 확장자가 존재하지 않을시, [fileName] 반환 - returns Parameter It Self when There is No Extension
     *
     */
    fun extractFilenameWithoutExtension(fileName: String): String {
        val lastIndexOfPeriod = fileName.lastIndexOf('.')
        if (lastIndexOfPeriod <= 0) return fileName
        return fileName.substring(0, lastIndexOfPeriod)
    }




}