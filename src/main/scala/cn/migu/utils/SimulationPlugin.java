package cn.migu.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * 插件异常必须抛出
 */
public class SimulationPlugin {

    public String foo(String srcPath, String targetPath) throws IOException {

        System.out.println("\u001b[36;1m"+srcPath + "--->模拟插件运行--->" + targetPath+"\u001b[0m\n");
        FileUtils.copyFile(new File(srcPath), new File(targetPath));
        return targetPath;
    }
}
