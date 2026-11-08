package filesystem;
public final class StatisticsVisitor implements Visitor {
    private int fileCount;
    private int directoryCount;
    private int maximumDepth;
    public void reset() { fileCount = 0; directoryCount = 0; maximumDepth = 0; }
    public void enter(String path, int depth) { maximumDepth = Math.max(maximumDepth, depth); }
    public void visit(FileNode file) { fileCount++; }
    public void visit(DirectoryNode directory) { directoryCount++; }
    public int getFileCount() { return fileCount; }
    public int getDirectoryCount() { return directoryCount; }
    public int getMaximumDepth() { return maximumDepth; }
}