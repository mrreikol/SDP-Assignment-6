package filesystem;
import java.util.List;
public sealed interface Element permits Node {
    String getName();
    List<Element> getChildren();
    void accept(Visitor visitor);
}