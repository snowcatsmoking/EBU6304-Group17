package ZiqianCao.Test;

import ZiqianCao.java.TAApplication;
import ZiqianCao.java.TAApplicationManager;
import java.io.File;

public class TAApplicationManagerTest {

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
        
        TAApplicationManager manager = new TAApplicationManager();
        
        TAApplication application1 = new TAApplication(
            "张三",
            "2024001",
            "计算机科学与技术",
            "138******00",
            "z*********@***********",
            "周一至周五 9:00-17:00",
            "Java, Python, 数据库"
        );
        
        TAApplication application2 = new TAApplication(
            "李四",
            "2024002",
            "软件工程",
            "138******01",
            "l***@***********",
            "周二至周四 10:00-18:00",
            "C++, 算法, 数据结构"
        );
        
        TAApplication application3 = new TAApplication(
            "王五",
            "2024003",
            "人工智能",
            "139******00",
            "w*******@***********",
            "周末",
            "机器学习, 深度学习, Python"
        );
        
        TAApplication application4 = new TAApplication(
            "赵六",
            "2024004",
            "数据科学",
            "139******01",
            "z******@***********",
            "工作日晚上",
            "数据分析, SQL, R语言"
        );
        
        TAApplication application5 = new TAApplication(
            "孙七",
            "2024005",
            "网络安全",
            "139******02",
            "s*****@***********",
            "周一三五 14:00-20:00",
            "网络安全, Linux, Python"
        );
        
        try {
            manager.createTAApplication(application1);
            System.out.println("  ✓ 申请1创建成功：张三 (2024001)");
            
            manager.createTAApplication(application2);
            System.out.println("  ✓ 申请2创建成功：李四 (2024002)");
            
            manager.createTAApplication(application3);
            System.out.println("  ✓ 申请3创建成功：王五 (2024003)");
            
            manager.createTAApplication(application4);
            System.out.println("  ✓ 申请4创建成功：赵六 (2024004)");
            
            manager.createTAApplication(application5);
            System.out.println("  ✓ 申请5创建成功：孙七 (2024005)");
            
            System.out.println("  ✓ 所有申请创建成功！\n");
            
            listCreatedFiles();
        } catch (Exception e) {
            System.out.println("  ✗ 测试失败：" + e.getMessage() + "\n");
        }
    }

    public void testCreateApplicationWithMissingFields() {
        System.out.println("【测试2】创建缺少必填字段的申请");
        
        TAApplicationManager manager = new TAApplicationManager();
        TAApplication application = new TAApplication(
            "",
            "2024006",
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
            if (e.getMessage().contains("必填字段为空")) {
                System.out.println("  ✓ 测试通过：正确捕获校验异常 - " + e.getMessage() + "\n");
            } else {
                System.out.println("  ✗ 测试失败：异常类型不正确 - " + e.getMessage() + "\n");
            }
        }
    }

    public void testCreateDuplicateApplication() {
        System.out.println("【测试3】创建重复学号的申请");
        
        TAApplicationManager manager = new TAApplicationManager();
        
        TAApplication application1 = new TAApplication(
            "重复测试1",
            "2024007",
            "测试专业",
            "138******99",
            "t***@***********",
            "测试时间",
            "测试技能"
        );
        
        TAApplication application2 = new TAApplication(
            "重复测试2",
            "2024007",
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
            if (e.getMessage().contains("已存在申请档案")) {
                System.out.println("  ✓ 测试通过：正确捕获重复学号异常 - " + e.getMessage() + "\n");
            } else {
                System.out.println("  ✗ 测试失败：异常类型不正确 - " + e.getMessage() + "\n");
            }
        }
    }

    private void listCreatedFiles() {
        File dir = new File("resources/Data/TAData/");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null && files.length > 0) {
                System.out.println("  当前目录下的 JSON 文件：");
                for (File file : files) {
                    System.out.println("    - " + file.getName());
                }
                System.out.println("  共 " + files.length + " 个文件\n");
            }
        }
    }

    private void cleanTestData() {
        File dir = new File("resources/Data/TAData/");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
                System.out.println("  已清理测试数据文件\n");
            }
        }
    }
}
