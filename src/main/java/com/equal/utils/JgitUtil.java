package com.equal.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import com.google.common.base.Splitter;


import java.io.File;
import java.io.IOException;
import java.util.Date;

@Slf4j
public class JgitUtil {
    /**
     * 将git仓库内容回滚到指定版本的上一提交
     * @param gitRoot 仓库目录
     * @param commitId 指定的版本号
     * @return true,回滚成功,否则flase
     * @throws IOException
     */
    public static boolean rollBackPreRevision(String gitRoot, String commitId) {
        Repository repository = null;
        try {
            Git git = Git.open(new File(gitRoot));
            repository = git.getRepository();
            RevWalk walk = new RevWalk(repository);
            ObjectId objId = repository.resolve(commitId);
            RevCommit revCommit = walk.parseCommit(objId);
            String preVision = revCommit.getParent(0).getName();
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(preVision).call();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            repository.close();
        }
        return true;
    }


    /**
     * 克隆代码到本地
     *
     * @param gitUrl
     * @param codePath
     * @param branchOrTag
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public static Git cloneRepository(String gitUrl, String codePath, String branchOrTag,String gitUserName,String gitPassWord) {
        Git git = null;
        try {
            if (!checkGitWorkSpace(gitUrl,codePath)) {
                log.info("本地代码:{}不存在，clone:{}", gitUrl, codePath);
                git = Git.cloneRepository()
                        .setURI(gitUrl)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUserName, gitPassWord))
                        .setDirectory(new File(codePath))
                        .setBranch(branchOrTag)
                        .call();
                // 下载指定tag/branch
                git.checkout().setName(branchOrTag).call();
            } else {
                log.info("本地代码,存在，codepath:{}", codePath);
                git = Git.open(new File(codePath));
                git.getRepository().getFullBranch();
                //判断是分支还是commitId，分支做更新，commitId无法改变用原有的
                if (git.getRepository().exactRef(org.eclipse.jgit.lib.Constants.HEAD).isSymbolic()) {
                    //更新代码
                    git.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUserName, gitPassWord)).call();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return git;
    }


    /**
     * 将代码转成树状
     *
     * @param repository
     * @param branchName
     * @return
     */
    public static AbstractTreeIterator prepareTreeParser(Repository repository, String branchName) {
        try {
            RevWalk walk = new RevWalk(repository);
            RevTree tree;
            if(null == repository.resolve(branchName)){
            }
            tree = walk.parseTree(repository.resolve(branchName));
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    /**
     * 判断工作目录是否存在，本来可以每次拉去代码时删除再拉取，但是这样代码多的化IO比较大，所以就代码可以复用
     *
     * @param codePath
     * @return
     */
    public static Boolean checkGitWorkSpace(String gitUrl, String codePath) throws IOException {
        System.out.println(gitUrl);
        System.out.println(codePath);
        Boolean isExist = Boolean.FALSE;
        File repoGitDir = new File(codePath + "/git");
        if (!repoGitDir.exists()) {
            return isExist;
        }
        Git git = Git.open(new File(codePath));
        if (null == git) {
            return isExist;
        }
        Repository repository = git.getRepository();
        //解析本地代码，获取远程uri,是否是我们需要的git远程仓库
        String repoUrl = repository.getConfig().getString("remote", "origin", "url");
        if (gitUrl.equals(repoUrl)) {
            isExist = Boolean.TRUE;
        } else {
            log.info("本地存在其他仓的代码，先删除");
            FileUtil.removeDir(new File(codePath));
        }
        return isExist;
    }



    /**
     * 取远程代码本地存储路径
     *
     * @param repoUrl giturl
     * @param version version
     * @return
     */
    public static String getLocalGitDir(String repoUrl, String version) {
        StringBuilder sb = new StringBuilder(Constants.GIT_PATH);
        String repoName = Splitter.on("/")
                .splitToStream(repoUrl).reduce((first, second) -> second)
                .map(e -> Splitter.on(".").splitToStream(e).findFirst().get()).get();
        sb.append("/").append(repoName).append("-").append(version);
        return sb.toString();
    }
}
