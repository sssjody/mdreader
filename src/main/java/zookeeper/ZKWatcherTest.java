package zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ZKWatcherTest {

    private CuratorFramework curator;

    @Before
    public void testConnect() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        curator = CuratorFrameworkFactory.newClient("192.168.80.128:2181", 60 * 1000,
                15 * 1000, retryPolicy);
        curator.start();
    }

    /**
     * 测试 给单个节点NodeCache注册监听
     *
     * @throws Exception
     */
    @Test
    public void testNodeCache() throws Exception {
        // 创建nodeCache对象
        NodeCache nodeCache = new NodeCache(curator, "/app4/p5");
        CountDownLatch latch = new CountDownLatch(1);
        // 注册监听
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                byte[] bytes = nodeCache.getCurrentData().getData();
                System.out.println(new String(bytes));
            }
        });

        // 开启监听
        nodeCache.start();
        latch.await();
    }

    /**
     * 测试 给单个节点的所有子节点注册监听
     */
    @Test
    public void testPathChildrenCache() throws Exception {
        PathChildrenCache childrenCache = new PathChildrenCache(curator, "/app4", true);
        CountDownLatch latch = new CountDownLatch(1);
        childrenCache.getListenable()
                .addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                        System.out.println(pathChildrenCacheEvent);
                    }
                });

        childrenCache.start();
        latch.await();
    }

    /**
     * 测试 给单个节点以及单个节点的子节点注册监听
     * @throws Exception
     */
    @Test
    public void testTreeCache() throws Exception {
        TreeCache treeCache = new TreeCache(curator, "/app4");

        treeCache.getListenable().addListener(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                System.out.println(treeCacheEvent);
            }
        });

        treeCache.start();
    }
}
