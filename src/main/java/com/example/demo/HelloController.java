package com.example.demo;

import com.example.demo.exceptions.CannotDecodeException;
import com.example.demo.exceptions.CannotEncodeException;
import com.example.demo.exceptions.UnsupportedImageTypeException;
import com.example.demo.logic.*;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class HelloController {
    // JavaFX Components
    @FXML
    private Menu editMenu;
    @FXML
    private MenuItem newSecretImage, cutMenu, copyMenu, pasteMenu, undoMenu, redoMenu, selectAllMenu, deleteMenu;
    @FXML
    private ImageView secretImageView, coverImageView, steganographicImageView;
    @FXML
    private TextArea secretMessage;
    @FXML
    private Button encodeImage, decodeImage;
    @FXML
    private Tab secretImageTab, secretMessageTab;
    @FXML
    private VBox coverImagePane, secretImagePane, steganographicImagePane;
    @FXML
    private ToggleGroup messagePixelsPerByte, pixelsPerPixel;
    @FXML
    private HBox messagePixelsPerByteWrapper;

    // Files;
    private File coverImage, secretImage, steganographicImage, tempFile;
    // Clipboard
    private Clipboard systemClipboard = Clipboard.getSystemClipboard();

    /**
     * Sets the cover image from the <code>JavaFX FileChooser</code> and adds it to the {@link #coverImageView}
     * then enables the disabled secret data controls.
     */
    public void setCoverImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("New Cover Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg"));
        coverImage = fc.showOpenDialog(null);
        if (coverImage != null) {
            coverImagePane.setMinSize(0, 0);
            coverImageView.setImage(new Image("file:" + coverImage.getPath()));
            coverImageView.fitWidthProperty().bind(coverImagePane.widthProperty());
            coverImageView.fitHeightProperty().bind(coverImagePane.heightProperty());
            coverImagePane.setMaxSize(900, 900);
            editMenu.setDisable(false);
            newSecretImage.setDisable(false);
            secretMessageTab.setDisable(false);
            secretImageTab.setDisable(false);
            messagePixelsPerByteWrapper.setVisible(true);

        } else {
            AlertBox.error("Error while setting cover image", "Try again...");
        }
    }

    /**
     * Sets the steganographic image from the <code>JavaFX FileChooser</code> and adds it to the {@link #steganographicImageView}.
     */
    public void setSteganographicImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("New Steganographic Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg", "*.gif"));
        steganographicImage = fc.showOpenDialog(null);
        if (steganographicImage != null) {
            steganographicImagePane.setMinSize(0, 0);
            steganographicImageView.setImage(new Image("file:" + steganographicImage.getPath()));
            steganographicImageView.fitWidthProperty().bind(steganographicImagePane.widthProperty());
            steganographicImageView.fitHeightProperty().bind(steganographicImagePane.heightProperty());
            steganographicImagePane.setMaxSize(1440, 900);
            decodeImage.setDisable(false);
        } else {
            AlertBox.error("Error while setting steganographic image", "Try again...");
        }
    }


    /**
     * Sets the secret image from the <code>JavaFX FileChooser</code> and adds it to the {@link #secretImageView}.
     */
    public void setSecretImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("New Secret Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg"));
        secretImage = fc.showOpenDialog(null);
        if (secretImage != null) {
            secretImagePane.setMinSize(0, 0);
            secretImageView.setImage(new Image("file:" + secretImage.getPath()));
            secretImageView.fitWidthProperty().bind(secretImagePane.widthProperty());
            secretImageView.fitHeightProperty().bind(secretImagePane.heightProperty());
            secretImagePane.setMaxSize(900, 900);
            encodeImage.setDisable(false);

        } else {
            AlertBox.error("Error while setting secret image", "Try again...");
        }
    }

    /**
     * Encodes a message in an image,
     * then calls either based
     * on the cover image extension.
     */
    public void encodeMessageInImage() {
        String message = secretMessage.getText();
        byte[] secret = message.getBytes(StandardCharsets.UTF_8);
        String imageExtension = Utils.getFileExtension(coverImage).toLowerCase();
        imageExtension = (imageExtension.matches("jpg|jpeg")) ? "png" : imageExtension;
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Steganographic Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        imageExtension.toUpperCase(),
                        "*." + imageExtension));

        steganographicImage = fc.showSaveDialog(null);
        if (steganographicImage != null) {
            BaseSteganography img;
            try {
                img = new ImageSteganography(coverImage, getToggleGroupValue(messagePixelsPerByte));
                img.encode(secret, steganographicImage);
                AlertBox.information("Encoding Successful!", "Message encoded successfully in " + steganographicImage.getName() + ".", steganographicImage);
            } catch (IOException | CannotEncodeException | UnsupportedImageTypeException e) {
                e.printStackTrace();
                AlertBox.error("Error while encoding", e.getMessage());
            }
        }
    }

    /**
     * Encodes an image in another image using {@link ImageInImageSteganography}.
     */
    public void encodeImageInImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "PNG Image",
                        "*.png"));
        steganographicImage = fc.showSaveDialog(null);
        if (steganographicImage != null) {
            try {
                ImageInImageSteganography img = new ImageInImageSteganography(coverImage, getToggleGroupValue(pixelsPerPixel));
                img.encode(secretImage, steganographicImage);
                AlertBox.information("Encoding Successful!", "Image " + secretImage.getName() + " encoded successfully in " + steganographicImage.getName() + ".", steganographicImage);
            } catch (IOException | CannotEncodeException | UnsupportedImageTypeException e) {
                e.printStackTrace();
                AlertBox.error("Error while encoding", e.getMessage());
            }
        }
    }

    /**
     * Handles decoding the image by decoding the data using the appropriate class
     * based on the extension ({@link ImageSteganography} ),
     * then constructs an {@link HiddenData} object from the image header,
     * then performs decoding and decompression (if enabled) to return the secret data.
     */
    public void decodeImage() {
        HiddenData hiddenData;
        FileChooser fc = new FileChooser();
        File file;
        try {
            BaseSteganography img = new ImageSteganography(steganographicImage);
            hiddenData = new HiddenData(img.getHeader());
            fc.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter(
                            hiddenData.extension.toUpperCase(),
                            "*." + hiddenData.extension));

            if (hiddenData.format == DataFormat.MESSAGE) {
                tempFile = File.createTempFile("message", ".txt");
                img.decode(tempFile);
                byte[] secret = Files.readAllBytes(tempFile.toPath());
                String message;
                message = new String(secret, StandardCharsets.UTF_8);
                if (message.length() > 0)
                    AlertBox.information("Decoding successful!", "Here is the secret message:", message);
                tempFile.deleteOnExit();
            }


            else if(hiddenData.format == DataFormat.IMAGE){
                ImageInImageSteganography imgInImg = new ImageInImageSteganography(steganographicImage);
                file = fc.showSaveDialog(null);
                imgInImg.decode(file);
                AlertBox.information("Decoding Successful!", "Image decoded in " + file.getName(), file);
            }
        } catch (IOException | CannotDecodeException | UnsupportedImageTypeException e) {
            e.printStackTrace();
            AlertBox.error("Error while decoding", e.getMessage());
        }
    }


    /**
     * Gets the encryption mode from the password prompt.
     */
//    public void getEncryptionPassword() {
//        if (encryptMessage.isSelected() || encryptDocument.isSelected()) {
//            if ((password = PasswordPrompt.display(PasswordType.ENCRYPT)) == null) {
//                encryptMessage.setSelected(false);
//                encryptDocument.setSelected(false);
//            }
//        } else {
//            password = null;
//        }
//    }

    /** Returns the value of the encryption mode radio buttons.
     *
     * @param group radio button group.
     * @return      encryption mode (1 or 2).
     */
    private byte getToggleGroupValue(ToggleGroup group){
        RadioButton selectedRadioButton = (RadioButton) group.getSelectedToggle();
        return (byte) Character.getNumericValue(selectedRadioButton.getText().charAt(0));
    }

    /**
     * Reads a document line by line and sets its contents into a <code>ListView</code>.
     *
     * @param documentView <code>JavaFX ListView</code> that will hold the document line by line
     * @param document     document to display in the ListView
     * @throws IOException if an error occurs while reading the document
     */
    private static void getDocumentContent(ListView<String> documentView, File document) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(document));
        BufferedReader reader = new BufferedReader(streamReader);//reads the user file
        String line;
        documentView.getItems().clear();
        while ((line = reader.readLine()) != null)
            documentView.getItems().add(line);
    }

    /** Undoes the last change to the {@link #secretMessage} <code>TextArea</code>. */
    public void undo() { secretMessage.undo(); }
    /** Redoes the last change to the {@link #secretMessage} <code>TextArea</code>. */
    public void redo() { secretMessage.redo(); }
    /** Cuts the content of the {@link #secretMessage} <code>TextArea</code> to the system clipboard. */
    public void cut() { secretMessage.cut(); }
    /** Copies the content of the {@link #secretMessage} <code>TextArea</code> to the system clipboard. */
    public void copy(){ secretMessage.copy();}
    /** Pastes the content of the system clipboard to the {@link #secretMessage} <code>TextArea</code>. */
    public void paste(){ secretMessage.paste(); }
    /** Deletes the selected text of the {@link #secretMessage} <code>TextArea</code>. */
    public void delete(){ secretMessage.replaceSelection(""); }
    /** Selects all the content of the {@link #secretMessage} <code>TextArea</code>. */
    public void selectAll(){ secretMessage.selectAll(); }
    /** Deselects the current {@link #secretMessage} <code>TextArea</code> selection. */
    public void deselect() { secretMessage.deselect(); }

    /** Handles the state of the menu items in the edit menu. */
    public void showingEditMenu() {
        if( systemClipboard == null ) {systemClipboard = Clipboard.getSystemClipboard();}

        if(systemClipboard.hasString()) { pasteMenu.setDisable(false); }
        else {pasteMenu.setDisable(true);}

        if(!secretMessage.getSelectedText().equals("")) {cutMenu.setDisable(false); copyMenu.setDisable(false); deleteMenu.setDisable(false);}
        else { cutMenu.setDisable(true); copyMenu.setDisable(true); deleteMenu.setDisable(true);}

        if (secretMessage.getSelectedText().equals(secretMessage.getText())) { selectAllMenu.setDisable(true); }
        else { selectAllMenu.setDisable(false); }

        if(secretMessage.isRedoable()) { redoMenu.setDisable(false); }
        else { redoMenu.setDisable(true); }

        if(secretMessage.isUndoable()) { undoMenu.setDisable(false); }
        else { undoMenu.setDisable(true); }
    }

}