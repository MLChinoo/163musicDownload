package zonas;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.reference.PictureTypes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
                put(FieldKey.ALBUM_ARTIST, null);
                // Artwork专辑图单独处理
            }};
            tags.forEach((key, value) -> {
                tag.deleteField(key);
                try {
                    if (value != null)
                        tag.addField(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            byte[] picData = Util.fileDownload(picurl);
            String mimeType = ImageFormats.getMimeTypeForBinarySignature(picData);
            if (!mimeType.equals("image/jpeg")) {
                if (mimeType.equals("image/png")) {
                    picData = convertPNGtoJPG(picData);
                } else {
                    System.out.println("Unsupported mimeType: " + mimeType);
                    picData = null;
                }
            }
            if (picData != null) {
                Artwork artwork = new Artwork();
                artwork.setBinaryData(picData);
                artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(artwork.getBinaryData()));
                artwork.setPictureType(PictureTypes.DEFAULT_ID);
                tag.setField(artwork);
            }

            audioFile.setTag(tag);
            audioFile.commit();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static byte[] convertPNGtoJPG(byte[] pngBytes) {
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(pngBytes);
            BufferedImage originalImage = ImageIO.read(input);

            BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newImage.createGraphics().drawImage(originalImage, 0, 0, null);
            newImage.createGraphics().dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(newImage, "jpg", output);

            return output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
