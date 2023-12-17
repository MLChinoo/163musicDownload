package zonas;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioTag {
    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);;
    }

    public static boolean setAudioTag(String filepath, String name, String artist, String album, String picurl) {
        try {
            AudioFile audioFile = AudioFileIO.read(new File(filepath));
            Tag tag = audioFile.getTag();

            Map<FieldKey, String> tags = new HashMap<>() {{
                put(FieldKey.TITLE, name);
                put(FieldKey.ARTIST, artist.replaceAll(",", "/"));  // jaudiotagger中默认使用/作为分隔符添加多个艺术家
                put(FieldKey.ALBUM, album);
                // Artwork专辑图单独处理
            }};
            tags.forEach((key, value) -> {
                tag.deleteField(key);
                try {
                    tag.addField(key, value);
                } catch (FieldDataInvalidException e) {
                    e.printStackTrace();
                }
            });

            Artwork artwork = new Artwork();
            artwork.setMimeType("image/jpeg");
            artwork.setBinaryData(Util.fileDownload(picurl));
            tag.setField(artwork);

            audioFile.setTag(tag);
            audioFile.commit();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
