package ai.model;

public class UploadedFile {
    public final String id;
    public final String filename;
    public final long size;
    public final String mimeType;

    public UploadedFile(String id, String filename, long size, String mimeType) {
        this.id = id;
        this.filename = filename;
        this.size = size;
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return "File: " + filename + " (" + size + " bytes)";
    }
}

