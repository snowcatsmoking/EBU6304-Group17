package ZiqianCao.java;

public class TAApplicationTest {

    public static void main(String[] args) {
        System.out.println("=== 测试申请功能 ===\n");

        TAApplicationRecordManager manager = new TAApplicationRecordManager();
        TAJob job = new TAJob("J001", "软件工程课程助教", "软件工程", 2, "3.5及以上", "2025年9月15日", "张老师", false);

        System.out.println("测试1: 检查重复申请");
        boolean hasDuplicate = manager.hasDuplicateApplication("2024004", job.getJobId());
        System.out.println("是否已申请: " + hasDuplicate);
        System.out.println();

        System.out.println("测试2: 创建申请记录");
        TAApplicationRecord record = new TAApplicationRecord(
            "2024004",
            job.getJobId(),
            job.getPositionName(),
            job.getCourseName(),
            "张三",
            "计算机科学",
            "13800138000",
            "zhangsan@example.com",
            "周一至周五",
            "Java, Python, 数据结构"
        );

        System.out.println("申请ID: " + record.getApplicationId());
        System.out.println("学生ID: " + record.getStudentId());
        System.out.println("岗位ID: " + record.getJobId());
        System.out.println("岗位名称: " + record.getPositionName());
        System.out.println("课程名称: " + record.getCourseName());
        System.out.println("申请状态: " + record.getStatus());
        System.out.println("申请时间: " + record.getApplicationDate());
        System.out.println();

        System.out.println("测试3: 保存申请记录");
        manager.saveApplication(record);
        System.out.println("申请记录已保存");
        System.out.println();

        System.out.println("测试4: 再次检查重复申请");
        hasDuplicate = manager.hasDuplicateApplication("2024004", job.getJobId());
        System.out.println("是否已申请: " + hasDuplicate);
        System.out.println();

        System.out.println("测试5: 查询所有申请记录");
        var allRecords = manager.getAllApplications();
        System.out.println("所有申请记录数量: " + allRecords.size());
        for (TAApplicationRecord r : allRecords) {
            System.out.println("- " + r.getStudentName() + " 申请了 " + r.getPositionName() + " (" + r.getStatus() + ")");
        }
        System.out.println();

        System.out.println("测试6: 根据学生ID查询申请记录");
        var studentRecords = manager.getApplicationsByStudentId("2024004");
        System.out.println("学生2024004的申请记录数量: " + studentRecords.size());
        for (TAApplicationRecord r : studentRecords) {
            System.out.println("- " + r.getPositionName() + " (" + r.getStatus() + ")");
        }

        System.out.println("\n=== 测试完成 ===");
    }
}
