package fxmlController;

import com.github.sarxos.webcam.Webcam;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bytedeco.javacv.FrameGrabber;
import org.controlsfx.control.ToggleSwitch;
import org.tensorflow.Tensor;

import utils.ImageDescription;
import utils.TensorFlowUtils;
import utils.Utils;

import saveImage.SaveImage;
import imageFilterManager.ImageFilterManager;

public class ApplicationController implements Initializable {

    @FXML
    private ToggleSwitch toggleSwitchWebCam;
    @FXML
    private ComboBox<String> comboBoxDevices;
    @FXML
    private GridPane gridImage;
    @FXML
    private Slider sliderPercentage;
    @FXML
    private Text textPercentage;
    @FXML
    private Spinner<Integer> spinnerTime;
    @FXML
    private ComboBox<String> comboBoxLabelsAvailable;
    @FXML
    private ComboBox<String> comboBoxLabelsSelected;
    @FXML
    private ToggleSwitch toggleApplyFilterColor;
    @FXML
    private ToggleSwitch toggleApplyFilterBorder;
    @FXML
    private ToggleSwitch toggleApplyFilterPicture;
    @FXML
    private ComboBox<String> comboBoxFilter;
    @FXML
    private TextField textFieldColorFilter;
    @FXML
    private TextField textFieldXPosPicture;
    @FXML
    private TextField textFieldYPosPicture;


    private final StringProperty folderSave;

    public String getFolderSave() {
        return this.folderSave.getValue();
    }

    public void setFolderSave(String value) {
        this.folderSave.setValue(value);
        checkCanSave();
    }

    public StringProperty folderSaveProperty() {
        return this.folderSave;
    }

    private final BooleanProperty disabledWebCam;

    public boolean getDisabledWebCam() {
        return this.disabledWebCam.getValue();
    }

    public void setDisabledWebCam(boolean value) {
        this.disabledWebCam.setValue(value);
    }

    public BooleanProperty disabledWebCamProperty() {
        return this.disabledWebCam;
    }

    private final BooleanProperty disableSave;

    public boolean getDisableSave() {
        return this.disableSave.getValue();
    }

    public void setDisableSave(boolean value) {
        this.disableSave.setValue(value);
    }

    public BooleanProperty disableSaveProperty() {
        return this.disableSave;
    }

    private final BooleanProperty disableFilterEdition;

    public boolean getDisableFilterEdition() {
        return this.disableFilterEdition.getValue();
    }

    public void setDisableFilterEdition(boolean value) {
        this.disableFilterEdition.setValue(value);
    }

    private BooleanProperty disableFilterEditionProperty() {
        return this.disableFilterEdition;
    }

    public ImageDescription getImageDescription() {
        return imageDescription;
    }

    public HashMap<String, ImageFilterManager> getLabelFilters() {
        return labelFilters;
    }

    private Stage owner;
    public ArrayList<String> allLabels;
    private final ArrayList<String> allLabelsSelected;
    private final HashMap<String, ImageFilterManager> labelFilters;
    public byte[] graphDef;
    private ImageDescription imageDescription;
    private GridImageController gridImageController;
    private int percentage;
    private List<Webcam> webCams;
    private int deviceSelectedIndex;

    public void setOwner(Stage value) {
        this.owner = value;
    }

    public void setAllLabels(ArrayList<String> value) {
        this.allLabels = value;
        fetchAvailableLabels();
        fetchFilters();
    }

    public void setGraphDef(byte[] value) {
        this.graphDef = value;
    }

    public int getSpinnerTime() {
        return Integer.parseInt(this.spinnerTime.getValue().toString()) * 1000;
    }

    private void setDeviceSelectedIndex(int value) {
        this.deviceSelectedIndex = value;
    }

    public int getDeviceSelectedIndex() {
        return this.deviceSelectedIndex;
    }

    /**
     * ApplicationController constructor
     */
    public ApplicationController() {
        this.labelFilters = new HashMap<>();
        this.folderSave = new SimpleStringProperty(null);
        this.disableSave = new SimpleBooleanProperty(true);
        this.disabledWebCam = new SimpleBooleanProperty(true);
        this.disableFilterEdition = new SimpleBooleanProperty(true);
        this.allLabelsSelected = new ArrayList<>();
    }

    /**
     * Call after the controller was created.
     * Initialize all components
     *
     * @param url       Url
     * @param resources Resources
     */
    public void initialize(URL url, ResourceBundle resources) {
        this.toggleSwitchWebCam.selectedProperty().addListener((observable, oldValue, newValue) -> toggleSwitchWebCamSelectionChanged());
        this.comboBoxDevices.valueProperty().addListener((observable, oldValue, newValue) -> comboBoxDevicesValueChanged(newValue));
        this.sliderPercentage.valueProperty().addListener((observable, oldValue, newValue) -> sliderPercentageValueChanged(newValue));
        this.comboBoxLabelsAvailable.getEditor().textProperty().addListener((observable, oldValue, newValue) -> fetchAvailableLabels());
        this.comboBoxLabelsAvailable.valueProperty().addListener((observable, oldValue, newValue) -> comboBoxLabelsAvailableValueChanged(newValue));
        this.comboBoxLabelsSelected.valueProperty().addListener((observable, oldValue, newValue) -> comboBoxLabelsSelectedValueChanged(newValue));
        this.comboBoxFilter.valueProperty().addListener((observable, oldValue, newValue) -> comboBoxFilterValueChanged(newValue));
        this.toggleApplyFilterColor.selectedProperty().addListener((observable, oldValue, newValue) -> toggleApplyFilterColorSelectionChanged());
        this.toggleApplyFilterPicture.selectedProperty().addListener((observable, oldValue, newValue) -> toggleApplyFilterPictureSelectionChanged());
        this.toggleApplyFilterBorder.selectedProperty().addListener((observable, oldValue, newValue) -> toggleApplyFilterBorderSelectionChanged());
        this.textFieldColorFilter.textProperty().addListener(((observable, oldValue, newValue) -> textFieldColorFilterTextChanged(newValue)));
        this.textFieldXPosPicture.textProperty().addListener((observable, oldValue, newValue) -> textFieldXPosPictureTextChanged(newValue));
        this.textFieldYPosPicture.textProperty().addListener((observable, oldValue, newValue) -> textFieldYPosPictureTextChanged(newValue));
        updateWebCamDevices();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/imagePanel.fxml"));
        try {
            Parent children = loader.load();
            this.gridImage.getChildren().add(children);
            this.gridImageController = loader.getController();
            this.gridImageController.setOwner(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Detect the webcam devices and refresh the view
     */
    private void updateWebCamDevices() {
        this.webCams = Webcam.getWebcams();
        switch (this.webCams.size()) {
            case 0 -> {
                this.toggleSwitchWebCam.setDisable(true);
                this.comboBoxDevices.setVisible(false);
                setDeviceSelectedIndex(-1);
                if (!getDisabledWebCam()) {
                    setDisabledWebCam(true);
                    this.resetImageDescription();
                }
            }
            case 1 -> {
                this.toggleSwitchWebCam.setDisable(false);
                this.comboBoxDevices.setVisible(false);
                setDeviceSelectedIndex(0);
            }
            default -> {
                this.toggleSwitchWebCam.setDisable(false);
                this.comboBoxDevices.setVisible(true);
                if (getDeviceSelectedIndex() >= this.webCams.size()) {
                    setDeviceSelectedIndex(0);
                    this.resetImageDescription();
                }
                if (!getDisabledWebCam()) {
                    this.comboBoxDevices.setDisable(true);
                }
            }
        }
        fetchDevicesDetected();
    }

    /**
     * Stop thread cam when exit app
     */
    public void onExit() {
        if (!getDisabledWebCam()) {
            setDisabledWebCam(true);
        }
    }

    /**
     * Switch cam on/off
     */
    private void toggleSwitchWebCamSelectionChanged() {
        try {
            onToggleClick();
        } catch (FrameGrabber.Exception ignored) {
        }
    }

    /**
     * Force to disable the WebCam
     */
    public void stopCam() {
        this.toggleSwitchWebCam.setSelected(false);
    }

    /**
     * Event raise when the devices combobox has changed value
     * @param newValue The new selected value
     */
    private void comboBoxDevicesValueChanged(String newValue) {
        for (int i = 0; i < this.webCams.size(); i++) {
            if (this.webCams.get(i).getName().equals(newValue)) {
                setDeviceSelectedIndex(i);
                return;
            }
        }
        setDeviceSelectedIndex(-1);
    }

    /**
     * Change min percentage probability detection
     * @param newValue Number percentage
     */
    private void sliderPercentageValueChanged(Number newValue) {
        this.percentage = Integer.parseInt(newValue.toString().split("\\.")[0]);
        this.textPercentage.setText(this.percentage + " %");
    }

    /**
     * Add labels selected
     * @param newValue String label selected
     */
    private void comboBoxLabelsAvailableValueChanged(String newValue) {
        addSelectedLabel(newValue);
        checkCanSave();
    }

    /**
     * Remove labels selected
     * @param newValue String label deselected
     */
    private void comboBoxLabelsSelectedValueChanged(String newValue) {
        removeSelectedLabel(newValue);
        checkCanSave();
    }

    /**
     * Set management filter for label
     * @param newValue String Label
     */
    private void comboBoxFilterValueChanged(String newValue) {
        if (newValue != null) {
            ImageFilterManager manager;
            if (!this.labelFilters.containsKey(newValue)) {
                this.labelFilters.put(newValue, new ImageFilterManager(0, 0, 0, 127));
            }
            manager = this.labelFilters.get(newValue);
            setDisableFilterEdition(false);
            this.textFieldColorFilter.setText(manager.getColor());
            this.textFieldXPosPicture.setText(Integer.toString(manager.getXPicture()));
            this.textFieldYPosPicture.setText(Integer.toString(manager.getYPicture()));

            this.toggleApplyFilterColor.setSelected(manager.getIsFilterColorApply());
            this.toggleApplyFilterBorder.setSelected(manager.getIsFilterBorderApply());
            this.toggleApplyFilterPicture.setSelected(manager.getIsFilterPictureApply());

        } else {
            setDisableFilterEdition(true);
            this.textFieldColorFilter.setText("");
            this.textFieldXPosPicture.setText("");
            this.textFieldYPosPicture.setText("");
            this.toggleApplyFilterColor.setSelected(false);
            this.toggleApplyFilterBorder.setSelected(false);
            this.toggleApplyFilterPicture.setSelected(false);
        }
    }

    /**
     * Apply filter color dynamic
     */
    private void toggleApplyFilterColorSelectionChanged() {
        if (!getDisableFilterEdition()) {
            ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
            manager.setIsFilterColorApply(this.toggleApplyFilterColor.isSelected());
            updateImage();
        }
    }

    /**
     * Apply stamp dynamic
     */
    private void toggleApplyFilterPictureSelectionChanged() {
        if (!getDisableFilterEdition()) {
            ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
            manager.setIsFilterPictureApply(this.toggleApplyFilterPicture.isSelected());
            updateImage();

        }
    }

    /**
     * Apply border dynamic
     */
    private void toggleApplyFilterBorderSelectionChanged() {
        if (!getDisableFilterEdition()) {
            ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
            manager.setIsFilterBorderApply(this.toggleApplyFilterBorder.isSelected());
            updateImage();
        }
    }

    /**
     * Set value rgba for filter color
     * @param newValue String code rgba
     */
    private void textFieldColorFilterTextChanged(String newValue) {
        if (!getDisableFilterEdition()) {
            ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
            manager.setFromString(newValue);
            if (!manager.getColor().equals(newValue)) {
                this.textFieldColorFilter.setText(manager.getColor());
            }
            if (manager.getIsFilterColorApply()) {
                updateImage();
            }
        }
    }

    /**
     * Set position x for stamp
     * @param newValue String position x
     */
    private void textFieldXPosPictureTextChanged(String newValue) {
        if (!getDisableFilterEdition()) {
            ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
            manager.setXPictureFromString(newValue);
            if (!Integer.toString(manager.getXPicture()).equals(newValue)) {
                this.textFieldXPosPicture.setText(Integer.toString(manager.getXPicture()));
            }
            if (manager.getIsFilterPictureApply()) {
                updateImage();
            }
        }
    }

    /**
     * Set position y for stamp
     * @param newValue String position y
     */
    private void textFieldYPosPictureTextChanged(String newValue) {
        if (!getDisableFilterEdition()) {
            ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
            manager.setYPictureFromString(newValue);
            if (!Integer.toString(manager.getYPicture()).equals(newValue)) {
                this.textFieldYPosPicture.setText(Integer.toString(manager.getYPicture()));
            }
            if (manager.getIsFilterPictureApply()) {
                updateImage();
            }
        }
    }

    /**
     * Call when the WebCam toggle is clicked
     *
     * @throws FrameGrabber.Exception if device not found
     */
    private void onToggleClick() throws FrameGrabber.Exception {
        this.resetImageDescription();
        this.setDisabledWebCam(!this.getDisabledWebCam());
        if (!this.getDisabledWebCam()) {
            this.comboBoxDevices.setDisable(true);
            this.gridImageController.setCam();
        } else {
            this.comboBoxDevices.setDisable(false);
        }
    }

    /**
     * The event raise when the refresh devices button is clicked
     * @param actionEvent The event
     */
    @FXML
    private void refreshDevicesButtonClick(ActionEvent actionEvent) {
        updateWebCamDevices();
    }

    /**
     * Check if the user can save the image.
     * Need a folder save, a loaded picture and a picture name.
     */
    private void checkCanSave() {
        setDisableSave(this.getFolderSave() == null || this.allLabelsSelected.size() == 0 || this.imageDescription == null);
    }

    /**
     * set description on application after the tensor result
     *
     * @param imageDescription of the tensor result
     */
    public void setImageDescription(ImageDescription imageDescription) {
        this.imageDescription = imageDescription;
        updateImage();
        checkCanSave();
    }

    /**
     * Update description when cam on
     */
    private void updateImage() {
        this.gridImageController.setDescription(this.imageDescription);
    }

    /**
     * Reset image description
     */
    public void resetImageDescription() {
        this.setImageDescription(null);
    }

    /**
     * EventHandler of the button "Select Picture"
     * <p>
     * Select a file and get it to TensorFlow and the data.
     *
     * @param event The event given by the sender
     * @throws IOException if file not exists
     */
    @FXML
    private void selectPictureButtonClick(ActionEvent event) throws IOException {
        File file = Utils.openFile(
            this.owner,
            "Select a picture to open...",
            System.getProperty("user.dir") + "/src/main/resources/images",
            new FileChooser.ExtensionFilter("Pictures", "*.jpg", "*.jpeg")
        );
        if (file != null) {
            TensorFlowUtils tensorFlowUtils = new TensorFlowUtils();
            Tensor<Float> tensor = tensorFlowUtils.executeModelFromByteArray(
                    this.graphDef,
                    tensorFlowUtils.byteBufferToTensor(Utils.readFileToBytes(file.getPath()))
            );
            setImageDescription(tensorFlowUtils.getDescription(file.getPath(), tensor, this.allLabels));
        }
    }

    /**
     * Select picture for stamp
     * @param actionEvent ActionEvent
     */
    @FXML
    private void selectPictureTamponButtonClick(ActionEvent actionEvent) {
        File file = Utils.openFile(
                this.owner,
                "Select a picture to open...",
                System.getProperty("user.dir") + "/src/main/resources/images",
                new FileChooser.ExtensionFilter("Pictures", "*.jpg", "*.jpeg", "*.png")
        );
        if (file != null) {
            ImageFilterManager.PATH_PICTURE = file.getPath();
            if (!getDisableFilterEdition()) {
                ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
                manager.setPathPicture(file.getPath());
                updateImage();
            }
        }
    }

    /**
     * Select picture for border
     * @param actionEvent ActionEvent
     */
    @FXML
    private void selectPictureBorderButtonClick(ActionEvent actionEvent) {
        File file = Utils.openFile(
                this.owner,
                "Select a picture to open...",
                System.getProperty("user.dir") + "/src/main/resources/images",
                new FileChooser.ExtensionFilter("Pictures", "*.jpg", "*.jpeg", "*.png")
        );
        if (file != null) {
            ImageFilterManager.PATH_BORDER = file.getPath();
            if (!getDisableFilterEdition()) {
                ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
                manager.setPathBorder(file.getPath());
                updateImage();
            }
        }
    }

    /**
     * EventHandler of the button "Select Save Folder"
     * <p>
     * Select a folder to save the picture.
     *
     * @param event The event given by the sender
     */
    @FXML
    private void selectSaveFolderButtonClick(ActionEvent event) {
        File file = Utils.openDirectory(
                this.owner,
                "Select a folder to save image...",
                System.getProperty("user.dir")
        );
        if (file != null) {
            setFolderSave(file.getPath());
        }
    }

    /**
     * Save the picture to the save folder selected
     *
     * @param event The event raise by the sender
     */
    @FXML
    private void handleButtonSave(ActionEvent event) {
        SaveImage saveImage = new SaveImage(this.percentage, this.allLabelsSelected, folderSave.getValue());
        saveImage.save(this.imageDescription, this.gridImageController.getLastBufferedImage());
    }

    /**
     * Feed the comboBox of the filter labels.
     */
    private void fetchFilters() {
        this.comboBoxFilter.getItems().clear();
        for (String label : this.allLabels) {
            this.comboBoxFilter.getItems().add(label);
        }
        this.comboBoxFilter.getItems().sort(String::compareToIgnoreCase);
    }

    /**
     * Feed the comboBox of the available labels.
     */
    private void fetchAvailableLabels() {
        String filter = this.comboBoxLabelsAvailable.getEditor().getText();
        this.comboBoxLabelsAvailable.getItems().clear();
        for (String label : this.allLabels) {
            if (label.startsWith(filter) && !this.allLabelsSelected.contains(label)) {
                this.comboBoxLabelsAvailable.getItems().add(label);
            }
        }
    }

    /**
     * Add a label in the SelectedLabels List
     *
     * @param label The new label to add
     */
    private void addSelectedLabel(String label) {
        if (label != null && label.length() > 0 && !this.allLabelsSelected.contains(label)) {
            this.allLabelsSelected.add(label);
            fetchSelectedLabels();
            fetchAvailableLabels();
        }
    }

    /**
     * Remove a label from the SelectedLabels List
     *
     * @param label The label to remove
     */
    private void removeSelectedLabel(String label) {
        if (label != null && label.length() > 0) {
            this.allLabelsSelected.remove(label);
            fetchSelectedLabels();
            fetchAvailableLabels();
        }
    }

    /**
     * Feed the comboBox of the selected labels.
     */
    private void fetchSelectedLabels() {
        this.comboBoxLabelsSelected.getItems().clear();
        this.allLabelsSelected.sort(String::compareToIgnoreCase);
        for (String label : this.allLabelsSelected) {
            this.comboBoxLabelsSelected.getItems().add(label);
        }
        this.comboBoxLabelsSelected.setPromptText("Labels selected (" + this.comboBoxLabelsSelected.getItems().size() + ")");
    }

    /**
     * Feed the comboBox of the detected devices.
     */
    private void fetchDevicesDetected() {
        this.comboBoxDevices.getItems().clear();
        for (Webcam webcam : this.webCams) {
            this.comboBoxDevices.getItems().add(webcam.getName());
        }
    }
}