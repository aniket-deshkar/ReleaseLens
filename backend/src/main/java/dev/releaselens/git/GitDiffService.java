package dev.releaselens.git;

import dev.releaselens.domain.ChangedFile;
import dev.releaselens.domain.Enums;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@Service
public class GitDiffService {
    public Snapshot snapshot(Path path, String baseRef, String headRef) {
        try (Repository repository = new FileRepositoryBuilder().setWorkTree(path.toFile()).findGitDir(path.toFile()).build()) {
            ObjectId base = repository.resolve(baseRef); ObjectId head = repository.resolve(headRef);
            if (base == null || head == null) throw new IllegalArgumentException("Unknown Git ref");
            RevCommit baseCommit; RevCommit headCommit;
            try (RevWalk walk = new RevWalk(repository)) { baseCommit = walk.parseCommit(base); headCommit = walk.parseCommit(head); }
            List<DiffEntry> entries;
            try (DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                formatter.setRepository(repository); formatter.setDetectRenames(true);
                entries = formatter.scan(baseCommit.getTree(), headCommit.getTree());
            }
            List<ChangedFile> files = new ArrayList<>();
            for (DiffEntry e : entries) {
                Enums.ChangeKind kind = switch (e.getChangeType()) { case ADD -> Enums.ChangeKind.ADDED; case DELETE -> Enums.ChangeKind.REMOVED; case RENAME -> Enums.ChangeKind.RENAMED; default -> Enums.ChangeKind.MODIFIED; };
                String p = kind == Enums.ChangeKind.RENAMED ? e.getNewPath() : (kind == Enums.ChangeKind.REMOVED ? e.getOldPath() : e.getNewPath());
                // Read the selected head/base blob, rather than the caller's working tree.
                // This keeps evidence tied to the requested revisions even when the checkout
                // has uncommitted changes or is currently on another branch.
                ObjectId contentId = kind == Enums.ChangeKind.REMOVED ? e.getOldId().toObjectId() : e.getNewId().toObjectId();
                List<String> lines = readLines(repository, contentId);
                files.add(new ChangedFile(p, kind, e.getOldPath().equals(DiffEntry.DEV_NULL) ? null : e.getOldPath(), e.getOldId().toObjectId().name(), e.getNewId().toObjectId().name(), lines));
            }
            return new Snapshot(base.name(), head.name(), files);
        } catch (IOException e) { throw new IllegalArgumentException("Unable to read Git repository", e); }
    }
    private List<String> readLines(Repository repository, ObjectId blobId) {
        try {
            if (ObjectId.zeroId().equals(blobId)) return List.of();
            return Arrays.asList(new String(repository.open(blobId).getBytes(), StandardCharsets.UTF_8).split("\\R", -1));
        } catch (IOException e) { return List.of(); }
    }
    public record Snapshot(String baseSha, String headSha, List<ChangedFile> changedFiles) { public Snapshot { changedFiles = List.copyOf(changedFiles); } }
}
