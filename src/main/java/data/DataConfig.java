package data;

import java.io.File;

/**
 * 全局数据路径配置。
 * 所有模块（TA/MO/Admin）统一从这里引用路径常量，禁止在其他类中硬编码路径字符串。
 */
public class DataConfig {

    // ==================== 基础路径 ====================

    public static final String BASE_DIR         = "data/";

    // ==================== 用户数据目录 ====================

    /** TA 个人档案，文件名格式：{学号}.json，内容为 TAApplication 对象 */
    public static final String TA_DIR           = BASE_DIR + "TAData/";

    /** MO 账号数据，文件名格式：{工号}.json，内容为 User 对象 */
    public static final String MO_DIR           = BASE_DIR + "MOData/";

    /** Admin 账号数据，文件名格式：{账号}.json，内容为 User 对象 */
    public static final String ADMIN_DIR        = BASE_DIR + "AdminData/";

    // ==================== 业务数据目录 ====================

    /** 职位数据，文件名格式：{jobId}.json，内容为 TAJob 对象 */
    public static final String JOB_DIR          = BASE_DIR + "JobData/";

    /** 申请记录，文件名格式：{UUID}.json，内容为 TAApplicationRecord 对象 */
    public static final String APPLICATION_DIR  = BASE_DIR + "ApplicationData/";

    /** 操作日志，文件名格式：log_{yyyy-MM-dd}.txt */
    public static final String LOG_DIR          = BASE_DIR + "Logs/";

    // ==================== 工具方法 ====================

    /**
     * 确保指定目录存在，不存在则自动创建。
     * 在各 Manager 的构造函数中调用。
     */
    public static void ensureDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 初始化所有数据目录，程序启动时调用一次即可。
     */
    public static void initAllDirs() {
        ensureDir(TA_DIR);
        ensureDir(MO_DIR);
        ensureDir(ADMIN_DIR);
        ensureDir(JOB_DIR);
        ensureDir(APPLICATION_DIR);
        ensureDir(LOG_DIR);
    }
}
