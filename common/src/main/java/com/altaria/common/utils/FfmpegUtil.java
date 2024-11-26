package com.altaria.common.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.schild.jave.process.ffmpeg.FFMPEGProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class FfmpegUtil {
    private static final Logger logger = LoggerFactory.getLogger(FfmpegUtil.class);

    private final static String ffmpegPath = "ffmpeg";


    public static void createTargetThumbnail(File sourceFile, Integer width, File targetFile) {
        try {
            FFMPEGProcess ffmpeg = new FFMPEGProcess(ffmpegPath);
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(sourceFile.getAbsoluteFile().toString());
            ffmpeg.addArgument("-y");
            ffmpeg.addArgument("-f");
            ffmpeg.addArgument("image2");
            ffmpeg.addArgument("-ss");
            ffmpeg.addArgument("1");
            ffmpeg.addArgument("-t");
            ffmpeg.addArgument("0.001");
            ffmpeg.addArgument(targetFile.getAbsoluteFile().toString());
            ffmpeg.execute();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()))) {
                blockFfmpeg(br);
            }
        } catch (IOException e) {
            logger.error("生成封面失败", e);
            throw new RuntimeException("生成封面失败");
        }
    }


    public static void compressImage(File sourceFile, Integer width, File targetFile, Boolean delSource) {
        // String cmd = "ffmpeg -i %s -vf scale=%d:-1 %s -y"
        try {
            FFMPEGProcess ffmpeg = new FFMPEGProcess(ffmpegPath);
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(sourceFile.getAbsoluteFile().toString());
            ffmpeg.addArgument("-vf");
            ffmpeg.addArgument(String.format("scale=%d:-1", width));
            ffmpeg.addArgument(targetFile.getAbsolutePath());
            ffmpeg.addArgument("-y");
            ffmpeg.execute();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()))) {
                blockFfmpeg(br);
            }
            if (delSource) {
                FileUtils.deleteDirectory(sourceFile);
            }
        } catch (IOException e) {
            logger.error("压缩图片失败", e);
            throw new RuntimeException("生成封面失败");
        }
    }

    private static void blockFfmpeg(BufferedReader br) throws IOException {
        String line;
        // 该方法阻塞线程，直至合成成功
        while ((line = br.readLine()) != null) {
            doNothing(line);
        }
    }


    private static void doNothing(String line) {

    }

}
