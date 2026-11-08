# Assignment 6: Visitor Pattern — Advanced File System

| |                          |
| --- |--------------------------|
| Student | Mustafa Rayazov          |
| Group | SE - 2529                |
| Course | Software Design Patterns |
| Tools | Java 17, Maven, JUnit 5  |

## Project purpose

The project implements an in-memory file system with files and nested directories. The Visitor pattern separates three operations from the tree model, so each operation can run independently on the same subtree.

## Build, test, and run

Install JDK 17 and Maven. Run the following commands from the directory containing `pom.xml`.

Run the full test suite:

```sh
mvn test
```

Build and run the executable demonstration:

```sh
mvn package
java -jar target/visitor-file-system-1.0.0.jar
```

The demonstration runs all three visitors on a fixed tree. The test suite and demonstration run from the command line without manual input or IDE configuration.

## Object model and double dispatch

`Element` defines the common contract for `FileNode` and `DirectoryNode`. A file has a name and a non-negative size in bytes. A directory holds named child elements and supports nested and empty directories.

Calling `accept(visitor)` selects the concrete element's implementation. Inside that method, the declared type of `this` selects the matching overloaded `visit` method. The visitor instance then determines which implementation of that overload runs. This design avoids runtime type checks and casts for choosing element behavior.

`DirectoryNode.add` checks ownership, duplicate sibling names, and cycles before updating the tree. File construction rejects negative sizes. The public child accessor returns an immutable snapshot.

## Traversal and visitor state

`FileSystemWalker` owns depth-first traversal. It calls `accept` once per reachable node and supplies the current path and depth to the visitor. Traversal lives in the walker so each visitor can focus on its result and use the same visiting rules.

Visitor instances can be reused for sequential calls to `walk`. The walker resets the visitor at the start of every call so each result reflects only the current run.

## Operations

| Visitor | Behavior |
| --- | --- |
| `TotalSizeVisitor` | Adds the byte size of each file in the selected subtree once. Directories contribute zero bytes. |
| `SearchVisitor` | Requires both a configurable filename predicate and an inclusive minimum size. Returns paths relative to the selected root, with `/` separators, in lexicographic order. A matching file selected as the root is represented by `.`. |
| `StatisticsVisitor` | Counts files and directories, including the selected root when it is a directory. Reports the largest number of edges from the root to a visited node. The root has depth zero. |

The walker excludes the selected root name from descendant paths and increases depth by one for each child edge.

## Extension boundary

A new operation implements the existing `Visitor` interface. `StatisticsVisitor` uses the same overloaded methods as the other visitors, so adding it requires no changes to `FileNode`, `DirectoryNode`, or their `accept` methods. A new element type would require a matching overload in the visitor contract and updates to existing visitors.

## Automated verification

`filesystem.FileSystemTest` contains 14 JUnit 5 tests. The recorded Maven run completed with 14 passed, 0 failures, 0 errors, and 0 skipped.

| Checkpoint | Coverage |
| --- | --- |
| T1 | Both element types reach their matching visitor overloads. |
| T2 | Total size includes files across several directory levels. |
| T3 | Search combines filename and inclusive size conditions. |
| T4 | Search paths are relative to the selected root and sorted. |
| T5 | Statistics count nodes and track maximum depth. |
| T6 | Empty directories and searches with no matches produce valid results. |
| T7 | Repeated traversal resets visitor state. |
| T8 | Invalid mutations are rejected and valid nesting is supported. |