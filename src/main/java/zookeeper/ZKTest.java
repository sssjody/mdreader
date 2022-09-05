package zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZKTest {

    private CuratorFramework curator;

    @Before
    public void testConnect() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        curator = CuratorFrameworkFactory.newClient("192.168.80.128:2181", 60 * 1000,
                15 * 1000, retryPolicy);
        curator.start();
    }

    /**
     *  创建节点
     */
    @Test
    public void testCreate() throws Exception {
        String path = curator.create().forPath("/app2", "hello world".getBytes());
        System.out.println(path);
    }

    /**
     *   创建不同模式节点
     */
    @Test
    public void testCreateWithMode() throws Exception {
        String path = curator.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                .forPath("/app3");
        System.out.println(path);
    }

    /**
     *   创建级联模式节点
     */
    @Test
    public void testCreateWithParent() throws Exception {
        String path = curator.create().creatingParentsIfNeeded()
                .forPath("/app4/p4");
        System.out.println(path);
    }


    /**
     *   获取节点数据
     */
    @Test
    public void testGetData() throws Exception {
        byte[] bytes = curator.getData().forPath("/app2");
        System.out.println(new String(bytes));
    }

    /**
     *   获取子节点
     */
    @Test
    public void testGetNodes() throws Exception {
        List<String> path = curator.getChildren().forPath("/");
        System.out.println(path);
    }

    /**
     *   获取节点状态信息
     */
    @Test
    public void testGetNodeState() throws Exception {
        Stat stat = new Stat();
        byte[] bytes = curator.getData().storingStatIn(stat)
                .forPath("/app1");
        System.out.println(stat);
    }

    /**
     *  设置节点数据
     */
    @Test
    public void testSetData() throws Exception {
        curator.setData().forPath("/app1", "hehe".getBytes());
    }

    /**
     *  设置带版本号的节点数据
     */
    @Test
    public void testSetDataWithVersion() throws Exception {
        Stat stat = new Stat();
        curator.getData().storingStatIn(stat).forPath("/app1");

        int version = stat.getVersion();
        curator.setData().withVersion(version).forPath("/app1", "haha".getBytes());
    }

    /**
     *  删除没有子节点的单个节点
     */
    @Test
    public void testDelete() throws Exception {
        curator.delete().forPath("/app2");
    }

    /**
     *  删除带子节点的节点
     */
    @Test
    public void testDeleteWithChildren() throws Exception {
        curator.delete().deletingChildrenIfNeeded().forPath("/app1");
    }

    /**
     *  删除节点保证成功(guarantee会不断重试保证删除成功)
     */
    @Test
    public void testDeleteWithGuarantee() throws Exception {
        curator.delete().guaranteed().forPath("/app1");
    }

    /**
     *  删除节点完成之后的回调方法
     */
    @Test
    public void testDeleteWithCallback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        curator.delete()
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        latch.countDown();
                        System.out.println(event);
                    }
                })
                .forPath("/app6");
        latch.await();
    }
}
