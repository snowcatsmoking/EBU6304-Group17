package ZiqianCao;

import TA.java.TAApplication;
import TA.java.TAApplicationManager;
import data.DataConfig;
import java.io.File;

public class TAApplicationManagerTest {

    private static final String TEST_ID_PREFIX = "TEST2024";

    public static void main(String[] args) {
        TAApplicationManagerTest test = new TAApplicationManagerTest();
        
        System.out.println("========== TAApplicationManager 测试开始 ==========\n");
        
        test.cleanTestData();
        
        test.testCreateMultipleApplications();
        test.testCreateApplicationWithMissingFields();
        test.testCreateDuplicateApplication();
        
        System.out.println("========== TAApplicationManager 测试结束 ==========");
    }

    public void testCreateMultipleApplications() {
        System.out.println("【测试1】创建多个有效的助教申请");
        cleanTestData();
        
        TAApplicationManager manager = new TAApplicationManager();
        
        TAApplication application1 = new TAApplication(
            "张三",
            TEST_ID_PREFIX + "001",
            "计算机科学与技术",
            "138******00",
            "z*********@***********",
            "周一至周五 9:00-17:00",
            "Java, Python, 数据库"
        );
        
        TAApplication application2 = new TAApplication(
            "李四",
            TEST_ID_PREFIX + "002",
            "软件工程",
            "138******01",
            "l***@***********",
            "周二至周四 10:00-18:00",
            "C++, 算法, 数据结构"
        );
        
        TAApplication application3 = new TAApplication(
            "王五",
            TEST_ID_PREFIX + "003",
            "人工智能",
            "139******00",
            "w*******@***********",
            "周末",
            "机器学习, 深度学习, Python"
        );
        
        TAApplication application4 = new TAApplication(
            "赵六",
            TEST_ID_PREFIX + "004",
            "数据科学",
            "139******01",
            "z******@***********",
            "工作日晚上",
            "数据分析, SQL, R语言"
        );
        
        TAApplication application5 = new TAApplication(
            "孙七",
            TEST_ID_PREFIX + "005",
            "网络安全",
            "139******02",
            "s*****@***********",
            "周一三五 14:00-20:00",
            "网络安全, Linux, Python"
        );
        
        try {
            manager.createTAApplication(application1);
            System.out.println("  ✓ 申请1创建成功：张三 (" + TEST_ID_PREFIX + "001)");
            
            manager.createTAApplication(application2);
            System.out.println("  ✓ 申请2创建成功：李四 (" + TEST_ID_PREFIX + "002)");
            
            manager.createTAApplication(application3);
            System.out.println("  ✓ 申请3创建成功：王五 (" + TEST_ID_PREFIX + "003)");
            
            manager.createTAApplication(application4);
            System.out.println("  ✓ 申请4创建成功：赵六 (" + TEST_ID_PREFIX + "004)");
            
            manager.createTAApplication(application5);
            System.out.println("  ✓ 申请5创建成功：孙七 (" + TEST_ID_PREFIX + "005)");
            
            System.out.println("  ✓ 所有申请创建成功！\n");
            
            listCreatedFiles();
        } catch (Exception e) {
            System.out.println("  ✗ 测试失败：" + e.getMessage() + "\n");
        } finally {
            cleanTestData();
        }
    }

    public void testCreateApplicationWithMissingFields() {
        System.out.println("【测试2】创建缺少必填字段的申请");
        cleanTestData();
        
        TAApplicationManager manager = new TAApplicationManager();
        TAApplication application = new TAApplication(
            "",
            TEST_ID_PREFIX + "006",
            "软件工程",
            "",
            "l****@***********",
            "周一至周五",
            "C++, 算法"
        );
        
        try {
            manager.createTAApplication(application);
            System.out.println("  ✗ 测试失败：应该抛出校验异常\n");
        } catch (Exception e) {
            if (e.getMessage().contains("必填字段为空")
                    || e.getMessage().contains("required fields are empty")) {
                System.out.println("  ✓ 测试通过：正确捕获校验异常 - " + e.getMessage() + "\n");
            } else {
                System.out.println("  ✗ 测试失败：异常类型不正确 - " + e.getMessage() + "\n");
            }
        } finally {
            cleanTestData();
        }
    }

    public void testCreateDuplicateApplication() {
        System.out.println("【测试3】创建重复学号的申请");
        cleanTestData();
        
        TAApplicationManager manager = new TAApplicationManager();
        
        TAApplication application1 = new TAApplication(
            "重复测试1",
            TEST_ID_PREFIX + "007",
            "测试专业",
            "138******99",
            "t***@***********",
            "测试时间",
            "测试技能"
        );
        
        TAApplication application2 = new TAApplication(
            "重复测试2",
            TEST_ID_PREFIX + "007",
            "测试专业2",
            "138******88",
            "t***@***********",
            "测试时间2",
            "测试技能2"
        );
        
        try {
            manager.createTAApplication(application1);
            System.out.println("  第一条申请创建成功");
            
            manager.createTAApplication(application2);
            System.out.println("  ✗ 测试失败：应该抛出重复学号异常\n");
        } catch (Exception e) {
            if (e.getMessage().contains("已存在申请档案")
                    || e.getMessage().contains("already has an application profile")) {
                System.out.println("  ✓ 测试通过：正确捕获重复学号异常 - " + e.getMessage() + "\n");
            } else {
                System.out.println("  ✗ 测试失败：异常类型不正确 - " + e.getMessage() + "\n");
            }
        } finally {
            cleanTestData();
        }
    }

    private void listCreatedFiles() {
        File dir = new File(DataConfig.TA_DIR);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) ->
                    name.startsWith(TEST_ID_PREFIX) && name.endsWith(".json"));
            if (files != null && files.length > 0) {
                System.out.println("  测试创建的 JSON 文件：");
                for (File file : files) {
                    System.out.println("    - " + file.getName());
                }
                System.out.println("  共 " + files.length + " 个文件\n");
            }
        }
    }

    private void cleanTestData() {
        File dir = new File(DataConfig.TA_DIR);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.matches(TEST_ID_PREFIX + "00[1-7]\\.json"));
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
                System.out.println("  已清理测试数据文件\n");
            }
        }
    }
}
