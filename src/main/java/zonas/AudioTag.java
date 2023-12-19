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
import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static zonas.App.config;

public class AudioTag {
    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);;
    }

    public static boolean setAudioTag(String filepath, String name, String artist, String album, String picurl) {
        try {
            AudioFile audioFile = AudioFileIO.read(new File(filepath));
            Tag tag = audioFile.getTag();

            if (config.infotags.addInFile) {
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
            }

            if (config.cover.addInFile || config.cover.saveAsFile) {
                byte[] picData = Util.fileDownload(picurl);
                String mimeType = ImageFormats.getMimeTypeForBinarySignature(picData);
                switch (mimeType) {
                    case ImageFormats.MIME_TYPE_JPEG:
                    case ImageFormats.MIME_TYPE_JPG:
                        break;
                    case ImageFormats.MIME_TYPE_PNG:
                        if (config.cover.convertToJpg)
                            picData = convertPNGtoJPG(picData);
                        break;
                    default:
                        System.out.println("Unsupported mimeType: " + mimeType);
                        picData = null;
                }
                if (picData != null) {
                    Artwork artwork = new Artwork();
                    artwork.setBinaryData(picData);
                    artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(artwork.getBinaryData()));
                    artwork.setPictureType(PictureTypes.DEFAULT_ID);
                    if (config.cover.addInFile) {
                        tag.setField(artwork);
                    }
                    if (config.cover.saveAsFile) {
                        try (FileOutputStream file = new FileOutputStream(Path.of(Util.changeFileExtension(filepath, ImageFormats.getFormatForMimeType(artwork.getMimeType()).toLowerCase())).toFile())) {
                            file.write(artwork.getBinaryData());
                        }
                    }
                }
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
