/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.entities.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Collects what a generator-run did, so changes between minecraft-versions are visible without reading the whole diff
 */
final class Report {

    private final TreeSet<String> added = new TreeSet<>();
    private final TreeSet<String> changed = new TreeSet<>();
    private final TreeSet<String> removed = new TreeSet<>();
    private final TreeSet<String> overridden = new TreeSet<>();
    private final TreeSet<String> unresolved = new TreeSet<>();
    private final TreeSet<String> resolved = new TreeSet<>();
    private final TreeSet<String> empty = new TreeSet<>();
    private final TreeSet<String> warnings = new TreeSet<>();
    private int unchanged;

    void added(String path) { added.add(path); }
    void changed(String path) { changed.add(path); }
    void removed(String path) { removed.add(path); }
    void overridden(String path) { overridden.add(path); }
    void unresolved(String layer) { unresolved.add(layer); }
    void resolved(String layer, String source, int variants) {
        resolved.add(layer + " -> " + source + (variants > 0 ? " (" + variants + " variants)" : ""));
    }
    void empty(String layer) { empty.add(layer); }
    void warning(String warning) { warnings.add(warning); }
    void unchanged(String path) { unchanged++; }

    String summary() {
        return "models: %d added, %d changed, %d removed, %d unchanged | %d layers without texture, %d warnings"
                .formatted(added.size(), changed.size(), removed.size(), unchanged, unresolved.size(), warnings.size());
    }

    void write(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(summary());

        section(lines, "added", added);
        section(lines, "changed", changed);
        section(lines, "removed", removed);
        section(lines, "overridden by generator/overrides", overridden);
        section(lines, "no texture found (pin one in config/textures.json)", unresolved);
        section(lines, "layers without any cube", empty);
        section(lines, "warnings", warnings);
        section(lines, "resolved textures", resolved);

        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private static void section(List<String> lines, String title, TreeSet<String> entries) {
        if (entries.isEmpty()) return;
        lines.add("");
        lines.add("### " + title + " (" + entries.size() + ")");
        lines.addAll(entries);
    }

}
