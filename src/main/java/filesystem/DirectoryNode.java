package filesystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public final class DirectoryNode extends Node {
    private final List<Element> children = new ArrayList<>();
    public DirectoryNode(String name) { super(name); }
    public void add(Node child) {
        Objects.requireNonNull(child, "Child must not be null");
        if (child.getParent() != null) throw new IllegalArgumentException("Child already has a parent");
        for (DirectoryNode ancestor = this; ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor == child) throw new IllegalArgumentException("Cycle is not allowed");
        }
        for (Element existing : children) {
            if (existing.getName().equals(child.getName())) throw new IllegalArgumentException("Duplicate sibling name");
        }
        children.add(child);
        child.attachTo(this);
    }
    public List<Element> getChildren() { return List.copyOf(children); }
    public void accept(Visitor visitor) { visitor.visit(this); }
}