package filesystem;
public final class Main {
    public static void main(String[] args) {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode archive = new DirectoryNode("archive");
        root.add(new FileNode("readme.txt", 120));
        root.add(documents);
        root.add(new DirectoryNode("empty"));
        documents.add(new FileNode("notes.txt", 350));
        documents.add(new FileNode("photo.png", 2048));
        documents.add(archive);
        archive.add(new FileNode("old-notes.txt", 600));
        FileSystemWalker walker = new FileSystemWalker();
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> name.contains("notes"), 350);
        walker.walk(root, size);
        walker.walk(root, search);
        System.out.println("Total size: " + size.getTotalSize() + " bytes");
        System.out.println("Search: name contains notes, size >= 350 bytes");
        search.getMatches().forEach(System.out::println);
        StatisticsVisitor statistics = new StatisticsVisitor();
        walker.walk(root, statistics);
        System.out.println("Files: " + statistics.getFileCount());
        System.out.println("Directories: " + statistics.getDirectoryCount());
        System.out.println("Maximum depth: " + statistics.getMaximumDepth());
    }
}