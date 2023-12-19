package zonas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class App {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.print("请选择下载类型（1.歌单id 2.歌曲id）：");
        String temp = sc.nextLine();
        List<Map<String, Object>> playlist = new ArrayList<>();
        String ids;
        if ("1".equals(temp)) {
            System.out.print("请输入歌单id（仅限单id）：");
            String playId = sc.nextLine();
            ids = API.playlist_detail(playId, playlist);
        } else if ("2".equals(temp)) {
            System.out.print("请输入歌曲id（支持多个id用','隔开）：");
            String songIds = sc.nextLine();
            ids = API.song_detail(songIds, playlist);
        } else {
            System.out.println("请输入正确类型");
            return;
        }
        API.song_url(ids, playlist);
        System.out.println("解析完成，共下载 " + playlist.size() + " 首歌曲");
        if (!Util.createDir("music163")) {
            System.out.println("创建文件夹失败，退出下载");
            return;
        }
        int counter = 1;
        for (Map<String, Object> map : playlist) {
            System.out.println();
            String filename = map.get("filename").toString();
            Object type = map.get("type");
            if (map.get("url") != null && type != null) {
                filename = "%s.%s".formatted(filename, type.toString());
                try {
                    System.out.println("┌ （%d/%d）正在下载：".formatted(counter, playlist.size()) + Util.formatFilePath(filename));
                    byte[] data = Util.fileDownload(map.get("url").toString());
                    String filepath = "music163" + File.separator + Util.formatFilePath(filename);
                    FileOutputStream outputStream = new FileOutputStream(filepath);
                    outputStream.write(data);
                    System.out.println("├────下载成功 √");
                    outputStream.close();

                    if (AudioTag.setAudioTag(filepath, map.get("name").toString(), map.get("singer").toString(),
                            map.get("album").toString(), map.get("picurl").toString()))
                    {
                        System.out.println("└────添加标签成功 √");
                    } else {
                        System.out.println("└────添加标签失败 ×");
                    }
                } catch (IOException e) {
                    System.out.println("无法创建歌曲文件：" + filename);
                }
            } else {
                System.out.println("无法获取下载地址：" + filename);
            }
            counter++;
        }
        System.exit(0);
    }
}
