package filesystem;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class FileSystemTest {
    private final FileSystemWalker walker = new FileSystemWalker();
    private DirectoryNode tree() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode a = new DirectoryNode("a");
        DirectoryNode deep = new DirectoryNode("deep");
        root.add(new FileNode("z.txt", 100));
        root.add(a);
        root.add(new DirectoryNode("empty"));
        a.add(new FileNode("b.txt", 200));
        a.add(new FileNode("image.png", 400));
        a.add(deep);
        deep.add(new FileNode("c.txt", 300));
        return root;
    }
    @Test void dispatchesBothConcreteTypes() {
        class RecordingVisitor implements Visitor {
            int files; int directories;
            public void reset() { files = 0; directories = 0; }
            public void visit(FileNode file) { files++; }
            public void visit(DirectoryNode directory) { directories++; }
        }
        RecordingVisitor visitor = new RecordingVisitor();
        new FileNode("one", 1).accept(visitor);
        new DirectoryNode("two").accept(visitor);
        assertEquals(1, visitor.files);
        assertEquals(1, visitor.directories);
    }
    @Test void sumsNestedFilesOnce() {
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        walker.walk(tree(), visitor);
        assertEquals(1000, visitor.getTotalSize());
    }
    @Test void combinesNameAndInclusiveSize() {
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 200);
        walker.walk(tree(), visitor);
        assertEquals(List.of("a/b.txt", "a/deep/c.txt"), visitor.getMatches());
    }
    @Test void sortsPathsRelativeToSelectedRoot() {
        DirectoryNode root = tree();
        SearchVisitor visitor = new SearchVisitor(name -> true, 0);
        walker.walk(root, visitor);
        assertEquals(List.of("a/b.txt", "a/deep/c.txt", "a/image.png", "z.txt"), visitor.getMatches());
        walker.walk(root.getChildren().get(1), visitor);
        assertEquals(List.of("b.txt", "deep/c.txt", "image.png"), visitor.getMatches());
    }
    @Test void countsAllNodesAndMaximumDepth() {
        StatisticsVisitor visitor = new StatisticsVisitor();
        walker.walk(tree(), visitor);
        assertEquals(4, visitor.getFileCount());
        assertEquals(4, visitor.getDirectoryCount());
        assertEquals(3, visitor.getMaximumDepth());
    }
    @Test void handlesEmptyDirectory() {
        DirectoryNode root = new DirectoryNode("empty");
        TotalSizeVisitor size = new TotalSizeVisitor();
        StatisticsVisitor stats = new StatisticsVisitor();
        SearchVisitor search = new SearchVisitor(name -> true, 0);
        walker.walk(root, size); walker.walk(root, stats); walker.walk(root, search);
        assertEquals(0, size.getTotalSize()); assertEquals(0, stats.getFileCount());
        assertEquals(1, stats.getDirectoryCount()); assertEquals(0, stats.getMaximumDepth());
        assertTrue(search.getMatches().isEmpty());
    }
    @Test void handlesNoMatchesAndCaseSensitiveCriterion() {
        SearchVisitor visitor = new SearchVisitor(name -> name.contains("TXT"), 0);
        walker.walk(tree(), visitor);
        assertTrue(visitor.getMatches().isEmpty());
    }
    @Test void resetsAllVisitorsAcrossRepeatedAndChangedInputs() {
        Element root = tree();
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> true, 0);
        StatisticsVisitor stats = new StatisticsVisitor();
        for (int i = 0; i < 2; i++) {
            walker.walk(root, stats); walker.walk(root, search); walker.walk(root, size);
            assertEquals(1000, size.getTotalSize()); assertEquals(4, search.getMatches().size());
            assertEquals(4, stats.getFileCount()); assertEquals(4, stats.getDirectoryCount());
        }
        Element empty = new DirectoryNode("empty");
        walker.walk(empty, size); walker.walk(empty, search); walker.walk(empty, stats);
        assertEquals(0, size.getTotalSize()); assertTrue(search.getMatches().isEmpty());
        assertEquals(0, stats.getFileCount()); assertEquals(1, stats.getDirectoryCount());
        assertEquals(0, stats.getMaximumDepth());
    }
    @Test void rejectsNegativeSizesAndInvalidNames() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("bad", -1));
        for (String name : List.of("", " ", ".", "..", "a/b", "a\\b")) {
            assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(name));
        }
        assertThrows(NullPointerException.class, () -> new FileNode(null, 0));
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(n -> true, -1));
    }
    @Test void rejectsDuplicatesAndMultipleParentsWithoutChangingTree() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode other = new DirectoryNode("other");
        FileNode file = new FileNode("item", 0);
        root.add(file);
        assertThrows(IllegalArgumentException.class, () -> root.add(new DirectoryNode("item")));
        assertThrows(IllegalArgumentException.class, () -> other.add(file));
        assertEquals(List.of(file), root.getChildren()); assertTrue(other.getChildren().isEmpty());
    }
    @Test void rejectsCyclesAndAllowsValidNesting() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode child = new DirectoryNode("child");
        assertThrows(IllegalArgumentException.class, () -> root.add(root));
        root.add(child);
        assertThrows(IllegalArgumentException.class, () -> child.add(root));
        child.add(new DirectoryNode("leaf"));
        assertEquals(1, root.getChildren().size()); assertEquals(1, child.getChildren().size());
    }
    @Test void protectsCollectionsAndAllowsSameNameInDifferentDirectories() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode a = new DirectoryNode("a");
        DirectoryNode b = new DirectoryNode("b");
        root.add(a); root.add(b); a.add(new FileNode("same", 1)); b.add(new FileNode("same", 2));
        assertThrows(UnsupportedOperationException.class, () -> root.getChildren().clear());
        SearchVisitor visitor = new SearchVisitor(name -> true, 0);
        walker.walk(root, visitor);
        assertEquals(List.of("a/same", "b/same"), visitor.getMatches());
        assertThrows(UnsupportedOperationException.class, () -> visitor.getMatches().clear());
    }
    @Test void handlesFileRootAndZeroSize() {
        FileNode root = new FileNode("zero.txt", 0);
        SearchVisitor search = new SearchVisitor(n -> n.endsWith(".txt"), 0);
        StatisticsVisitor stats = new StatisticsVisitor();
        walker.walk(root, search); walker.walk(root, stats);
        assertEquals(List.of("."), search.getMatches());
        assertEquals(1, stats.getFileCount()); assertEquals(0, stats.getDirectoryCount());
        assertEquals(0, stats.getMaximumDepth());
    }
    @Test void rejectsTotalSizeOverflow() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new FileNode("a", Long.MAX_VALUE)); root.add(new FileNode("b", 1));
        assertThrows(ArithmeticException.class, () -> walker.walk(root, new TotalSizeVisitor()));
    }
}