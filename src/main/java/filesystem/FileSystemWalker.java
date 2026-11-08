package filesystem;
import java.util.Objects;
public final class FileSystemWalker {
    public void walk(Element root, Visitor visitor) {
        Objects.requireNonNull(root, "Root must not be null");
        Objects.requireNonNull(visitor, "Visitor must not be null");
        visitor.reset();
        walkNode(root, "", 0, visitor);
    }
    private void walkNode(Element node, String path, int depth, Visitor visitor) {
        visitor.enter(path, depth);
        node.accept(visitor);
        for (Element child : node.getChildren()) {
            String childPath = path.isEmpty() ? child.getName() : path + "/" + child.getName();
            walkNode(child, childPath, depth + 1, visitor);
        }
    }
}