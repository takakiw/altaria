package com.altaria.common.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum FileType {
    DIRECTORY(0),
    IMAGE(1,
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/tiff",
            "image/webp",
            "image/svg+xml"),

    VIDEO(2,
            "video/mp4",
            "video/x-msvideo",
            "video/quicktime",
            "video/x-flv",
            "video/x-matroska",
            "video/webm",
            "video/mpeg"),

    AUDIO(3,
            "audio/mpeg",
            "audio/wav",
            "audio/ogg",
            "audio/aac",
            "audio/flac",
            "audio/x-ms-wma"),

    PDF(4,
            "application/pdf",
            "application/x-pdf",
            "application/force-download"),

    WORD(5,
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.wordperfect",
            "application/vnd.oasis.opendocument.text"),

    EXCEL(6,
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.lotus-123"),

    TEXT(7,
            "text/plain",
            "text/html",
            "text/xml",
            "application/json",
            "application/xml",
            "application/javascript",
            "text/css",
            "text/csv",
            "text/yaml"),

    OTHER(8,
            "application/octet-stream",
            "application/x-zip-compressed",
            "application/zip",
            "application/x-rar-compressed",
            "application/x-7z-compressed");


    private final int type;
    private final Set<String> mimeTypes;

    FileType(int type, String... mimeTypes) {
        this.type = type;
        this.mimeTypes = new HashSet<>(Arrays.asList(mimeTypes));
    }

    public boolean matches(String contentType) {
        return mimeTypes.isEmpty() || mimeTypes.contains(contentType.toLowerCase());
    }

    public int getType() {
        return type; // 返回指定的类型代码
    }

    public static FileType getFileType(String contentType) {
        for (FileType fileType : FileType.values()) {
            if (fileType.matches(contentType)) {
                return fileType;
            }
        }
        return OTHER; // 默认类型
    }
}
