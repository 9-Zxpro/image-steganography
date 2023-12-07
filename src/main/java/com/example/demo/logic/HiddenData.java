package com.example.demo.logic;

public class HiddenData {
    /** Represents the format of the embedded data (MESSAGE, DOCUMENT or IMAGE).
     * @see DataFormat */
    public DataFormat format;
    /** Mode of encoding (for MESSAGE or DOCUMENT). */
    public byte pixelsPerByte;
    /** Mode of encoding (for IMAGE). */
    public byte pixelsPerPixel;
    /** Hidden data length. */
    public long length;
    /** Hidden data extension. */
    public String extension;
    /** Hidden image width. */
    public int width;
    /** Hidden image height. */
    public int height;

    /**
     * Creates a HiddenData object from a byte array
     *
     * @param header byte array that contains info about the data to embed/extract
     */

    public HiddenData(byte[] header){
        switch((char) header[0]){
            case 'M':
                this.format = DataFormat.MESSAGE;
                this.extension = "txt";
                break;
//            case 'D':
//                this.format = DataFormat.DOCUMENT;
//                StringBuilder extension = new StringBuilder();
//                for(int j=7; j<header.length-1;j++)
//                    extension.append((char) header[j]);
//                this.extension = extension.toString();
//                break;
            case 'I':
                this.format = DataFormat.IMAGE;
                this.pixelsPerPixel = header[1];
                this.extension = "png";
                StringBuilder height= new StringBuilder();StringBuilder width= new StringBuilder();
                for(int i=2; i<4; i++){
                    String w = String.format("%8s",Integer.toBinaryString(header[i])).replace(' ','0');
                    String h = String.format("%8s",Integer.toBinaryString(header[i+2])).replace(' ','0');
                    width.append(w.substring(w.length() - 8, w.length()));
                    height.append(h.substring(h.length() - 8, h.length()));
                }
                this.width = Integer.parseInt(width.toString(),2);
                this.height = Integer.parseInt(height.toString(),2);
                break;
        }
        if (this.format != DataFormat.IMAGE){
//            this.isEncrypted = ((char) header[1]) == 'E';
//            this.isCompressed = ((char) header[2]) == 'C';
            this.pixelsPerByte = header[1];
            StringBuilder length = new StringBuilder();
            System.out.println(header.length);

            for(var i : header){
                System.out.println(i);
            }
            for(int i=2; i<7; i++){
                String l = String.format("%8s",Integer.toBinaryString(header[i])).replace(' ','0');
                length.append(l.substring(l.length() - 8, l.length()));
            }
            this.length = Integer.parseInt(length.toString(), 2);
        }
    }
}
