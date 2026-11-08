package filesystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
public final class SearchVisitor implements Visitor {
    private final Predicate<String> nameCriterion;
    private final long minimumSize;
    private final List<String> matches = new ArrayList<>();
    private String path;
    public SearchVisitor(Predicate<String> nameCriterion, long minimumSize) {
        this.nameCriterion = Objects.requireNonNull(nameCriterion, "Criterion must not be null");
        if (minimumSize < 0) throw new IllegalArgumentException("Minimum size must not be negative");
        this.minimumSize = minimumSize;
    }
    public void reset() { matches.clear(); path = ""; }
    public void enter(String path, int depth) { this.path = path; }
    public void visit(FileNode file) {
        if (nameCriterion.test(file.getName()) && file.getSize() >= minimumSize) {
            matches.add(path.isEmpty() ? "." : path);
        }
    }
    public void visit(DirectoryNode directory) { }
    public List<String> getMatches() { return matches.stream().sorted().toList(); }
}