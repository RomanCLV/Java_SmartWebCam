package utils;

import org.tensorflow.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;


public class TensorFlowUtils {

    public Tensor<Float> executeSavedModel(String modelFolderPath, Tensor<Float> input) {
        try {
            Path path = Paths.get(ClassLoader.getSystemClassLoader().getResource(modelFolderPath).toURI()).toAbsolutePath();
            SavedModelBundle model = SavedModelBundle.load(path.toString(),"serve");
            return model.session().runner().feed("input", input).fetch("output").run().get(0).expect(Float.class);
        }
        catch (URISyntaxException e) {
            throw new Error("invalid path");
        }
    }

    /**
     * Create a Tensor from a bytes buffer and with the Neuron networks
     * @param graphDef The Neuron networks (file .pb)
     * @param input The tensor extracted from the picture
     * @return The tensor that contains the Neuron networks result for each case
     */
    public Tensor<Float> executeModelFromByteArray(byte[] graphDef, Tensor<Float> input) {
        try (Graph g = new Graph()) {
            g.importGraphDef(graphDef);
            try (Session s = new Session(g)) {
                return s.runner().feed("input", input).fetch("output").run().get(0).expect(Float.class);
            }
        }
    }

    /**
     * Create a Tensor from an image
     * <p>
     * Scale and normalize an image (to 224x224), and convert to a tensor
     *
     * @param imageBytes The image (JPG) converted into a bytes buffer
     * @return A float tensor with the data
     */
    public Tensor<Float> byteBufferToTensor(byte[] imageBytes) {
        try (Graph g = new Graph()) {
            GraphBuilder graphBuilder = new GraphBuilder(g);

            final float mean = 117f;
            final float scale = 1f;
            final int height = 224;
            final int width = 224;

            final Output input = graphBuilder.constant("input", imageBytes);
            final Output output =
                    graphBuilder.div(
                            graphBuilder.sub(
                                    graphBuilder.resizeBilinear(
                                            graphBuilder.expandDims(
                                                    graphBuilder.cast(graphBuilder.decodeJpeg(input, 3), DataType.FLOAT),
                                                    graphBuilder.constant("make_batch", 0)),
                                            graphBuilder.constant("size", new int[] { height, width })),
                                    graphBuilder.constant("mean", mean)),
                            graphBuilder.constant("scale", scale));
            try (Session s = new Session(g)) {
                return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public ImageDescription getDescription(String path, Tensor<Float> input, ArrayList<String> labels) {
        long[] shape = input.shape();
        if (input.numDimensions() != 2 || shape[0] != 1) {
            throw new RuntimeException(
                    String.format(
                            "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                            Arrays.toString(shape)));
        }
        int nLabels = (int)shape[1];
        float[] probabilities = input.copyTo(new float[1][nLabels])[0];

        int index = Utils.maxIndex(probabilities);
        return new ImageDescription(path, index, probabilities[index], labels.get(index));
    }



    private static class GraphBuilder {
        private Graph g;

        GraphBuilder(Graph g) {
            this.g = g;
        }

        Output div(Output x, Output y) {
            return binaryOp("Div", x, y);
        }

        Output sub(Output x, Output y) {
            return binaryOp("Sub", x, y);
        }

        Output resizeBilinear(Output images, Output size) {
            return binaryOp("ResizeBilinear", images, size);
        }

        Output expandDims(Output input, Output dim) {
            return binaryOp("ExpandDims", input, dim);
        }

        Output cast(Output value, DataType dtype) {
            return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0);
        }

        Output decodeJpeg(Output contents, long channels) {
            return g.opBuilder("DecodeJpeg", "DecodeJpeg")
                    .addInput(contents)
                    .setAttr("channels", channels)
                    .build()
                    .output(0);
        }

        Output constant(String name, Object value) {
            try (Tensor t = Tensor.create(value)) {
                return g.opBuilder("Const", name)
                        .setAttr("dtype", t.dataType())
                        .setAttr("value", t)
                        .build()
                        .output(0);
            }
        }

        private Output binaryOp(String type, Output in1, Output in2) {
            return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
        }
    }
}
