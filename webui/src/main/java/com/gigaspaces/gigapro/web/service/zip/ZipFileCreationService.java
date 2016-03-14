package com.gigaspaces.gigapro.web.service.zip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gigaspaces.gigapro.web.util.FileUtils.createTempFile;
import static java.net.URI.create;
import static java.nio.file.FileSystems.newFileSystem;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class ZipFileCreationService implements ZipFileCreator {

    private static final Logger LOG = LoggerFactory.getLogger(ZipFileCreationService.class);

    private static final String ZIP_FILE_TYPE = ".zip";

    @Override
    public Path createZipFile(String name, List<Path> filesToAdd) throws IOException {
        final Path tempZipFile = createTempFile(name, ZIP_FILE_TYPE);
        String generatedZipPath;

        try (FileSystem zipFileSystem = createZipFileSystem(tempZipFile.toFile().getName(), true)) {
            final Path root = zipFileSystem.getPath("/");
            for (Path file : filesToAdd) {
                if (!isDirectory(file)) {
                    addFileToZip(zipFileSystem, root, file);
                } else {
                    addDirectoryToZip(zipFileSystem, root, file);
                }
            }
            generatedZipPath = zipFileSystem.toString();
        }
        replaceTempZipFile(tempZipFile, get(generatedZipPath));
        return tempZipFile;
    }

    private void addFileToZip(FileSystem zipFileSystem, Path root, Path file) throws IOException {
        final Path dest = zipFileSystem.getPath(root.toString(), file.toFile().getName());
        LOG.info("Adding file " + dest);
        Files.copy(file, dest, REPLACE_EXISTING);
    }

    private void addDirectoryToZip(final FileSystem zipFileSystem, final Path root, Path file) throws IOException {
        Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final Path dest = zipFileSystem.getPath(root.toString(), file.toFile().getName());
                Files.copy(file, dest, REPLACE_EXISTING);
                return CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                final Path dirToCreate = zipFileSystem.getPath(root.toString(), dir.toFile().getName());
                if (notExists(dirToCreate)) {
                    LOG.info("Adding directory " + dirToCreate);
                    createDirectories(dirToCreate);
                }
                return CONTINUE;
            }
        });
    }

    private FileSystem createZipFileSystem(String zipFilename, boolean create) throws IOException {
        final Path path = get(zipFilename);
        final URI uri = create("jar:file:" + path.toUri().getPath());
        final Map<String, String> env = new HashMap<>();
        env.put("encoding", "UTF-8");
        if (create) {
            env.put("create", "true");
        }
        return newFileSystem(uri, env);
    }

    private void replaceTempZipFile(Path tempZipFile, Path source) throws IOException {
        Path newDir = tempZipFile.getParent();
        Files.move(source, newDir.resolve(source.getFileName()), REPLACE_EXISTING);
    }
}
