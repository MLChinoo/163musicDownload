package zonas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class Config {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final Path defaultConfigPath = Path.of("config.json");

    public String cookies = "";  // 下载时所用的cookie，填写此项可以解锁已购歌曲及更高音质
    public InfoTags infotags = new InfoTags();
    public Cover cover = new Cover();
    // public Lyric lyric = new Lyric();

    public static class InfoTags {
        public boolean addInFile = true;  // 将歌曲信息内嵌进歌曲文件中
    }
    public static class Cover {
        public boolean addInFile = true;  // 将专辑图内嵌进歌曲文件中
        public boolean convertToJpg = false;  // 将专辑图格式转换为通用的JPG格式（某些MP3只能读取JPG格式的专辑图）
        public boolean saveAsFile = false;  // 将专辑图保存为歌曲同名图片文件（格式受convertToJpg影响）
    }
    /* public static class Lyric {
        public boolean addInFile = true;  // 将歌词内嵌进歌曲文件中
        public boolean saveAsFile = false;  // 将歌词保存为歌曲同名.lrc文件
    } */

    public static Config load(Path path) throws IOException {
        if (!Files.exists(path))
            throw new NoSuchFileException(path.toString());
        return gson.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), Config.class);
    }
    public static void save(Path path, Config config) throws IOException {
        try (FileWriter file = new FileWriter(path.toFile())) {
            file.write(gson.toJson(config));
        }
    }
}
