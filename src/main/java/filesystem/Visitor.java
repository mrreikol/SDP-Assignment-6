package filesystem;
public interface Visitor {
    void reset();
    default void enter(String path, int depth) { }
    void visit(FileNode file);
    void visit(DirectoryNode directory);
}