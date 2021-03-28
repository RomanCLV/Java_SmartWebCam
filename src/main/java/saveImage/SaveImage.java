package saveImage;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import utils.ImageDescription;

public class SaveImage {

    private final int minPercentage;
    private final ArrayList<String> labelsSelected;
    private final String filePath;

    /**
     * SaveImage constructor
     * @param minPercentage The minimum percentage expected to record a picture
     * @param labelsSelected The labels of the picture
     * @param filePath The path to save the picture
     */
    public SaveImage(int minPercentage, ArrayList<String> labelsSelected, String filePath) {
        this.minPercentage = minPercentage;
        this.labelsSelected = labelsSelected;
        this.filePath = filePath;
    }

    /**
     * Write the picture of the information given
     * @param imageDescription The information from a picture
     */
    public void save(ImageDescription imageDescription, BufferedImage bufferedImage) {

        int percentage = Math.round(imageDescription.getProbability() * 100);
        if (percentage >= this.minPercentage  && labelsSelected.contains(imageDescription.getLabel())) {
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                LocalDateTime now = LocalDateTime.now();
                String fileName = dtf.format(now) + "_" + imageDescription.getLabel() + "_" + percentage + ".jpg";
                ImageIO.write(bufferedImage, "jpg", new File(filePath + "/" + fileName));
            }
            catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
}
