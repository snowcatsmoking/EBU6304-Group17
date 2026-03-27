package ZiqianCao.Test;

import ZiqianCao.java.TAApplication;
import ZiqianCao.java.TAApplicationManager;
import java.io.File;

public class TAApplicationManagerTest {

    public static void main(String[] args) {
        TAApplicationManagerTest test = new TAApplicationManagerTest();
        
        System.out.println("========== TAApplicationManager 测试开始 ==========\n");
        
        test.testCreateValidApplication();
        test.testCreateApplicationWithMissingFields();
        test.testCreateDuplicateApplication();
        
        System.out.println("========== TAApplicationManager 测试结束 ==========");
    }

    public void testCreateValidApplication() {
        System.out.println("【测试1】创建有效的助教申请");
        
        cleanTestData();
        
        TAApplicationManager manager = new TAApplicationManager();
        TAApplication application = new TAApplication(
            "张三",
            "2024001",
            "计算机科学与技术",
            "13800138000",
            "zhangsan@example.com",
            "周一至周五 9:00-17:00",
            "Java, Python, 数据库"
        );
        
        try {
            manager.createTAApplication(application);
            System.out.println("  ✓ 测试通过：申请创建成功\n");
        } catch (Exception e) {
            System.out.println("  ✗ 测试失败：" + e.getMessage() + "\n");
        }
    }

    public void testCreateApplicationWithMissingFields() {
        System.out.println("【测试2】创建缺少必填字段的申请");
        
        cleanTestData();
        
        TAApplicationManager manager = new TAApplicationManager();
        TAApplication application = new TAApplication(
            "",
            "2024002",
            "软件工程",
            "",
            "lisi@example.com",
            "周一至周五",
            "C++, 算法"
        );
        
        try {
            manager.createTAApplication(application);
            System.out.println("  ✗ 测试失败：应该抛出校验异常\n");
        } catch (Exception e) {
            if (e.getMessage().contains("必填字段为空")) {
                System.out.println("  ✓ 测试通过：正确捕获校验异常 - " + e.getMessage() + "\n");
            } else {
                System.out.println("  ✗ 测试失败：异常类型不正确 - " + e.getMessage() + "\n");
            }
        }
    }

    public void testCreateDuplicateApplication() {
        System.out.println("【测试3】创建重复学号的申请");
        
        cleanTestData();
        
        TAApplicationManager manager = new TAApplicationManager();
        TAApplication application1 = new TAApplication(
            "王五",
            "2024003",
            "人工智能",
            "13900139000",
            "wangwu@example.com",
            "周末",
            "机器学习, 深度学习"
        );
        
        TAApplication application2 = new TAApplication(
            "赵六",
            "2024003",
            "数据科学",
            "13900139001",
            "zhaoliu@example.com",
            "工作日晚上",
            "数据分析, SQL"
        );
        
        try {
            manager.createTAApplication(application1);
            System.out.println("  第一条申请创建成功");
            
            manager.createTAApplication(application2);
            System.out.println("  ✗ 测试失败：应该抛出重复学号异常\n");
        } catch (Exception e) {
            if (e.getMessage().contains("已存在申请档案")) {
                System.out.println("  ✓ 测试通过：正确捕获重复学号异常 - " + e.getMessage() + "\n");
            } else {
                System.out.println("  ✗ 测试失败：异常类型不正确 - " + e.getMessage() + "\n");
            }
        }
    }

    private void cleanTestData() {
        File file = new File("ta_applications.json");
        if (file.exists()) {
            file.delete();
        }
    }
}
