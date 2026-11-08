package filesystem;
import java.util.Objects;
public abstract sealed class Node implements Element permits FileNode, DirectoryNode {
    private final String name;
    private DirectoryNode parent;
    Node(String name) {
        Objects.requireNonNull(name, "Name must not be null");
        if (name.isBlank() || name.contains("/") || name.contains("\\") || name.equals(".") || name.equals("..")) {
            throw new IllegalArgumentException("Name must be a nonblank path segment");
        }
        this.name = name;
    }
    public final String getName() { return name; }
    final DirectoryNode getParent() { return parent; }
    final void attachTo(DirectoryNode parent) { this.parent = parent; }
}