package com.example.demo.logic;

import com.example.demo.exceptions.CannotDecodeException;
import com.example.demo.exceptions.CannotEncodeException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public abstract class BaseSteganography{

    byte pixelsPerByte;

    /** Capacity of the cover image. */
    long capacity;

    /** Information about the embedded data.*/
    byte[] header;

    /** Contains info about the embedded data in a readable format.*/
    HiddenData secretInfo;

    /** x-axis current pixel position */
    int i = 0;

    /** y-axis current pixel position */
    int j = 0;

    BaseSteganography(){}

    /**
     * Encodes a byte array into a cover image.
     * <p>A subclass must provide an implementation of this method.</p>
     *
     * @param message                message to embed in the cover image
     * @param output                 stego image with the embedded message
     * @throws IOException           if an error occurs when creating the stego image.
     * @throws CannotEncodeException if the message is empty or larger than maximum capacity.

     */
    public abstract void encode(byte[] message, File output) throws IOException, CannotEncodeException;

    /**
     * Decodes the content of a stego image and saves it to a file.
     * <p>A subclass must provide an implementation of this method.</p>

     * @param file                   destination of the embedded data in the stego image
     * @throws IOException           if an error occurs while writing hidden data to the file.
     * @throws CannotDecodeException if there is no data embedded in the stego image.

     */
    public abstract void decode(File file) throws IOException, CannotDecodeException;

    /**
     * Returns the header containing information about the embedded data from a stego image.
     * <p>A subclass must provide an implementation of this method.</p>
     *
     * @return                       a byte array with the information about the embedded data.
     * @throws CannotDecodeException if there is no data embedded in the stego image.
     */
    public abstract byte[] getHeader() throws CannotDecodeException;

    /**
     * Increments the position of the current pixel in the image.
     * <p>A subclass must provide an implementation of this method.</p>
     */
    protected abstract void increment();

    /**
     * Resets the value of the index.
     * <p>A subclass must provide an implementation of this method.</p>
     */
    protected abstract void reset();
    /**
     * Returns the capacity of the cover image.
     * <p>A subclass must provide an implementation of this method.</p>
     *
     * @return the cover image capacity.
     */
    public long getCapacity(){
        return this.capacity;
    }

    /**
     * Returns the {@link #secretInfo} field containing the info about the embedded data of the stego image.
     *
     * @return a {@link HiddenData} <code>Object</code> that contains the info about the embedded data.
     */
    protected HiddenData getSecretInfo(){
        return this.secretInfo;
    }

    /**
     * Sets the {@link #secretInfo} field containing info about the embedded data in the stego image.
     *
     * @param info {@link HiddenData} object containing the info about the embedded data
     */
    void setSecretInfo(HiddenData info){
        this.secretInfo = info;
    }


    byte[] setHeader(byte[] message) throws CannotEncodeException{
        if (message.length == 0)
            throw new CannotEncodeException("Message is empty");
        if (message.length > 16777215)
            throw new CannotEncodeException("Message is larger than maximum allowed capacity (16777215 bytes)");
        List<Byte> header = new ArrayList<>();
        header.add((byte)'M');
        header.add(this.pixelsPerByte);
        String messageLength = String.format("%24s", Integer.toBinaryString(message.length)).replace(' ', '0');
        for(int i=0; i<messageLength.length();i+=8){
            header.add((byte)Integer.parseInt(messageLength.substring(i,i+8),2));
        }
        header.add(((byte) '!'));
        this.header = Utils.toByteArray(header);
        if (capacity - this.header.length < message.length)
            throw new CannotEncodeException("Message is larger than image capacity by "+(message.length-capacity+this.header.length)+" bytes.");
        return Utils.toByteArray(header);
    }


//    byte[] setHeader(File file) throws IOException, CannotEncodeException{
//        if (file.length() == 0)
//            throw new CannotEncodeException("File is empty.");
//        if (file.length() >  16777215)
//            throw new CannotEncodeException("File is larger than maximum allowed capacity (16777215 bytes)");
//        List<Byte> header = new ArrayList<>();
//        String extension = Utils.getFileExtension(file).toLowerCase();
//        header.add((byte)'D');
//        header.add(this.pixelsPerByte);
//        String fileLength = String.format("%24s",Long.toBinaryString(file.length())).replace(' ', '0');
//        for(int i=0; i<fileLength.length();i+=8){
//            header.add((byte)Integer.parseInt(fileLength.substring(i,i+8),2));
//        }
//        byte[] fileExtension = extension.getBytes(StandardCharsets.UTF_8);
//        for(byte b : fileExtension)
//            header.add(b);
//        header.add(((byte) '!'));
//        this.header = Utils.toByteArray(header);
//        if (capacity - this.header.length < file.length())
//            throw new CannotEncodeException("File is larger than maximum capacity by "+(file.length()-capacity+this.header.length)+" bytes.");
//        return Utils.toByteArray(header);
//    }

}
