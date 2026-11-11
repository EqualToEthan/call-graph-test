package com.equal.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.CheckoutConflictException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.revwalk.RevCommit;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class GitOperations {
    private final String remoteUrl;
    private final String username;
    private final String password;
    private final String localPath;
    private Git git;

    public GitOperations(String remoteUrl, String username, String password, String localPath) {
        this.remoteUrl = remoteUrl;
        this.username = username;
        this.password = password;
        this.localPath = localPath;
    }

    /**
     * 克隆仓库
     */
    public void cloneRepository() throws GitAPIException {
        System.out.println("开始克隆仓库...");
        try {
            git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(new File(localPath))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                    .call();
            System.out.println("仓库克隆成功");
        } catch (GitAPIException e) {
            System.out.println("仓库克隆失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 切换到指定分支的指定版本
     */
    public void checkoutBranchAndVersion(String branchName, String commitId) throws GitAPIException, IOException {
        System.out.println("切换到分支: " + branchName + " 的版本: " + commitId);
        try {
            // 获取远程分支
            List<Ref> refs = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call();

            // 检查分支是否存在
            boolean branchExists = refs.stream()
                    .anyMatch(ref -> ref.getName().endsWith("/" + branchName));

            if (branchExists) {
                // 创建并切换到本地分支
                git.checkout()
                        .setCreateBranch(true)
                        .setName(branchName)
                        .setStartPoint("origin/" + branchName)
                        .call();
                System.out.println("成功切换到分支: " + branchName);

                // 获取提交ID
                ObjectId commit = git.getRepository().resolve(commitId);
                if (commit == null) {
                    throw new RefNotFoundException("提交ID不存在: " + commitId, null);
                }

                // 执行硬重置到指定版本
                git.reset()
                        .setMode(ResetCommand.ResetType.HARD)
                        .setRef(commitId)
                        .call();
                System.out.println("成功回滚到版本: " + commitId);
            } else {
                throw new InvalidRefNameException("分支不存在: " + branchName);
            }
        } catch (GitAPIException | IOException e) {
            throw new RefAlreadyExistsException("切换分支和版本失败");
        }
    }

    /**
     * 获取当前分支
     */
    public String getCurrentBranch() throws IOException {
        return git.getRepository().getBranch();
    }

    /**
     * 获取所有分支
     */
    public List<String> getAllBranches() throws GitAPIException {
        List<String> branches = new ArrayList<>();
        List<Ref> refs = git.branchList().call();
        for (Ref ref : refs) {
            branches.add(ref.getName().replace("refs/heads/", ""));
        }
        return branches;
    }

    /**
     * 获取提交历史
     */
    public List<RevCommit> getCommitHistory() throws GitAPIException, IOException {
        List<RevCommit> commits = new ArrayList<>();
        Iterable<RevCommit> revCommits = git.log().call();
        for (RevCommit commit : revCommits) {
            commits.add(commit);
        }
        return commits;
    }

    /**
     * 关闭Git连接
     */
    public void close() {
        if (git != null) {
            git.close();
        }
    }

    public static void main(String[] args) {
        try {
            // 配置参数
            String remoteUrl = "https://e.coding.net/persagy2021/bdtp/dtp-electricity-server.git";
            String username = "zhangpan@yushu.biz";
            String password = "XXXX";
            String localPath = "./local-repo";
            String branchName = "release";
            String commitId = "7cddb0916bda89df451dc3f419c72c2961f592d0"; // 提交ID

            // 创建Git操作实例
            GitOperations gitOps = new GitOperations(remoteUrl, username, password, localPath);

            // 克隆仓库
//            gitOps.cloneRepository();

            // 切换到指定分支的指定版本
            gitOps.checkoutBranchAndVersion(branchName, commitId);

//            // 获取当前分支
//            String currentBranch = gitOps.getCurrentBranch();
//            System.out.println("当前分支: " + currentBranch);
//
//            // 获取所有分支
//            List<String> branches = gitOps.getAllBranches();
//            System.out.println("所有分支: " + branches);
//
//            // 获取提交历史
//            List<RevCommit> commits = gitOps.getCommitHistory();
//            System.out.println("提交历史:");
//            for (RevCommit commit : commits) {
//                System.out.println("Commit: " + commit.getName() + " - " + commit.getFullMessage());
//            }

            // 关闭连接
            gitOps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}