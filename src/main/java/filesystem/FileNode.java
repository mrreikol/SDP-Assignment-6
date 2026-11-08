package filesystem;
import java.util.List;
public final class FileNode extends Node {
    private final long size;
    public FileNode(String name, long size) {
        super(name);
        if (size < 0) throw new IllegalArgumentException("Size must not be negative");
        this.size = size;
    }
    public long getSize() { return size; }
    public List<Element> getChildren() { return List.of(); }
    public void accept(Visitor visitor) { visitor.visit(this); }
}