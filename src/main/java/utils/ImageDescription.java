package utils;

public class ImageDescription {

    private final int index;
    private final float probability;
    private final String label;
    private final String path;

    /**
     * ImageDescription constructor
     * @param path String path image
     * @param index int index of detection label
     * @param probability float best detection probability
     * @param label String name of detection label
     */
    public ImageDescription(String path, int index, float probability, String label) {
        this.path = path;
        this.index = index;
        this.probability = probability;
        this.label = label;
    }

    public int getIndex() {
        return this.index;
    }

    public float getProbability() {
        return this.probability;
    }

    public String getLabel() {
        return this.label;
    }
    public String getPath() { return this.path; }
}

