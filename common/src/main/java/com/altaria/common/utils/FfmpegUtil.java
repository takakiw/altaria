package com.altaria.common.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.schild.jave.process.ProcessWrapper;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;
import ws.schild.jave.process.ffmpeg.FFMPEGProcess;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class FfmpegUtil {
    private static final Logger logger = LoggerFactory.getLogger(FfmpegUtil.class);

    private final static String ffmpegPath = "D:\\environment\\ffmpeg\\bin\\ffmpeg.exe";
    private final static String ffplayPath = "D:\\environment\\ffmpeg\\bin\\ffplay.exe";
    private final static String ffprobePath = "D:\\environment\\ffmpeg\\bin\\ffprobe.exe";

    /**
     * 获取视频缩略图
     *
     * @param sourceFile 视频文件地址
     * @param width      缩略图宽度
     * @param targetFile 缩略图地址
     */
    public static void createTargetThumbnail(File sourceFile, Integer width, File targetFile) {
        // "ffmpeg -i %s -y -vframes 1 -vf scale=%d:%d/a %s"
        try {

            FFMPEGProcess ffmpeg = new FFMPEGProcess(ffmpegPath);
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(sourceFile.getAbsoluteFile().toString());
            ffmpeg.addArgument("-y");
            ffmpeg.addArgument("-vframes");
            ffmpeg.addArgument("1");
            ffmpeg.addArgument("-vf");
            ffmpeg.addArgument(String.format("scale=%d:%d/a", width, width));
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

    /**
     * 获取视频缩略图，如果源视频宽度小于指定宽度，则不压缩
     *
     * @param sourceFile 源视频地址
     * @param width      缩略图宽度
     * @param targetFile 缩略图地址
     * @param delSource  是否删除源视频
     * @return 是否生成缩略图
     */
    public static Boolean createThumbnailWidthFFmpeg(File sourceFile, Integer width, File targetFile, Boolean delSource) {
        // "ffmpeg -i %s -y -vframes 1 -vf scale=%d:%d/a %s"
        try {
            BufferedImage src = ImageIO.read(sourceFile);
            int sourceW = src.getWidth();
            // 小于指定高宽不要压缩
            if (sourceW < width) {
                return false;
            }
            compressImage(sourceFile, width, targetFile, delSource);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 压缩图片，获取图片缩略图
     *
     * @param sourceFile 源图片地址
     * @param width      图片压缩宽度
     * @param targetFile 缩略图
     * @param delSource  是否删除原图
     */
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

    /**
     * 等待命令执行成功，退出
     *
     * @param br
     * @throws IOException
     */
    private static void blockFfmpeg(BufferedReader br) throws IOException {
        String line;
        // 该方法阻塞线程，直至合成成功
        while ((line = br.readLine()) != null) {
            doNothing(line);
        }
    }

    /**
     * 空方法，用于阻塞线程
     * @param line
     */
    private static void doNothing(String line) {
        // logger.info("ffmpeg命令执行中————{}", line);
    }

}
