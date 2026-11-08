package filesystem;
public final class TotalSizeVisitor implements Visitor {
    private long totalSize;
    public void reset() { totalSize = 0; }
    public void visit(FileNode file) { totalSize = Math.addExact(totalSize, file.getSize()); }
    public void visit(DirectoryNode directory) { }
    public long getTotalSize() { return totalSize; }
}