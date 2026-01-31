package bg.sofia.uni.fmi.mjt.food.client.message.barcode;

import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeParsingException;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class BarcodeParser {
    public static String parseBarcode(String path) throws BarcodeParsingException {
        try {
            BufferedImage image = ImageIO.read(Path.of(path).toFile());
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap bitmap = new BinaryBitmap(binarizer);
            Result result = new MultiFormatReader().decode(bitmap);

            return result.getText();
        } catch (IOException | NotFoundException e) {
            throw new BarcodeParsingException("Couldnt parse barcode from " + path, e);
        }
    }
}
